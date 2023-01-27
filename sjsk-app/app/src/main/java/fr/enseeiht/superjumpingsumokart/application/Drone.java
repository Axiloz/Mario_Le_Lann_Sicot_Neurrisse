package fr.enseeiht.superjumpingsumokart.application;


import fr.enseeiht.superjumpingsumokart.application.items.*;

import fr.enseeiht.superjumpingsumokart.arpack.DetectionTask;

/**
 * Class representing a drone in the application.
 * It represents a drone as a player, not as a device.
 * @author Vivian Guy.
 */

public class Drone {

    /**
     * The current item the drone has.
     */
    private Item currentItem;

    /**
     * The current number of lap made by the drone on the {@link fr.enseeiht.superjumpingsumokart.application.circuit.Circuit#circuitInstance}.
     */
    private int currentLap;

    /**
     * The current number of checkpoint validated by the drone on the {@link fr.enseeiht.superjumpingsumokart.application.circuit.Circuit#circuitInstance}.
     */
    private int currentCheckpoint;

    /**
     * The last marker seen by the drone.
     */
    private DetectionTask.Symbol lastMarkerSeen;

    /**
     * Constructor for the class {@link Drone}.
     */
    Drone() {
        this.currentItem = new NullItem();
        this.currentCheckpoint = 0;
        this.currentLap = 0;
    }

    /**
     * Get the {@link Item} of the drone.
     * @return the current item of the drone.
     */
    public Item getCurrentItem() {
        return currentItem;
    }

    /**
     * Set the {@link Item} of the drone.
     * @param currentItem the new item of the drone.
     */
    public void setCurrentItem(Item currentItem) {

        this.currentItem = currentItem;
    }

    /**
     * @return the number of lap the drone has done.
     */
    public int getCurrentLap() {
        return currentLap;
    }

    /**
     * @param currentLap the number of lap the drone has done.
     */
    void setCurrentLap(int currentLap) {
        this.currentLap = currentLap;
    }

    /**
     *
     * @return The current number of checkpoints validated by the drone.
     */
    public int getCurrentCheckpoint() {
        return currentCheckpoint;
    }

    /**
     * @param currentCheckpoint the number of checkpoint validated by the drone.
     */
    void setCurrentCheckpoint(int currentCheckpoint) {
        this.currentCheckpoint = currentCheckpoint;
    }

    /**
     * @return The marker last marker seen by the drone.
     */
    public DetectionTask.Symbol getLastMarkerSeen() {
        return lastMarkerSeen;
    }

    /**
     * @param lastMarkerSeen The marker to put as last marker seen.
     */
    public void setLastMarkerSeen(DetectionTask.Symbol lastMarkerSeen) {
        this.lastMarkerSeen = lastMarkerSeen;
    }
}
