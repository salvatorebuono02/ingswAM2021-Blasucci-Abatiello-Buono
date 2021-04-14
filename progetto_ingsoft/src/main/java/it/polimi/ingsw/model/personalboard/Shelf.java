package it.polimi.ingsw.model.personalboard;

import it.polimi.ingsw.exceptions.ResourceNotValidException;
import it.polimi.ingsw.model.Resources;

import java.util.ArrayList;

public class Shelf {
    private ArrayList<Resources> slots;
    private Resources resourceType;
    private boolean shelfAvailability ;
    private int maxSize;

    public Shelf(int maxSize) {
        this.maxSize = maxSize;
        this.slots=new ArrayList<Resources>();
    }

    /**
     * @return true if there is enough space in order to store resources in specialshelf
     */
    public boolean isShelfAvailability() {
        if(slots.size()<=maxSize )
            shelfAvailability=true;
        else
            shelfAvailability=false;
        return shelfAvailability;
    }

    /**
     * @param resource is what we want add in shelf
     * @return slot after adding resource. Adding is allow only in case of shelf availability and compatibility with existing resources type.
     */
    public ArrayList<Resources> addResources(Resources resource) throws ResourceNotValidException {
        if((isShelfAvailability())&&(resource==resourceType)&&(!slots.isEmpty())) {
            slots.add(resource);
        }
        else if(slots.isEmpty()){
            resourceType=resource;
            slots.add(resource);
        }
        else
            throw new ResourceNotValidException();
        return slots;
    }

    /**
     * @return all resources contained in shelf
     */
    public ArrayList<Resources> getSlots() {
        return slots;
    }

}