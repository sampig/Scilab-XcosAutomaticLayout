/*
 * Scilab ( http://www.scilab.org/ ) - This file is part of Scilab
 * Copyright (C) 2016 - Chenfeng ZHU
 *
 * This file must be used under the terms of the CeCILL.
 * This source file is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at
 * http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.txt
 *
 */
package org.scilab.modules.xcos.utils;

import java.util.ArrayList;
import java.util.List;

import org.scilab.modules.graph.ScilabGraph;
import org.scilab.modules.xcos.block.SplitBlock;
import org.scilab.modules.xcos.graph.XcosDiagram;
import org.scilab.modules.xcos.graph.swing.handler.SelectionCellsHandler;
import org.scilab.modules.xcos.link.BasicLink;
import org.scilab.modules.xcos.port.BasicPort;
import org.scilab.modules.xcos.port.Orientation;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxPoint;

/**
 * Provide methods to set the new position for SplitBlock.
 */
public abstract class BlockAutoPositionUtils {

    /**
     * Change the position of the SplitBlocks including their links.
     *
     * @param graph
     * @param cells
     */
    public static void changeSplitBlocksPosition(XcosDiagram graph, Object[] cells) {
        Object[] all = graph.getChildCells(graph.getDefaultParent());
        for (Object o : cells) {
            if (o instanceof SplitBlock) {
                SplitBlock cell = (SplitBlock) o;
                changeSplitBlockPosition(cell, all, graph);
            }
        }
    }

    /**
     * Change the position of the SplitBlock including its links.
     *
     * @param splitblock
     * @param all
     * @param graph
     */
    protected static void changeSplitBlockPosition(SplitBlock splitblock, Object[] all, XcosDiagram graph) {
        BasicPort out1 = splitblock.getOut1();
        BasicPort out2 = splitblock.getOut2();
        mxICell sourceCell = getSplitSource(splitblock);
        mxICell targetCell1 = getSplitTarget(splitblock, out1);
        mxICell targetCell2 = getSplitTarget(splitblock, out2);
        List<mxPoint> list1 = getRoute(sourceCell, targetCell1, all, graph);
        List<mxPoint> list2 = getRoute(sourceCell, targetCell2, all, graph);
        mxPoint point = getSplitPoint(list1, list2);
        updatePortOrientation(splitblock, list1, list2, point);
        mxGeometry splitGeo = (mxGeometry) graph.getModel().getGeometry(splitblock).clone();
        splitGeo.setX(point.getX() - splitGeo.getWidth() / 2);
        splitGeo.setY(point.getY() - splitGeo.getHeight() / 2);
        graph.getModel().setGeometry(splitblock, splitGeo);
        updateSplitLink(splitblock, all, graph);
    }

    /**
     * Get the source of a SplitBlock.
     *
     * @param splitblock
     * @return
     */
    private static mxICell getSplitSource(SplitBlock splitblock) {
        mxICell cell = null;
        BasicPort in = splitblock.getIn();
        mxICell edge = in.getEdgeAt(0);
        if (edge != null && edge instanceof mxCell) {
            cell = ((mxCell) edge).getSource();
        }
        return cell;
    }

    /**
     * Get the target of a SplitBlock according to its Output.
     *
     * @param split
     * @param out
     * @return
     */
    private static mxICell getSplitTarget(SplitBlock split, BasicPort out) {
        mxICell cell = null;
        mxICell edge = out.getEdgeAt(0);
        if (edge != null && edge instanceof mxCell) {
            cell = ((mxCell) edge).getTarget();
        }
        return cell;
    }

    /**
     * Get the route for the source and the target ignoring the SplitBlock.
     *
     * @param source
     * @param target
     * @param all
     * @param graph
     * @return all the turning points in the route including the start and end points
     */
    private static List<mxPoint> getRoute(mxICell source, mxICell target, Object[] all, XcosDiagram graph) {
        XcosRoute util = new XcosRoute();
        Object[] allOtherCells = util.getAllOtherCells(all, source, target, source.getEdgeAt(0),
                target.getEdgeAt(0));
        List<mxPoint> list = new ArrayList<mxPoint>(0);
        if (source != null) {
            list.add(getPortPosition(source));
        }
        boolean flag = util.computeRoute(source, target, allOtherCells, graph);
        if (flag) {
            list.addAll(util.getNonRedundantPoints());
        }
        if (target != null) {
            list.add(getPortPosition(target));
        }
        return list;
    }

    /**
     * Get the position of a port.
     *
     * @param port
     * @return
     */
    private static mxPoint getPortPosition(mxICell port) {
        mxPoint point = new mxPoint();
        if (port == null) {
            return null;
        }
        if (port.getParent() instanceof SplitBlock) {
            SplitBlock cell = (SplitBlock) port.getParent();
            point.setX(cell.getGeometry().getCenterX());
            point.setY(cell.getGeometry().getCenterY());
        } else {
            mxGeometry portGeo = port.getGeometry();
            double portX = portGeo.getX();
            double portY = portGeo.getY();
            double portW = portGeo.getWidth();
            double portH = portGeo.getHeight();
            mxICell parent = port.getParent();
            mxGeometry parentGeo = parent.getGeometry();
            double blockX = parentGeo.getX();
            double blockY = parentGeo.getY();
            double blockW = parentGeo.getWidth();
            double blockH = parentGeo.getHeight();
            if (portGeo.isRelative()) {
                portX *= blockW;
                portY *= blockH;
            }
            point.setX(blockX + portX + portW / 2);
            point.setY(blockY + portY + portH / 2);
        }
        return point;
    }

    /**
     * Get the split point for the two routes.
     *
     * @param list1
     * @param list2
     * @return
     */
    private static mxPoint getSplitPoint(List<mxPoint> list1, List<mxPoint> list2) {
        mxPoint point = null;
        int num = Math.min(list1.size(), list2.size());
        if (num <= 1 || !list1.get(0).equals(list2.get(0))) {
            return null;
        }
        // check the last intersection of two links
        int iList1 = 1;
        int iList2 = 1;
        for (int i = iList1; i < list1.size(); i++) {
            for (int j = iList2; j < list2.size(); j++) {
                mxPoint p1 = list1.get(i - 1);
                mxPoint p2 = list1.get(i);
                mxPoint p3 = list2.get(j - 1);
                mxPoint p4 = list2.get(j);
                double x1 = p1.getX();
                double y1 = p1.getY();
                double x2 = p2.getX();
                double y2 = p2.getY();
                double x3 = p3.getX();
                double y3 = p3.getY();
                double x4 = p4.getX();
                double y4 = p4.getY();
                mxPoint p0 = XcosRouteUtils.getIntersection(x1, y1, x2, y2, x3, y3, x4, y4);
                if (p0 != null) {
                    iList1 = i;
                    iList2 = j;
                    point = (mxPoint) p0.clone();
                }
            }
        }
        return point;
    }

    /**
     * Update port orientation.
     *
     * @param split
     * @param list1
     * @param list2
     * @param splitPoint
     */
    private static void updatePortOrientation(SplitBlock split, List<mxPoint> list1, List<mxPoint> list2, mxPoint splitPoint) {
        BasicPort inport = split.getIn();
        BasicPort outport1 = split.getOut1();
        BasicPort outport2 = split.getOut2();
        Orientation orientationIn = getInportOrientation(list1, list2, splitPoint);
        if(orientationIn!=null) {
            inport.setOrientation(orientationIn);
        }
        Orientation orientationOut1 = getOutportOrientation(list1, splitPoint);
        if (orientationOut1 != null) {
            outport1.setOrientation(orientationOut1);
        }
        Orientation orientationOut2 = getOutportOrientation(list2, splitPoint);
        if (orientationOut2 != null) {
            outport2.setOrientation(orientationOut2);
        }
    }

    /**
     * Get the orientation for the Input Port of a Split Block.
     *
     * @param list1 the first optimal route including the start Port and the end Port
     * @param list2 the second optimal route including the start Port and the end Port
     * @param splitPoint the new position for the Split Block
     * @return
     */
    private static Orientation getInportOrientation(List<mxPoint> list1, List<mxPoint> list2, mxPoint splitPoint) {
        int num1 = list1.size();
        if (num1 <= 1) {
            return null;
        }
        int num2 = list2.size();
        if (num2 <= 1) {
            return null;
        }
        double x = splitPoint.getX();
        double y = splitPoint.getY();
        int turning1 = 1;
        int turning2 = 1;
        Orientation orientation1 = null;
        Orientation orientation2 = null;
        for (int i = 1; i < num1; i++) {
            mxPoint p0 = list1.get(i - 1);
            mxPoint p1 = list1.get(i);
            double x0 = p0.getX();
            double y0 = p0.getY();
            double x1 = p1.getX();
            double y1 = p1.getY();
            // if the point is in this segment,
            if (XcosRouteUtils.pointInLineSegment(x, y, x0, y0, x1, y1)) {
                turning1 = i;
                if (x1 == x0 && y1 > y0) { // segment: south
                    orientation1 = Orientation.NORTH;
                } else if (x1 == x0 && y1 < y0) { // segment: north
                    orientation1 = Orientation.SOUTH;
                } else if (y1 == y0 && x1 > x0) { // segment: east
                    orientation1 = Orientation.WEST;
                } else if (y1 == y0 && x1 < x0) { // segment: west
                    orientation1 = Orientation.EAST;
                }
                break;
            }
        }
        for (int i = 1; i < num2; i++) {
            mxPoint p0 = list2.get(i - 1);
            mxPoint p1 = list2.get(i);
            double x0 = p0.getX();
            double y0 = p0.getY();
            double x1 = p1.getX();
            double y1 = p1.getY();
            // if the point is in this segment,
            if (XcosRouteUtils.pointInLineSegment(x, y, x0, y0, x1, y1)) {
                turning2 = i;
                if (x1 == x0 && y1 > y0) { // segment: south
                    orientation2 = Orientation.NORTH;
                } else if (x1 == x0 && y1 < y0) { // segment: north
                    orientation2 = Orientation.SOUTH;
                } else if (y1 == y0 && x1 > x0) { // segment: east
                    orientation2 = Orientation.WEST;
                } else if (y1 == y0 && x1 < x0) { // segment: west
                    orientation2 = Orientation.EAST;
                }
                break;
            }
        }
        if (turning1 <= turning2) { // if list1 is better
            return orientation1;
        } else { // if list2 is better
            return orientation2;
        }
    }

    /**
     * Get the orientation for the one Output Port of a Split Block according to its optimal route.
     *
     * @param list the second optimal route including the start Port and the end Port
     * @param splitPoint the new position for the Split Block
     * @return
     */
    private static Orientation getOutportOrientation(List<mxPoint> list, mxPoint splitPoint) {
        double x = splitPoint.getX();
        double y = splitPoint.getY();
        int num = list.size();
        if (num <= 1) {
            return null;
        }
        for (int i = 1; i < num; i++) {
            mxPoint p0 = list.get(i - 1);
            mxPoint p1 = list.get(i);
            double x0 = p0.getX();
            double y0 = p0.getY();
            double x1 = p1.getX();
            double y1 = p1.getY();
            // if the point is in this segment,
            if (XcosRouteUtils.pointInLineSegment(x, y, x0, y0, x1, y1)) {
                // if the point is in the next turning point,
                if (x == x1 && y == y1 && i + 1 != num) {
                    mxPoint p2 = list.get(i + 1);
                    double x2 = p2.getX();
                    double y2 = p2.getY();
                    if (x == x2 && y < y2) { // segment: south
                        return Orientation.SOUTH;
                    } else if (x == x2 && y > y2) { // segment: north
                        return Orientation.NORTH;
                    } else if (y == y2 && x < x2) { // segment: east
                        return Orientation.EAST;
                    } else if (y == y2 && x > x2) { // segment: west
                        return Orientation.WEST;
                    }
                }
                if (x1 == x0 && y1 > y0) { // segment: south
                    return Orientation.SOUTH;
                } else if (x1 == x0 && y1 < y0) { // segment: north
                    return Orientation.NORTH;
                } else if (y1 == y0 && x1 > x0) { // segment: east
                    return Orientation.EAST;
                } else if (y1 == y0 && x1 < x0) { // segment: west
                    return Orientation.WEST;
                }
                break;
            }
        }
        return null;
    }

    /**
     * Update the links of a SplitBlock.
     *
     * @param split
     * @param all
     * @param graph
     */
    private static void updateSplitLink(SplitBlock split, Object[] all, XcosDiagram graph) {
        XcosRoute route = new XcosRoute();
        BasicLink link = (BasicLink) split.getIn().getEdgeAt(0);
        boolean lockPort = true;
        reset(graph, link);
        route.updateRoute(link, all, graph, lockPort);
        link = (BasicLink) split.getOut1().getEdgeAt(0);
        reset(graph, link);
        route.updateRoute(link, all, graph, lockPort);
        link = (BasicLink) split.getOut2().getEdgeAt(0);
        reset(graph, link);
        route.updateRoute(link, all, graph, lockPort);
    }

    /**
     * Reset the link.
     *
     * @param graph
     * @param edge
     */
    private static void reset(final ScilabGraph graph, final Object edge) {
        final SelectionCellsHandler selectionCellsHandler = (SelectionCellsHandler) graph.getAsComponent()
                .getSelectionCellsHandler();
        graph.resetEdge(edge);
        selectionCellsHandler.clearCellHandler(edge);
    }

}
