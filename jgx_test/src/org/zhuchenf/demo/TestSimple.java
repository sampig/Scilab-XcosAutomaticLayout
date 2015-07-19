package org.zhuchenf.demo;

import org.zhuchenf.demo.utils.RouteUtils;

import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxUtils;

public class TestSimple {

    public static void main(String... strings) {
        mxPoint point = mxUtils.intersection(10, 10, 10, 100, 5, 50, 100, 50);
        System.out.println(point);
        point = mxUtils.intersection(10, 10, 10, 100, 100, 50, 5, 50);
        System.out.println(point);
        point = mxUtils.intersection(10, 10, 10, 100, 10, 50, 10, 70);
        System.out.println(point);
        point = mxUtils.intersection(10, 10, 10, 100, 100, 50, 15, 50);
        System.out.println(point);
        System.out.println(RouteUtils.pointInLineSegment(10, 10, 10, 100, 10, 50));
        System.out.println(RouteUtils.pointInLineSegment(10, 70, 10, 100, 10, 50));
        System.out.println(RouteUtils.pointInLineSegment(10, 50, 50, 50, 100, 50));
        System.out.println(RouteUtils.pointInLineSegment(70, 50, 50, 50, 100, 50));
        System.out.println(RouteUtils.pointInLineSegment(50, 50, 50, 50, 100, 50));
    }

}
