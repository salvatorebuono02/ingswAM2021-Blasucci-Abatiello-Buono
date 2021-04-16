package it.polimi.ingsw.model.Market;

import it.polimi.ingsw.exceptions.FullSupplyException;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Resource;
import it.polimi.ingsw.model.personalboard.FaithMarker;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BlueMarbleTest {

    /*
    this Test is implemented to check if a BlueMarble in changed correctly
     */
    @Test
    void changeMarbleTest() throws FullSupplyException {
        BlueMarble marble=new BlueMarble();
        ResourceSupply supply=new ResourceSupply();
        marble.changeMarble(new FaithMarker(),new Player(0));
        ArrayList<Resource> resources=new ArrayList<>();
        resources.add(marble.resource);
        assertEquals(resources,supply.showSupply());
    }

}