package org.zhuchenf.demo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class RouteTest {
    
    public List<Point> list = new ArrayList<>(0);
    public int pathValue() {
        int value =100; 
        return value;
    }
    
    public static void main(String...strings) {
        RouteTest rt = new RouteTest();
        Point p1 = new Point(0,0);
        Point p2 = new Point(1,0);
        System.out.println(rt.pathValue());
    }

}
