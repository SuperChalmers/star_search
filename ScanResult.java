/*
David Hansen
dhansen33@gatech.edu
*/

public class ScanResult {

    private Integer xHome;
    private Integer yHome;
    private RegionCode[] results;

    public ScanResult()
    {
        xHome = -1;
        yHome = -1;
        results = new RegionCode[DroneOrientation.getSize()];
    }
    public ScanResult(Integer x, Integer y){
        xHome = x;
        yHome = y;
        results = new RegionCode[DroneOrientation.getSize()];
    }
    public void setxHome(final Integer x){
        xHome = x;
    }

    public void setyHome(final Integer y){
        yHome = y;
    }

    public Integer getxHome(){
        return xHome;
    }

    public Integer getyHome(){
        return yHome;
    }

    public void setScanResult(Integer id, RegionCode r){
        results[id] = r;
    }

    public RegionCode getScanResult(Integer id){
        return results[id];
    }

    public String getScanString(){
        String nextSquare, resultString = "";
        if(results.length == DroneOrientation.getSize()){
            for(int k = 0; k < DroneOrientation.getSize();k++){
                switch (results[k]) {
                    case EMPTY_CODE:
                        nextSquare = "empty";
                        break;
                    case STARS_CODE:
                        nextSquare = "stars";
                        break;
                    case SUN_CODE:
                        nextSquare = "sun";
                        break;
                    case BARRIER_CODE:
                        nextSquare = "barrier";
                        break;
                    case DRONE_CODE:
                        nextSquare = "drone";
                        break;
                    default:
                        nextSquare = "unknown";
                        break;
                }
                if (resultString.isEmpty()) {
                    resultString = nextSquare;
                } else {
                    resultString = resultString + "," + nextSquare;
                }
            }
        } else {
            resultString = "Invalid_Scan";
        }
        return resultString;
    }
}