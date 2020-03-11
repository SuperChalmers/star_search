/*
David Hansen
dhansen33@gatech.edu
*/

import java.util.Scanner;
import java.util.HashMap;

import java.io.*;

public class SimManager {
    private static int MAX_THRUST = 3;
    private SpaceRegion region;
    private Drone[] drones;
    private SpaceRegion knownSpace;
    private Integer numberOfDrones;
    private final HashMap<DroneOrientation, Integer> xMAP;
    private final HashMap<DroneOrientation, Integer> yMAP;
    private String trackMoveCheck;
    private String trackScanResults;
    private Integer turnLimit;    
 
    public SimManager() {
 
        region = new SpaceRegion(); //this is the general region of the problem
        knownSpace = new SpaceRegion(); //this stores the drone's explored knowledge
        knownSpace.newKnownSpace();

        numberOfDrones = -1;
    
        xMAP = DroneOrientation.getXMAP();
        yMAP = DroneOrientation.getYMAP();
        turnLimit = -1;
    }

    public Drone[] uploadStartingFile(final String testFileName) {
        final String DELIMITER = ",";
        int regionHeight, regionWidth;
        
        try {
            final Scanner takeCommand = new Scanner(new File(testFileName));
            String[] tokens;
            int k;

            // read in the region information
            tokens = takeCommand.nextLine().split(DELIMITER);
            regionWidth = Integer.parseInt(tokens[0]);
            tokens = takeCommand.nextLine().split(DELIMITER);
            regionHeight = Integer.parseInt(tokens[0]);

            // generate the region information
            region = new SpaceRegion(regionWidth, regionHeight);
            knownSpace = new SpaceRegion(regionWidth, regionHeight);
            knownSpace.newKnownSpace();

            // read in the drone starting information
            tokens = takeCommand.nextLine().split(DELIMITER);
            numberOfDrones = Integer.parseInt(tokens[0]);
            drones = new Drone[numberOfDrones]; //create a collection of drones the size of the number of drones

            for (k = 0; k < numberOfDrones; k++) {
                Drone tempDrone = new Drone();
                tokens = takeCommand.nextLine().split(DELIMITER);
                tempDrone.setXloc(Integer.parseInt(tokens[0]));
                tempDrone.setYloc(Integer.parseInt(tokens[1]));
                tempDrone.SetDroneOrientationStr(tokens[2]);
                tempDrone.setDroneStrat(Integer.parseInt(tokens[3]));
                drones[k] = tempDrone; //add the drone to the collection

                // explore the stars at the initial location
                region.exploreRegion(tempDrone.getXLoc(), tempDrone.getYLoc());
                knownSpace.exploreRegion(tempDrone.getXLoc(), tempDrone.getYLoc()); //explore known space.
            }

            // read in the sun information
            tokens = takeCommand.nextLine().split(DELIMITER);
            final int numSuns = Integer.parseInt(tokens[0]);
            for (k = 0; k < numSuns; k++) {
                tokens = takeCommand.nextLine().split(DELIMITER);
                final int xSunLoc = Integer.parseInt(tokens[0]);
                final int ySunLoc = Integer.parseInt(tokens[1]);

                region.setSunLocation(xSunLoc, ySunLoc);
            }

            tokens = takeCommand.nextLine().split(DELIMITER);
            turnLimit = Integer.parseInt(tokens[0]);

            takeCommand.close();
            return drones;
           
        } catch (final Exception e) {
            e.printStackTrace();
            System.out.println();
            return null; //don't return drones because something went wrong.
        }
        
    }

    public Integer simulationDuration() {
        return turnLimit;
    }

    public Integer droneCount() {
        return numberOfDrones;
    }

    public void newValidateDroneAction(Action a, final int id) {
        int xOrientation, yOrientation;

        if(a.getAction() == DroneAction.SCAN){
            ScanResult s = new ScanResult();
            s = scanAroundSquare(drones[id].getXLoc(), drones[id].getYLoc());
            drones[id].recieveScanResults(s);
            updateKnownSpace(s);
            trackScanResults = s.getScanString();
            trackMoveCheck = "ok";
        
        } else if(a.getAction() == DroneAction.PASS){
            trackMoveCheck = "ok";

        } else if(a.getAction() == DroneAction.STEER){
            drones[id].SetDroneOrientation(a.getDirection());
            trackMoveCheck = "ok";

        } else if(a.getAction() == DroneAction.THRUST){
            xOrientation = xMAP.get(drones[id].getDroneOrientation());
            yOrientation = yMAP.get(drones[id].getDroneOrientation());

            trackMoveCheck = "ok";
            int remainingThrust = a.getDistance();
            if(remainingThrust > MAX_THRUST){
                trackMoveCheck = "action_not_recognized";
            }

            while (remainingThrust > 0 && trackMoveCheck.equals("ok")) {
                final int newSquareX = drones[id].getXLoc() + xOrientation;
                final int newSquareY = drones[id].getYLoc() + yOrientation;

                if (newSquareX < 0 || newSquareX >= region.getRegionWidth() || newSquareY < 0 || newSquareY >= region.getRegionHeight()) {
                    // drone hit a barrier and simply doesn't move (do nothing)
                    trackMoveCheck = "boundary";
                	
                
                } else if(region.getRegionState(newSquareX, newSquareY) == RegionCode.SUN_CODE) {// drone hit a sun)
                    drones[id].CrashDrone();
                    knownSpace.setSunLocation(newSquareX, newSquareY);
                    trackMoveCheck = "crash";

                } else if (numberOfDrones > 1 && trackMoveCheck.equals("ok")) {
                    // More than one drone. Check if they collided
                    int idroneCount = 0; // counter to walk through the drones
                    while (idroneCount < numberOfDrones) {
                        if (idroneCount == id) {
                            // Don't check the location of the same drone. It can't crash into itself
                        } else if(newSquareX == drones[idroneCount].getXLoc() && newSquareY == drones[idroneCount].getYLoc()){
                            // drone collided with the other drone
                            drones[id].CrashDrone();
                            drones[idroneCount].CrashDrone();
                            trackMoveCheck = "crash";
                        }
                        idroneCount++;
                    }
                
                } 
                if(trackMoveCheck.equals("ok")) {
                    // drone thrust is successful
                    // First clear the existing space to no longer be drone space for known space
                    knownSpace.exploreRegion(drones[id].getXLoc(), drones[id].getYLoc());
                    // Next, change the drone's location
                    drones[id].setXloc(newSquareX);
                    drones[id].setYloc(newSquareY);

                    // update region status
                    region.exploreRegion(newSquareX, newSquareY);
                    knownSpace.setDroneLocation(newSquareX, newSquareY);  //update known space with drone location
                    //knownSpace.exploreRegion(newSquareX, newSquareY); //update known space
                } else if(trackMoveCheck.equals("boundary")){
                	trackMoveCheck = "ok"; //set it back to ok after not doing anything
                }

                remainingThrust = remainingThrust - 1;
            }  
        } else {
                // in the case of an unknown action, treat the action as a pass
                trackMoveCheck = "action_not_recognized";
        }
    }

    public ScanResult scanAroundSquare(final int targetX, final int targetY) {
        ScanResult scan = new ScanResult(targetX, targetY);
    
        for (int k = 0; k < DroneOrientation.getSize() ; k++) {
            final DroneOrientation lookThisWay = DroneOrientation.chosenOrientation(k);
            final int offsetX = xMAP.get(lookThisWay);
            final int offsetY = yMAP.get(lookThisWay);
    
            final int checkX = targetX + offsetX;
            final int checkY = targetY + offsetY;
            boolean isDroneNextDoor = false;
    
            for(int droneID = 0;droneID < numberOfDrones; droneID++){
                if(drones[droneID].isDroneActive() && checkX == drones[droneID].getXLoc() && checkY == drones[droneID].getYLoc()){
                    isDroneNextDoor = true;
                }
            }
    
            if (checkX < 0 || checkX >= region.getRegionWidth() || checkY < 0 || checkY >= region.getRegionHeight()) {
                scan.setScanResult(k, RegionCode.BARRIER_CODE);
            } else if (isDroneNextDoor) { //check to see if there's a drone next door
                scan.setScanResult(k, RegionCode.DRONE_CODE);
            } else {
                switch (region.getRegionState(checkX, checkY)) {
                    case EMPTY_CODE:
                        scan.setScanResult(k, RegionCode.EMPTY_CODE);
                        break;
                    case STARS_CODE:
                        scan.setScanResult(k, RegionCode.STARS_CODE);
                        break;
                    case SUN_CODE:
                        scan.setScanResult(k, RegionCode.SUN_CODE);
                        break;
                    default:
                        scan.setScanResult(k, RegionCode.UNKNOWN_CODE);
                        break;
                    }
            }
        }
        return scan;
    }
    

    public void newDisplayActionAndResponses(final Action a, final int id) {
        // display the drone's actions
        System.out.print("d" + String.valueOf(id) + "," + a.getAction().toString());
        if(a.getAction() == DroneAction.STEER){
            System.out.println("," + a.getDirection().toString());
        } else if(a.getAction() == DroneAction.THRUST){
            System.out.println("," + a.getDistance());
        } else{
            System.out.println();
        }

        // display the simulation checks and/or responses
    
        switch (a.getAction()) {
            case THRUST:
            case STEER:
            case PASS:
                System.out.println(trackMoveCheck);
                break;
            case SCAN:
                System.out.println(trackScanResults);
                break;
            default:
                System.out.println("action_not_recognized");
                break;
        }
    }

    private void renderHorizontalBar(final int size) {
        System.out.print(" ");
        for (int k = 0; k < size; k++) {
            System.out.print("-");
        }
        System.out.println("");
    }

    public void newRenderRegion() {
        int i, j;
        boolean wroteDrone = false;
        final int charWidth = 2 * region.getRegionWidth() + 2;
        // display the rows of the region from top to bottom
        for (j = region.getRegionHeight() - 1; j >= 0; j--) {
            renderHorizontalBar(charWidth);

            // display the Y-direction identifier
            System.out.print(j);

            // display the contents of each square on this row
            for (i = 0; i < region.getRegionWidth(); i++) {
                System.out.print("|");

                // the drone overrides all other contents
                // loop through all the drones
                for(int x = 0; x < numberOfDrones; x++){
                    if(drones[x].isDroneActive() && i == drones[x].getXLoc() && j == drones[x].getYLoc()){
                        //if so, print out the number for the drone
                        System.out.print(x);
                        wroteDrone = true;
                    }

                }
                if(!wroteDrone){
                    switch (region.getRegionState(i, j)) {
                        case EMPTY_CODE:  
                            System.out.print(" ");
                            break;
                        case STARS_CODE:
                            System.out.print(".");
                            break;
                        case SUN_CODE:
                            System.out.print("s");
                            break;
                        default:
                            break;
                    }
                }
                wroteDrone = false;
            }
            System.out.println("|");
        }
        renderHorizontalBar(charWidth);

        // display the column X-direction identifiers
        System.out.print(" ");
        for (i = 0; i < region.getRegionWidth(); i++) {
            System.out.print(" " + i);
        }
        System.out.println("");

        // display the drone's directions
        for (int k = 0; k < numberOfDrones; k++) {
            if(drones[k].isDroneActive()){
                System.out.println("dir d" + String.valueOf(k) + ": " + drones[k].getDroneOrientation().toString());
            }
        }
        System.out.println("");
    }

    public Boolean dronesAllStopped() {
        for (int k = 0; k < numberOfDrones; k++) {
            if(drones[k].isDroneActive()){ //is the drone active?
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    public Boolean droneStopped(final int id) {
        return drones[id].isDroneActive();
    }

    public void finalReport(final int completeTurns) {
        int numSuns = 0;
        int numStars = 0;
        for (int i = 0; i < region.getRegionWidth(); i++) {
            for (int j = 0; j < region.getRegionHeight(); j++) {
                if (region.getRegionState(i, j)  == RegionCode.SUN_CODE) {
                    numSuns++;
                }
                if (region.getRegionState(i, j) == RegionCode.STARS_CODE) {
                    numStars++;
                }
            }
        }
        final int potentialCut = region.getRegionSize() - numSuns;
        final int actualCut = potentialCut - numStars;
        System.out.println(String.valueOf(region.getRegionSize()) + "," + String.valueOf(potentialCut) + "," + String.valueOf(actualCut) + "," + String.valueOf(completeTurns));
    }

    public SpaceRegion getKnownSpace(){
        return knownSpace;
    }

    public void updateKnownSpace(ScanResult sr){
        knownSpace.readScan(sr);
    }

    public boolean isSpaceExplored(){
        if(region.getSpaceLeftToExplore() == 0){
            return true;
        }
        return false;
    }
}