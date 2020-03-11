/*
David Hansen
dhansen33@gatech.edu
*/

import java.util.Scanner;
import java.util.Random;
import java.util.HashMap;

public class Drone {
    private static Random randGenerator;
    private final HashMap<DroneOrientation, Integer> xMAP;
    private final HashMap<DroneOrientation, Integer> yMAP;
    private static int MAX_THRUST = 3;

    private Integer locX; //x location of drone
    private Integer locY; //y location of drone
    private final Integer droneID;
    private boolean status; // false = disabled
    private DroneOrientation direction;
    private Integer strategy;
    private final HashMap<String, DroneOrientation> DIR_MAP;
    private ScanResult latestScan;
    private SpaceRegion knownSpace;


    private void setDroneOffMap() {
        locX = -1;
        locY = -1;
    }

    public void CrashDrone() {
        status = false;
        setDroneOffMap(); // if the drone is crashed, get it off the map
    }

    public Drone(){
        droneID = -1 ;  //init to negative 1
        setDroneOffMap(); // if we don't have a location yet, initialize it off the map.
        status = true; //init to active
        direction = DroneOrientation.NORTH; //default to north
        strategy = -1; //init to -1 
        DIR_MAP = new HashMap<>();
        DIR_MAP.put("north", DroneOrientation.NORTH);
        DIR_MAP.put("northeast", DroneOrientation.NORTHEAST);
        DIR_MAP.put("east", DroneOrientation.EAST);
        DIR_MAP.put("southeast", DroneOrientation.SOUTHEAST);
        DIR_MAP.put("south", DroneOrientation.SOUTH);
        DIR_MAP.put("southwest", DroneOrientation.SOUTHWEST);
        DIR_MAP.put("west", DroneOrientation.WEST);
        DIR_MAP.put("northwest", DroneOrientation.NORTHWEST);
        knownSpace = new SpaceRegion();
        knownSpace.newKnownSpace();
        latestScan = new ScanResult();
        xMAP = DroneOrientation.getXMAP();
        yMAP = DroneOrientation.getYMAP();

    }

    public void setXloc(final Integer x) {
        locX = x;
    }

    public void setYloc(final Integer y) {
        locY = y;
    }

    public Integer getXLoc() {
        return locX;
    }

    public Integer getYLoc() {
        return locY;
    }

    public Integer getDroneID() {
        return droneID;
    }

    public boolean isDroneActive() {
        return status;
    }

    public DroneOrientation getDroneOrientation() {
        return direction;
    }

    public void SetDroneOrientation(final DroneOrientation o) {
        direction = o;
    }
    public void SetDroneOrientationStr(final String str){
        direction = DIR_MAP.get(str);
    }

    public void setDroneStrat(final Integer s){
        strategy = s;
    }

    public Action pollForAction(SpaceRegion ks){
        final Action localAction = new Action();
        knownSpace = ks;
        randGenerator = new Random();
        int moveRandomChoice, thrustRandomChoice, steerRandomChoice;
        //DroneAction trackAction = DroneAction.PASS; //default to pass
        String userSuppliedAction, trackNewDirection;
        if(strategy == 2){
            final Scanner askUser = new Scanner(System.in);
            // generate a move by asking the user - DIAGNOSTIC ONLY
            System.out.print("action?(steer, thrust, scan, or pass): ");
            userSuppliedAction = askUser.nextLine();

            if (userSuppliedAction.equals("steer")) {
                localAction.setAction(DroneAction.STEER); 
                System.out.print("direction?: ");
                trackNewDirection = askUser.nextLine();
                localAction.setDirection(DIR_MAP.get(trackNewDirection));
            } else if (userSuppliedAction.equals("thrust")) {
                localAction.setAction(DroneAction.THRUST); 
                System.out.print("distance?: ");
                //trackThrustDistance = Integer.parseInt(askUser.nextLine());
                localAction.setDistance(Integer.parseInt(askUser.nextLine()));
            } else if (userSuppliedAction.equals("scan")){
                localAction.setAction(DroneAction.SCAN);
            } else if (userSuppliedAction.equals("pass")){
                localAction.setAction(DroneAction.PASS);
            }
            askUser.close();
        
        } else if(strategy == 1){
            int xOrientation, yOrientation;
            boolean foundAction = false;
            xOrientation = xMAP.get(direction);
            yOrientation = yMAP.get(direction);
            //First see if there's anything to scan, if yes, then scan
            for(int k = 0;k<DroneOrientation.getSize();k++){
                final DroneOrientation lookThisWay = DroneOrientation.chosenOrientation(k);
                final int checkX = locX + xMAP.get(lookThisWay);
                final int checkY = locY + yMAP.get(lookThisWay);
                
                if(knownSpace.getRegionState(checkX, checkY) == RegionCode.UNKNOWN_CODE){
                    //found an unscanned space
                    localAction.setAction(DroneAction.SCAN); //scan if this is unscanned
                    foundAction = true;
                    break;
                }
    
            }
            // Next, see if there are stars I can go to right away. Go to the farthest if it is safe
            if(!foundAction){
                for(int thrust = 0;thrust < MAX_THRUST;thrust++){
                    final int checkX = locX + xOrientation + (thrust * xMAP.get(direction));
                    final int checkY = locY + yOrientation + (thrust * yMAP.get(direction));
                    if(knownSpace.isSafeMove(checkX, checkY)){
                        if(knownSpace.isStars(checkX, checkY)){ //found stars in the direction we're facing
                            localAction.setAction(DroneAction.THRUST);//go there
                            localAction.setDistance(thrust+1); // +1 since we're starting with zero
                            localAction.setDirection(direction); //set the direction for the action the way the drone is facing
                            foundAction = true;
                        }
                    }else{
                        break; //not a safe move in the middle. Stop checking since you can't skip the spot. 
                    }
                }
            }
            // Next, see if there are stars in any direction I can go to 
            if(!foundAction){
                for(int k = 0;k<DroneOrientation.getSize();k++){
                    final DroneOrientation lookThisWay = DroneOrientation.chosenOrientation(k);
                    int checkX = locX + xMAP.get(lookThisWay);
                    int checkY = locY + yMAP.get(lookThisWay);
                    for(int range = 0; range < MAX_THRUST; range++){
                        checkX = checkX + (range * xMAP.get(lookThisWay));//* to factor the direction given the map
                        checkY = checkY + (range * yMAP.get(lookThisWay));
                        if(knownSpace.isSafeMove(checkX, checkY)){
                            if(knownSpace.isStars(checkX,checkY)){ //found stars around us in thrust range
                                //turn to face the stars
                                localAction.setAction(DroneAction.STEER);
                                localAction.setDirection(lookThisWay);
                                foundAction = true ;
                                break;
                            }
                        } else{
                            break;//not a safe move in the middle. don't try to go that way because you can't
                        }
                    }
                    if(foundAction){
                        break;
                    }
                }
            }
            // Next, see if there's a safe move in my current direction. go to the farthest safe move
            if(!foundAction){
                for(int thrust = 0;thrust < MAX_THRUST;thrust++){
                    final int checkX = locX + xOrientation + (thrust * xMAP.get(direction));
                    final int checkY = locY + yOrientation + (thrust * yMAP.get(direction));
                    if(knownSpace.isSafeMove(checkX, checkY)){ //found safe move in the direction we're facing
                        localAction.setAction(DroneAction.THRUST);//go there
                        localAction.setDistance(thrust+1); // +1 since we're starting with zero
                        localAction.setDirection(direction); //set the direction to the direction I'm facing
                        foundAction = true;
                    } else{
                        break; //once it's not a safe move stop trying
                    }
                }               
            }
            
            // Next, see if there's a safe move that is not where you came from
            if(!foundAction){
                for(int k = 0;k<DroneOrientation.getSize();k++){
                    final DroneOrientation lookThisWay = DroneOrientation.chosenOrientation(k);
                    int checkX = locX + xMAP.get(lookThisWay);
                    int checkY = locY + yMAP.get(lookThisWay);
                    if(lookThisWay.ordinal() == (direction.ordinal() + (DroneOrientation.values().length / 2)) || 
                        lookThisWay.ordinal() == (direction.ordinal() - (DroneOrientation.values().length / 2)) ){
                            //don't go back where you came if you don't have to 
                    }else{      
                        if(knownSpace.isSafeMove(checkX,checkY)){ //found safe move around us in thrust range
                            //turn to face the safe move
                            localAction.setAction(DroneAction.STEER);
                            localAction.setDirection(lookThisWay);
                            foundAction = true ;
                            break;
                        } 
                    }
                }               
            }

            // Turn back the way you came
            if(!foundAction){
                Integer dirOrdinal;
                if((direction.ordinal() + (DroneOrientation.values().length/2) > (DroneOrientation.values().length -1))){
                    dirOrdinal = direction.ordinal() - (DroneOrientation.values().length/2);
                } else{
                    dirOrdinal = direction.ordinal() + (DroneOrientation.values().length/2);
                }
                final DroneOrientation lookThisWay = DroneOrientation.chosenOrientation(dirOrdinal);
                localAction.setAction(DroneAction.STEER);
                localAction.setDirection(lookThisWay);
                foundAction = true ;
            }

            //no safte moves. No idea how I got here. Maybe surronded by suns to start with...
            if(!foundAction){
                localAction.setAction(DroneAction.PASS);
            }
        } 
        else {
            moveRandomChoice = randGenerator.nextInt(100);
            if (moveRandomChoice < 5) {
                // do nothing
                localAction.setAction(DroneAction.PASS);
            } else if (moveRandomChoice < 20) {
                // check your surroundings
                localAction.setAction(DroneAction.SCAN); 
            } else if (moveRandomChoice < 50) {
                // change direction
                localAction.setAction(DroneAction.STEER);
            } else {
                // thrust forward
                localAction.setAction(DroneAction.THRUST);
                thrustRandomChoice = randGenerator.nextInt(3);
                //trackThrustDistance = thrustRandomChoice + 1;
                localAction.setDistance(thrustRandomChoice + 1);
            }

            // determine a new direction
            steerRandomChoice = randGenerator.nextInt(8);
            if (localAction.getAction() == DroneAction.STEER ) { 
                localAction.setDirection(DroneOrientation.chosenOrientation(steerRandomChoice));
                //trackNewDirection = ORIENT_LIST[steerRandomChoice];
            }
        }

        return localAction;

    }
    public void recieveScanResults(ScanResult s){
        latestScan = s;
        knownSpace.readScan(latestScan);
    }
}
