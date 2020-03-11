/*
David Hansen
dhansen33@gatech.edu
*/

public class Action {

    private DroneOrientation direction;
    private DroneAction act;
    private Integer distance;

    public Action(){
        direction = DroneOrientation.NORTH; //default north
        act = DroneAction.PASS; //default pass
        distance = 0; //default no distance
    }

    public void setDirection(DroneOrientation o){
        direction = o;
    }

    public void setAction(DroneAction a){
        act = a;
    }

    public void setDistance(Integer d){
        distance = d;
    }

    public DroneOrientation getDirection(){
        return direction;
    }

    public DroneAction getAction(){
        return act;
    }

    public Integer getDistance(){
        return distance;
    }
}