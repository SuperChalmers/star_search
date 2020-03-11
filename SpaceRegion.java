/*
David Hansen
dhansen33@gatech.edu
*/

import java.util.HashMap;

public class SpaceRegion {
    
    private static final int MAX_WIDTH = 20;
    private static final int MAX_HEIGHT = 15;
  
    private static final int DEFAULT_WIDTH = MAX_WIDTH;
    private static final int DEFAULT_HEIGHT = MAX_HEIGHT;
    private final HashMap<DroneOrientation, Integer> xMAP;
    private final HashMap<DroneOrientation, Integer> yMAP;

    private Integer regionHeight;
    private Integer regionWidth;
    private final RegionCode[][] regionInfo;
    private Integer leftToExplore;
    private Integer numSuns;
    
    private void initRegion(final Integer x, final Integer y){
        int i, j;
        for (i = 0; i < x; i++) {
            for (j = 0; j < y; j++) {
                regionInfo[i][j] = RegionCode.STARS_CODE; // initialize everywhere to stars when you first create the zone
                                                          
            }
        }

    }

    private void setLeftToExplore(){
        leftToExplore = (regionWidth * regionHeight) - numSuns;
    }

    public SpaceRegion() {
        regionHeight = DEFAULT_HEIGHT;
        regionWidth = DEFAULT_WIDTH;
        regionInfo = new RegionCode[regionWidth][regionHeight];
        initRegion(regionWidth, regionHeight);
        xMAP = DroneOrientation.getXMAP();
        yMAP = DroneOrientation.getYMAP();
        numSuns = 0;
        setLeftToExplore();
     }

    public SpaceRegion(final Integer width, final Integer height) {
        regionHeight = height;
        regionWidth = width;
        regionInfo = new RegionCode[regionWidth][regionHeight];
        initRegion(regionWidth, regionHeight);
        xMAP = DroneOrientation.getXMAP();
        yMAP = DroneOrientation.getYMAP();
        numSuns = 0;
        setLeftToExplore();
    }

    public Integer getRegionHeight() {
        return regionHeight;
    }

    public Integer getRegionWidth() {
        return regionWidth;
    }

    public RegionCode[][] getRegionInfo() {
        return regionInfo;
    }

    public Integer getSpaceLeftToExplore(){
        return leftToExplore;
    }

    public void setRegionHeight(final Integer h) {
        regionHeight = h;
        setLeftToExplore();
    }

    public void setRegionWidth(final Integer w) {
        regionWidth = w;
        setLeftToExplore();
    }

    public void exploreRegion(final Integer x, final Integer y) {
        if(regionInfo[x][y] == RegionCode.STARS_CODE) {
            leftToExplore--; //one less space to explore
        }
        regionInfo[x][y] = RegionCode.EMPTY_CODE; // Set to empty if Explored
    }

    public RegionCode getRegionState(final Integer x, final Integer y) {
        if(x < 0 || y < 0 || x >= regionWidth || y >= regionHeight){
            return RegionCode.BARRIER_CODE;
        }
        return regionInfo[x][y];
    }

    public void setSunLocation(final Integer x, final Integer y) {
        regionInfo[x][y] = RegionCode.SUN_CODE; //set sun location
        numSuns++;
        leftToExplore--; //don't need to explore suns
    }

    public Integer getRegionSize(){
        return regionWidth * regionHeight;
    }

    public boolean isStars(final Integer x, final Integer y){
        if(x < 0 || y < 0 || x >= regionWidth || y >= regionHeight){
            return false;
        }
        if(regionInfo[x][y] == RegionCode.STARS_CODE){
            return true;
        }
        return false;
    }

    public boolean isSafeMove(final Integer x, final Integer y){
        boolean returnCode = false;
        if(x < 0 || y < 0 || x >= regionWidth || y >= regionHeight){
            return returnCode;
        }
        switch (regionInfo[x][y]) {
            case STARS_CODE:
            case EMPTY_CODE:
                returnCode = true;
                break;
            default:
                returnCode = false;
                break;
        }
        return returnCode;
    }

    public void readScan(final ScanResult sr){

        for(int k = 0; k < DroneOrientation.getSize(); k++){
            final DroneOrientation lookThisWay = DroneOrientation.chosenOrientation(k);
            final int offsetX = xMAP.get(lookThisWay);
            final int offsetY = yMAP.get(lookThisWay);
    
            final int checkX = sr.getxHome() + offsetX; //xhome is x center of the scan
            final int checkY = sr.getyHome() + offsetY; //yhome is y center of the scan
            if(sr.getScanResult(k)==RegionCode.BARRIER_CODE){
                //don't do anything, you can't update outside of the space
            } else{
                regionInfo[checkX][checkY] = sr.getScanResult(k);
            }
        }

    }

    public void newKnownSpace(){
        int i, j;
        for (i = 0; i < regionWidth; i++) {
            for (j = 0; j < regionHeight; j++) {
                regionInfo[i][j] = RegionCode.UNKNOWN_CODE; // initialize everywhere to unknown when you first create known space
                                                          
            }
        }

    }
    public void setDroneLocation(final Integer x, final Integer y){
        regionInfo[x][y] = RegionCode.DRONE_CODE;
    }

}


