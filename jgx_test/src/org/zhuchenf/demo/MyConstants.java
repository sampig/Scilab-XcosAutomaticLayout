package org.zhuchenf.demo;

public class MyConstants {

    public final static int POSITION_RIGHT = 1;
    public final static int POSITION_DOWN = 2;
    public final static int POSITION_LEFT = 3;
    public final static int POSITION_UP = 4;

    public enum MyOrientation {
        EAST, SOUTH, WEST, NORTH;

        public static boolean isOpposite(MyOrientation o1, MyOrientation o2) {
            if (o1 == EAST && o2 == WEST) {
                return true;
            }
            if (o1 == SOUTH && o2 == NORTH) {
                return true;
            }
            if (o1 == WEST && o2 == EAST) {
                return true;
            }
            if (o1 == NORTH && o2 == SOUTH) {
                return true;
            }
            return false;
        }

        public MyOrientation getOppositeOrientation() {
            switch (this) {
            case EAST:
                return WEST;
            case WEST:
                return EAST;
            case SOUTH:
                return NORTH;
            case NORTH:
                return SOUTH;
            }
            return WEST;
        }

        public MyOrientation[] getDifferentOrientation() {
            MyOrientation[] o = new MyOrientation[3];
            switch (this) {
            case EAST:
                o = new MyOrientation[] { SOUTH, WEST, NORTH };
                break;
            case WEST:
                o = new MyOrientation[] { EAST, SOUTH, NORTH };
                break;
            case SOUTH:
                o = new MyOrientation[] { EAST, WEST, NORTH };
                break;
            case NORTH:
                o = new MyOrientation[] { EAST, SOUTH, WEST };
                break;
            }
            return o;
        }

        public MyOrientation[] changeOrientation() {
            MyOrientation[] o = new MyOrientation[3];
            switch (this) {
            case EAST:
                o = new MyOrientation[] { SOUTH, NORTH };
                break;
            case WEST:
                o = new MyOrientation[] { SOUTH, NORTH };
                break;
            case SOUTH:
                o = new MyOrientation[] { EAST, WEST };
                break;
            case NORTH:
                o = new MyOrientation[] { EAST, WEST };
                break;
            }
            return o;
        }
    };

    /**
     * 
     */
    public final static double SLOPE_ERROR = 5;
    
    /**
     * 
     */
    public final static double BEAUTY_DISTANCE = 15;

}
