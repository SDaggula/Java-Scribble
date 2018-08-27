package com.scribble.patterns;

import com.scribble.patterns.NBestList;
import com.scribble.patterns.NDollarRecognizer;
import com.scribble.patterns.PointR;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Sreelakshmi Daggula on 6/30/2016.
 This class is the entry point for passing actual touch points on touch down, move and up to detect the gesture.
 This class also processes additional gestures based on direction and angle.
 */
public class ScribbleProcessor {
    private int touch_start_x = -1, touch_start_y = -1;
    private int touch_init_x = -1, touch_init_y = -1;

    private boolean touch_west = false;
    private boolean touch_north = false;
    private boolean touch_east = false;
    private boolean touch_south = false;
    private boolean touch_northwest = false;
    private boolean touch_southeast = false;
    private boolean touch_southwest = false;
    private boolean touch_northeast = false;


    private int eastCount = 0;
    private int westCount = 0;
    private int northCount = 0;
    private int southCount = 0;
    private int northEastCount = 0;
    private int northWestCount = 0;
    private int southEastCount = 0;
    private int southWestCount = 0;


    private ArrayList<Integer> directionList;
    private static int northEast = 4, southEast = 7, east = 0, northWest = 5, southWest = 6, west = 2, north = 1, south = 3;

    private int numberOfStrokes = 0;
    private int digit = -1;
    private int prevDigit = -1;
    private int MAX_DISTANCE_THRESHOLD = 500;
    private int pointerCount = 0;


    private Vector<PointR> allPoints = new Vector<>();

    public ScribbleProcessor(){
        directionList = new ArrayList<>();
    }

    public void onActionDown(int x, int y, int pointerCount){
        this.pointerCount = pointerCount;
        if(numberOfStrokes == 1 && digit != -1){
            prevDigit = digit;
        }
        allPoints.add(new PointR(x, y));
        touch_start_x = touch_init_x = x;
        touch_start_y = touch_init_y = y;
        resetConstants();
        directionList.clear();
    }

    private void resetConstants(){
        touch_west = false;
        touch_north = false;
        touch_east = false;
        touch_south = false;
        touch_northwest = false;
        touch_southeast = false;
        touch_southwest = false;
        touch_northeast = false;
        eastCount = 0;
        westCount = 0;
        northCount = 0;
        southCount = 0;
        northEastCount = 0;
        northWestCount = 0;
        southEastCount = 0;
        southWestCount = 0;
    }

    public void onActionUp(int x, int y, int pointerCount){
        allPoints.add(new PointR(x, y));
        numberOfStrokes++;
        this.pointerCount = pointerCount;

        float dx = (x - touch_init_x);
        float dy = (y - touch_init_y);
        int length = (int)Math.sqrt(dx * dx + dy * dy);
        int angle = (int) (Math.toDegrees(Math.atan2(dy, dx)));
        int threshold_length = 25;
        int size = directionList.size();
        if(size>0){
            if(size == 1){
                calculateDigit(directionList, angle);
            }else{
                calculateDigit(directionList, angle);
            }
        }else {
            if(length < threshold_length)
                setDigit(1);
            else
                calculateDigit(directionList, angle);
        }
        directionList.clear();
        pointerCount = 0;
    }


    public List<String> onTouchUp(int numStrokes, int pathLength){
        List<String> data = new ArrayList<>();
        if(pathLength >= 120  && numberOfStrokes == 2 && ((prevDigit == 5 && digit == 9 ) ||(prevDigit == 5 && digit == 13 )
                ||(prevDigit == 13 && digit == 5) ||(prevDigit == 9 && digit == 5 ))){
            data.add("11");
            data.add("100");//confidence
            resetToDefaults();
            return data;
        }else if (pathLength >= 120  && numberOfStrokes == 1 && digit == 13){
            data.add("10");
            data.add("100");//confidence
            resetToDefaults();
            return data;
        }else if(pathLength >= 120){
            data = evaluateResultData(numStrokes);
            resetToDefaults();
            return data;
        }else{
            data.add("-1");
            data.add("100");//confidence
            resetToDefaults();
            return data;
        }
    }

    private void resetToDefaults(){
        numberOfStrokes = 0;
        allPoints.clear();
        digit = -1;
        prevDigit = -1;
        pointerCount = 0;
    }

    private List<String> evaluateResultData(int numStrokes){
        if (numStrokes > 0 && pointerCount == 1) {
            try {
                if(allPoints.size()>5) {
                    NBestList result1 = NDollarRecognizer.getInstance().Recognize2(allPoints, numStrokes);
                    NBestList result = NDollarRecognizer.getInstance().Recognize(allPoints, numStrokes);
                    List<String> resultTxt = new ArrayList<>();
                    if (result1.getScore(0) != -1) {
                        int confidence = (int)(Utils.round(result1.getScore(0), 2) * 100);
                        System.out.println("TRaining data = "+result1.getName(0)+" confidence = "+confidence);

                        if(confidence<90){
                            if (result.getScore(0) != -1) {
                                String data = result.getName(0);
                                resultTxt.add(data);

                                resultTxt.add("" + result.getScore(0));
                                System.out.println("evaluate data with samples Training failed = " + resultTxt.toString());

                            }
                        }else{
                            String data = result1.getName(0);
                            resultTxt.add(data);
                            resultTxt.add("" + result1.getScore(0));
                            System.out.println("evaluate data with training = "+resultTxt.toString());
                        }
                    }else{
                        if (result.getScore(0) != -1) {
                            String data = result.getName(0);
                            resultTxt.add(data);

                            resultTxt.add("" + result.getScore(0));
                        }
                    }

                    return resultTxt;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public void onActionMove(int x, int y, int pointerCount){
    
		this.pointerCount = pointerCount;
        int COUNT_THRESHOLD = 0;
        float threshold = 1.0f;
        allPoints.add(new PointR(x, y));

        int leftRightDiff =  (y - touch_start_y);
        int upDownDiff = (x - touch_start_x);
        if(pointerCount == 1) {
            if (leftRightDiff != 0 && upDownDiff != 0) {
                float ratioleftRightDiff = upDownDiff / leftRightDiff;
                float ratioupDownDiff = leftRightDiff / upDownDiff;
                int dist = (int) Math.sqrt((upDownDiff * upDownDiff) + (leftRightDiff * leftRightDiff));
                if (ratioupDownDiff == 0 && Math.abs(ratioleftRightDiff) > threshold && upDownDiff > 20 && Math.abs(leftRightDiff) < 50) {
                    if (!touch_east) {
                        if ((dist > MAX_DISTANCE_THRESHOLD && eastCount == 0) || eastCount > COUNT_THRESHOLD) {
                            directionList.add(east);
                            eastCount = 0;
                            touch_east = true;
                        }
                    }
                    eastCount++;

                    touch_start_x = x;
                    touch_start_y = y;
                } else if (ratioupDownDiff == 0 && Math.abs(ratioleftRightDiff) > threshold && upDownDiff < -20 && Math.abs(leftRightDiff) < 50) {
                    if (!touch_west) {
                        if ((dist > MAX_DISTANCE_THRESHOLD && westCount == 0) || westCount > COUNT_THRESHOLD) {
                            directionList.add(west);
                            westCount = 0;
                            touch_west = true;
                        }
                    }

                    westCount++;
                    touch_start_x = x;
                    touch_start_y = y;
                } else if (ratioleftRightDiff == 0 && Math.abs(ratioupDownDiff) > threshold && leftRightDiff > 20 && Math.abs(upDownDiff) < 50) {
                    if (!touch_south) {
                        if ((dist > MAX_DISTANCE_THRESHOLD && southCount == 0) || southCount > COUNT_THRESHOLD) {
                            directionList.add(south);
                            southCount = 0;
                            touch_south = true;
                        }
                    }

                    southCount++;
                    touch_start_x = x;
                    touch_start_y = y;
                } else if (ratioleftRightDiff == 0 && Math.abs(ratioupDownDiff) > threshold && leftRightDiff < -20 && Math.abs(upDownDiff) < 50) {
                    if (!touch_north) {
                        if ((dist > MAX_DISTANCE_THRESHOLD && northCount == 0) || northCount > COUNT_THRESHOLD) {
                            directionList.add(north);
                            northCount = 0;
                            touch_north = true;
                        }
                    }

                    northCount++;
                    touch_start_x = x;
                    touch_start_y = y;
                } else if ((((ratioleftRightDiff == 0 || ratioleftRightDiff == 1) && ratioupDownDiff <= 0 && ratioupDownDiff >= -1 && leftRightDiff < -20 && upDownDiff > 20)
                        || ((ratioupDownDiff == 0 || ratioupDownDiff == 1) && (ratioleftRightDiff == 0 || ratioleftRightDiff == -1) && leftRightDiff < -20 && upDownDiff > 20))) {
                    if (!touch_northeast) {
                        if ((dist > MAX_DISTANCE_THRESHOLD && northEastCount == 0) || northEastCount > COUNT_THRESHOLD) {
                            directionList.add(northEast);
                            northEastCount = 0;
                            touch_northeast = true;
                        }
                    }

                    northEastCount++;
                    touch_start_x = x;
                    touch_start_y = y;
                } else if ((((ratioleftRightDiff == 0 || ratioleftRightDiff == 1) && ratioupDownDiff >= 0 && ratioupDownDiff <= 1 && leftRightDiff < -5 && upDownDiff < -5)
                        || ((ratioupDownDiff == 0 || ratioupDownDiff == 1) && (ratioleftRightDiff == 0 || ratioleftRightDiff == 1) && leftRightDiff < -5 && upDownDiff < -5))) {
                    if (!touch_northwest && ((dist > MAX_DISTANCE_THRESHOLD && northWestCount == 0) || northWestCount > COUNT_THRESHOLD)) {
                        directionList.add(northWest);
                        northWestCount = 0;
                        touch_northwest = true;
                    }
                    northWestCount++;

                    touch_start_x = x;
                    touch_start_y = y;
                } else if ((((ratioleftRightDiff == 0 || ratioleftRightDiff == 1 || ratioleftRightDiff == -1) && (ratioupDownDiff == 0 || ratioupDownDiff == 1)
                        && leftRightDiff > 20 && upDownDiff > 20)
                        || ((ratioupDownDiff == 0 || ratioupDownDiff == 1 || ratioupDownDiff == -1) && (ratioleftRightDiff == 0 || ratioleftRightDiff == -1)
                        && leftRightDiff < 20 && upDownDiff > 20))) {
                    if (!touch_southeast && ((dist > MAX_DISTANCE_THRESHOLD && southEastCount == 0) || southEastCount > COUNT_THRESHOLD)) {
                        directionList.add(southEast);
                        southEastCount = 0;
                        touch_southeast = true;
                    }

                    southEastCount++;
                    touch_start_x = x;
                    touch_start_y = y;
                } else if ((((ratioleftRightDiff == 0 || ratioleftRightDiff == 1 || ratioleftRightDiff == -1) && (ratioupDownDiff == 0 || ratioupDownDiff == -1)
                        && leftRightDiff > 20 && upDownDiff < -5)
                        || ((ratioupDownDiff == 0 || ratioupDownDiff == 1 || ratioupDownDiff == -1) && (ratioleftRightDiff == 0 || ratioleftRightDiff == -1)
                        && leftRightDiff < 80 && upDownDiff < -5))) {
                    if (!touch_southwest && ((dist > MAX_DISTANCE_THRESHOLD && southWestCount == 0) || southWestCount > COUNT_THRESHOLD)) {
                        directionList.add(southWest);
                        southWestCount = 0;
                        touch_southwest = true;
                    }
                    southWestCount++;

                    touch_start_x = x;
                    touch_start_y = y;
                }

            }
        }
    }

/*Calculate digits in Braille based on direction and angle
 * Angles are compared with +15 or -15 of actual angles to remove noise*/

    public void calculateDigit(ArrayList<Integer> dirList, int angle) {
        if (dirList.size() > 0) {
            System.out.println("directions = " + dirList.toString());

            if (dirList.size() == 1) {
                int direction = dirList.get(0);
                if (direction == northWest || direction == southEast) {
                    setDigit(5);
                } else if (direction == northEast || direction == southWest) {
                    setDigit(9);
                }else if (direction == west){
                    setDigit(13);
                }else{
                    setDigit(-1);
                }

            } else if (dirList.size() == 2) {
                if ((dirList.get(0)== southWest && dirList.get(1)==west)
                        || (dirList.get(0)== west && (dirList.contains(southWest)||dirList.contains(northWest)))) {
                    setDigit(13);// for BACK
                }else if ((dirList.get(0) == east && dirList.get(1) == southEast)||(dirList.get(0) == southEast && dirList.get(1) == east)
                        ||(dirList.get(0) == west && dirList.get(1) == northWest)||(dirList.get(0) == northWest && dirList.get(1) == west)) {
                    if (((angle >= 20 && angle < 75) || (angle > -165 && angle < -120))) {
                        setDigit(5);
                    }
                } else if ((dirList.get(0) == southWest && dirList.get(1) == west) || (dirList.get(0) == west && dirList.get(1) == southWest)
                        ||(dirList.get(0) == northEast && dirList.get(1) == east) || (dirList.get(0) == east && dirList.get(1) == northEast)) {
                    if (((angle >= -75 && angle < -20) || (angle > 120 && angle < 165))) {
                        setDigit(9);
                    }
                }else{
                    setDigit(-1);
                }
            }
        }else {
            if (((angle >= 20 && angle < 75) || (angle > -165 && angle < -120))) {
                setDigit(5);
            }else if (((angle >= -75 && angle < -20) || (angle > 120 && angle < 165))) {
                setDigit(9);
            }else if (angle <= -160 || angle > 160) {
                setDigit(13);
            }
        }

    }

    public void setDigit(int d){
        digit = d;
    }
}