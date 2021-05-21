package it.polimi.ingsw.server;

import it.polimi.ingsw.messages.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.stream.Collectors;
import it.polimi.ingsw.server.ConsoleColors;

public class ClientHandler implements Runnable {
    private Socket socket;
    private MainServer server;
    private ObjectInputStream inputStreamObj;
    private ObjectOutputStream outputStreamObj;
    private int clientID;
    private boolean active;
    private PingObserver pingObserver;

    public MainServer getServer() {
        return server;
    }

    /**
     * @param active sets the active connection field of this SocketServer object
     */
    public void setActive(boolean active) {
        this.active = active;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObjectOutputStream getOutputStreamObj() {
        return outputStreamObj;
    }

    /**
     Constructor of Client Handler which generates output and input stream of the socket
     */
    public ClientHandler(Socket socket, MainServer server) {
        System.out.println("sto creando il CH");
        this.socket = socket;
        this.server = server;
        try {
            OutputStream output= socket.getOutputStream();
            outputStreamObj = new ObjectOutputStream(output);
            InputStream input =socket.getInputStream();
            inputStreamObj = new ObjectInputStream(input) ;
            System.out.println("ho creato gli stream");
            active = true;
        } catch (IOException e) {
            System.err.println("Error during socket creation");
        }
    }

    public synchronized boolean isActive() {
        return active;
    }

    /**
     * Send a message to client and read the output stream as long as the socket is open
     */
    @Override
    public void run() {
        try {
            SerializedMessage input;
            do{
               send(new NickNameAction("Please choose a nickname"));
               input = (SerializedMessage) inputStreamObj.readObject();
               actionHandler(input);
            }while(isActive() && !(input instanceof NickNameAction));
            while (isActive()) {
                 input = (SerializedMessage) inputStreamObj.readObject();
                 if(!pingHandler(input))
                    actionHandler(input);
            }
            outputStreamObj.close();
            inputStreamObj.close();
            socket.close();
        }
        catch (IOException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * @param input is message sent by the client
     * @return false if input is not the type of ping
     */
    private boolean pingHandler(SerializedMessage input) {
        if(input instanceof PingMessage) {
            pingObserver.setResponse(true);
            return true;
        }
        return false;
    }

    /**
     * @param input is message sent by the client
     * This method handles each type of input: nickname reception, dimension of the lobby at its creation and messages referred to a Game Action
     */
    private synchronized void actionHandler(SerializedMessage input) {
        if (input instanceof NickNameAction) {
            clientID = checkNickName((NickNameAction) input);
            //TODO altri casi di ritorno clientID non validi
            System.out.println("sto creando il pingObserver");
            pingObserver = new PingObserver(this);
            System.out.println("sto salvando il pingObserver");
            MainServer.getConnectionServer().addPingObserver(pingObserver);
            System.out.println("ho salvato il pingObserver");
            if (clientID >= 0) {
                if (checkFirstPlayer(clientID))
                    send(new RequestNumOfPlayers(ConsoleColors.YELLOW_BACKGROUND_BRIGHT + ConsoleColors.BLUE_BOLD +"YOU ARE THE HOST OF A NEW LOBBY"
                            + " CHOOSE HOW MANY PLAYERS YOU WANT TO CHALLENGE [0 to 3]"+ConsoleColors.RESET));
            }else
                send(new NickNameAction(ConsoleColors.RED_UNDERLINED +"Nickname already taken." + " Please choose another one:" + ConsoleColors.RESET));
        }

        if (input instanceof NumOfPlayersAction) {
            int num = ((NumOfPlayersAction) input).getPlayersNum();
            System.out.println("ho ricevuto: " +num);
            if (num < 0 || num > 3)
                send(new RequestNumOfPlayers("Number of Player not valid. Please type a valid number [0 to 3]"));
            else {
                Lobby lobby = new Lobby(clientID, server.generateLobbyId(), num+1, server);
                //TODO controllo valido clientID
                System.out.println("Lobby di" + num + "giocatori creata con id: " + lobby.getLobbyID() + "." +
                        "inserisco l'host");
                lobby.insertPlayer(clientID);
                System.out.println("host inserito");
                send(new LobbyMessage("Lobby created. Wait for the other players to join!"));
            }
        } else if(input instanceof GameMessage)
            server.getClientFromId().get(clientID).getLobby().actionHandler((GameMessage) input,clientID);
    }

    /**
     * @param id is the player ID
     * @return false if player is already contained in someone lobby or if he is entered in a new lobby, true if every lobby is full
     */
    private boolean checkFirstPlayer(int id) {
        System.out.println("sei dentro check first player");
        if(server.getLobbyFromClientID().containsKey(id)) {
            System.out.println("client già in una lobby");
            return false;
        }
        ArrayList<Lobby> lobbies= (ArrayList<Lobby>) server.getLobbyFromClientID().values().stream().distinct().collect(Collectors.toList());
        for(Lobby lobby:lobbies) {
            if (!lobby.isLobbyFull()) {
                System.out.println("c'è una lobby libera");
                lobby.sendAll(new LobbyMessage(server.getNameFromId().get(id)+" is entered in the lobby"));
                lobby.insertPlayer(id);//TODO aggiungo nuovo client nella mappa ID-lobby
                return false;
            }
        }
        System.out.println("tutte le lobby sono piene, creo una nuova lobby");
        return true;
    }

    /**
     * @param message is the type of input given by the client
     * @return new player ID when he is inserted in a new game, -1 when he is already logged into
     */
    private int checkNickName(NickNameAction message) {
        int ID;
        System.out.println("sei dentro checknickname con: " + message.getMessage());
        for (String name : server.getNameFromId().values()) {
            System.out.println(name);
            if (message.getMessage().equals(name)) {
                ID = server.getIDFromName().get(name);
                System.out.println("nickname già scelto dall'utente: "+ID);
                if(server.getClientFromId().containsKey(ID)) {
                    System.out.println("l'utente " + ID + "è online. Il nuovo utente deve cambiare nickname");
                    return -1;
                }else if (server.getLobbyFromClientID().containsKey(ID)) {
                    System.out.println("l'utente" + ID + "non è collegato, ma esiste partita in cui giocava");
                    Lobby lobby = server.getLobbyFromClientID().get(ID);
                    if (lobby.getStateOfGame() != GameState.ENDED) {
                        System.out.println("la partita è in corso, il giocatore può essere ricollegato");
                        reconnectClient(ID, name);
                        lobby.reinsertPlayer(ID);
                        //TODO ricorda modifica lista virtual client in lobby quando disconnessione, gestisco poi riconessione e aggiunta
                        lobby.sendAll((SerializedMessage) new LobbyMessage(name + " is back in the game"));
                        return ID;
                    } else
                        reconnectClient(ID, name);
                    return ID;
                } else {
                    ID = connectClient(message.getMessage());
                    return ID;
                }
            }
        }
        ID = connectClient(message.getMessage());
        return ID;
}

    /**
     * @param name is the user's nickname
     * @return player ID after connected his client to the server
     */
    private int connectClient(String name) {
        int ID= server.getNameFromId().size() +1;
        server.getNameFromId().put(ID,name);
        server.getIDFromName().put(name,ID);
        VirtualClient newClient = new VirtualClient(ID, name, this.socket, this);
        server.getClientFromId().put(ID,newClient);
        return ID;
    }

    /**
     * @param ID is player ID
     * @param name is player's nickname
     */
    private void reconnectClient(int ID, String name) {
        VirtualClient newClient = new VirtualClient(ID, name, this.socket, this);
        server.getClientFromId().put(ID,newClient);
    }

    /**
     * @param message is sent to the client from server
     */
    public void send(SerializedMessage message){
        try {
            outputStreamObj.writeObject(message);
            outputStreamObj.flush();
        } catch (IOException e) {
            System.err.println("Impossibile inviare il messaggio. Chiudo il server");
        }
    }

    public Socket getSocket() {
        return this.socket;
    }

    public int getClientId() {
        return clientID;
    }

    public PingObserver getPingObserver() {
        return pingObserver;
    }
}

