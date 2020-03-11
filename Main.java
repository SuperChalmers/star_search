/*
David Hansen
dhansen33@gatech.edu
*/

public class Main {
    private static int MAX_DRONES = 10;
 
    public static void main(final String[] args) {
        final SimManager monitorSim = new SimManager();
        int trackTurnsCompleted = 0;
        Action DroneAction = new Action();
        Boolean showState = Boolean.FALSE;
        Drone[] drones = new Drone[MAX_DRONES];
          
    

        // check for the test scenario file name
        if (args.length < 1) {
            System.out.println("ERROR: Test scenario file name not found.");
            return;
        }

        if (args.length >= 2 && (args[1].equals("-v") || args[1].equals("-verbose"))) { showState = Boolean.TRUE; }

        drones = monitorSim.uploadStartingFile(args[0]);

        // run the simulation for a fixed number of steps
        for(int turns = 0; turns < monitorSim.simulationDuration(); turns++) {
            trackTurnsCompleted = turns;

            if (monitorSim.dronesAllStopped()) { break; }
            if (monitorSim.isSpaceExplored()) {break;} //looked through all space

            for (int k = 0; k < monitorSim.droneCount(); k++) {
                 
                if(drones[k].isDroneActive()){
                    DroneAction = drones[k].pollForAction(monitorSim.getKnownSpace());
                    monitorSim.newValidateDroneAction(DroneAction, k);
                    monitorSim.newDisplayActionAndResponses(DroneAction, k);
 
                    // render the state of the space region after each command
                    if(showState) { monitorSim.newRenderRegion(); }
                }

            }
        }

        monitorSim.finalReport(trackTurnsCompleted);
    }

}
