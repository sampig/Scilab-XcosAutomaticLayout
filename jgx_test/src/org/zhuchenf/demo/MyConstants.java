package org.zhuchenf.demo;

public class MyConstants {

    public final static int POSITION_RIGHT = 1;
    public final static int POSITION_DOWN = 2;
    public final static int POSITION_LEFT = 3;
    public final static int POSITION_UP = 4;

    public enum MyOrientation {
        RIGHT, DOWN, LEFT, UP;

        public static boolean isOpposite(MyOrientation o1, MyOrientation o2) {
            if (o1 == RIGHT && o2 == LEFT) {
                return true;
            }
            if (o1 == DOWN && o2 == UP) {
                return true;
            }
            if (o1 == LEFT && o2 == RIGHT) {
                return true;
            }
            if (o1 == UP && o2 == DOWN) {
                return true;
            }
            return false;
        }

        public MyOrientation getOppositeOrientation() {
            switch (this) {
            case RIGHT:
                return LEFT;
            case LEFT:
                return RIGHT;
            case DOWN:
                return UP;
            case UP:
                return DOWN;
            }
            return LEFT;
        }

        public MyOrientation[] getDifferentOrientation() {
            MyOrientation[] o = new MyOrientation[3];
            switch (this) {
            case RIGHT:
                o = new MyOrientation[] { DOWN, LEFT, UP };
                break;
            case LEFT:
                o = new MyOrientation[] { RIGHT, DOWN, UP };
                break;
            case DOWN:
                o = new MyOrientation[] { RIGHT, LEFT, UP };
                break;
            case UP:
                o = new MyOrientation[] { RIGHT, DOWN, LEFT };
                break;
            }
            return o;
        }

        public MyOrientation[] changeOrientation() {
            MyOrientation[] o = new MyOrientation[3];
            switch (this) {
            case RIGHT:
                o = new MyOrientation[] { DOWN, UP };
                break;
            case LEFT:
                o = new MyOrientation[] { DOWN, UP };
                break;
            case DOWN:
                o = new MyOrientation[] { RIGHT, LEFT };
                break;
            case UP:
                o = new MyOrientation[] { RIGHT, LEFT };
                break;
            }
            return o;
        }
    };

    public final static double SLOPE_ERROR = 15;
    
    public final static double BEAUTY_DISTANCE = 15;

}
