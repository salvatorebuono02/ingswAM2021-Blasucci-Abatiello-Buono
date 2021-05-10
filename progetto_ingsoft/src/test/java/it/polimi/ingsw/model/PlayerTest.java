package it.polimi.ingsw.model;

import it.polimi.ingsw.exceptions.AbilityAlreadySetException;
import it.polimi.ingsw.exceptions.ActionAlreadySet;
import it.polimi.ingsw.exceptions.ResourceNotValidException;
import it.polimi.ingsw.exceptions.WrongAbilityInCardException;
import it.polimi.ingsw.model.cards.LeadAbility;
import it.polimi.ingsw.model.cards.LeadAbilityWhiteMarble;
import it.polimi.ingsw.model.cards.LeadCard;
import it.polimi.ingsw.model.cards.LeadDeck;
import it.polimi.ingsw.model.cards.cardExceptions.CardChosenNotValidException;
import it.polimi.ingsw.model.cards.cardExceptions.playerLeadsNotEmptyException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void doBasicProduction() throws ResourceNotValidException {
        Player player=new Player("Pippo");
        ArrayList<Resource> prodInput=new ArrayList<>();
        prodInput.add(Resource.SERVANT);
        prodInput.add(Resource.SHIELD);
        player.getPersonalBoard().getWarehouseDepots().addinShelf(0, Resource.SERVANT);
        player.getPersonalBoard().getStrongBox().addInStrongbox(Resource.SHIELD);
        assertEquals(Resource.COIN,player.doBasicProduction(prodInput,Resource.COIN));
    }


    @Test
    void doNotValidBasicProduction1() throws ResourceNotValidException {
        Player player=new Player("Paki");
        ArrayList<Resource> prodInput=new ArrayList<>();
        prodInput.add(Resource.SERVANT);
        prodInput.add(Resource.SHIELD);
        player.getPersonalBoard().getWarehouseDepots().addinShelf(0, Resource.SERVANT);

        assertThrows(ResourceNotValidException.class,()-> player.doBasicProduction(prodInput,Resource.COIN));
    }


    //TODO divide the test in different tries
    @Test
    void choose2leadsWorks() throws WrongAbilityInCardException, CardChosenNotValidException, AbilityAlreadySetException, playerLeadsNotEmptyException {

        LeadDeck deck= new LeadDeck();
        Player player = new Player("Ciccio");
        deck.giveToPlayer(player);
        ArrayList<LeadCard> oldPlayerCards= player.getLeadCards();

        player.choose2Leads(player.getLeadCards().get(0), player.getLeadCards().get(1));

        assertEquals(oldPlayerCards.get(0),player.getLeadCards().get(0));
        assertEquals(oldPlayerCards.get(1),player.getLeadCards().get(1));

        assertTrue(player.getLeadCards().size()==2);

    }


    @Test
    void checkSetAction() throws ActionAlreadySet {
        Player player= new Player("Jenny");
        player.setAction(Action.ACTIVATEPRODUCTION);
        assertEquals(Action.ACTIVATEPRODUCTION,player.getAction());
    }

    @Test
    void checkExceptionSetAction() throws ActionAlreadySet {
        Player player= new Player("USER");
        player.setAction(Action.ACTIVATEPRODUCTION);
        assertThrows(ActionAlreadySet.class,()->player.setAction(Action.BUYCARD));
    }

}