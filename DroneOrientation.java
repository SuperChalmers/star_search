/*
David Hansen
dhansen33@gatech.edu
*/

import java.util.List;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;

public enum DroneOrientation { NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST;

    private static final List<DroneOrientation> VALUES = Collections.unmodifiableList(Arrays.asList(values()));

    private static final int size = DroneOrientation.values().length;

    private static final HashMap<DroneOrientation, Integer> exMAP = new HashMap<>();
    private static final HashMap<DroneOrientation, Integer> eyMAP = new HashMap<>();

    public static DroneOrientation chosenOrientation(final int i) {
        return VALUES.get(i);
    }

    public static int getSize(){
        return size;
    } 

    public static HashMap<DroneOrientation, Integer> getXMAP(){
        exMAP.put(DroneOrientation.NORTH, 0);
        exMAP.put(DroneOrientation.NORTHEAST, 1);
        exMAP.put(DroneOrientation.EAST, 1);
        exMAP.put(DroneOrientation.SOUTHEAST, 1);
        exMAP.put(DroneOrientation.SOUTH, 0);
        exMAP.put(DroneOrientation.SOUTHWEST, -1);
        exMAP.put(DroneOrientation.WEST, -1);
        exMAP.put(DroneOrientation.NORTHWEST, -1);
        return exMAP;
    }

    public static HashMap<DroneOrientation, Integer> getYMAP(){
        eyMAP.put(DroneOrientation.NORTH, 1);
        eyMAP.put(DroneOrientation.NORTHEAST, 1);
        eyMAP.put(DroneOrientation.EAST, 0);
        eyMAP.put(DroneOrientation.SOUTHEAST, -1);
        eyMAP.put(DroneOrientation.SOUTH, -1);
        eyMAP.put(DroneOrientation.SOUTHWEST, -1);
        eyMAP.put(DroneOrientation.WEST, 0);
        eyMAP.put(DroneOrientation.NORTHWEST, 1);
        return eyMAP;
    }

};