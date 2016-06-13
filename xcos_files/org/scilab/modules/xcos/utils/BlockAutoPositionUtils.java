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
        for (int i = 1; i < num; i++) {
            mxPoint p0 = list1.get(i - 1);
            mxPoint p1 = list1.get(i);
            mxPoint p2 = list2.get(i);
            if (p1.equals(p2)) {
                continue;
            }
            double x0 = p0.getX();
            double y0 = p0.getY();
            double x1 = p1.getX();
            double y1 = p1.getY();
            double x2 = p2.getX();
            double y2 = p2.getY();
            if (XcosRouteUtils.pointInLineSegment(x1, y1, x0, y0, x2, y2)) {
                // p1 in the line segment (p0,p2)
                point = (mxPoint) p1.clone();
                break;
            } else if (XcosRouteUtils.pointInLineSegment(x2, y2, x0, y0, x1, y1)) {
                // p2 in the line segment (p0,p1)
                point = (mxPoint) p2.clone();
                break;
            } else {
                point = (mxPoint) p0.clone();
                break;
            }
        }
        return point;
    }

    private static void updatePortOrientation(SplitBlock split, List<mxPoint> list1, List<mxPoint> list2, mxPoint splitPoint) {
        BasicPort inport = split.getIn();
        BasicPort outport1 = split.getOut1();
        BasicPort outport2 = split.getOut2();
        double x = splitPoint.getX();
        double y = splitPoint.getY();
        int num1 = list1.size();
        if (num1 <= 1) {
            return;
        }
        for (int i = 1; i < num1; i++) {
            mxPoint p0 = list1.get(i - 1);
            mxPoint p1 = list1.get(i);
            double x0 = p0.getX();
            double y0 = p0.getY();
            double x1 = p1.getX();
            double y1 = p1.getY();
            // if the point is in this segment,
            if (XcosRouteUtils.pointInLineSegment(x, y, x0, y0, x1, y1)) {
                if (x1 == x0 && y1 > y0) { // segment: south
                    inport.setOrientation(Orientation.NORTH);
                    outport1.setOrientation(Orientation.SOUTH);
                } else if (x1 == x0 && y1 < y0) { // segment: north
                    inport.setOrientation(Orientation.SOUTH);
                    outport1.setOrientation(Orientation.NORTH);
                } else if (y1 == y0 && x1 > x0) { // segment: east
                    inport.setOrientation(Orientation.WEST);
                    outport1.setOrientation(Orientation.EAST);
                } else if (y1 == y0 && x1 < x0) { // segment: west
                    inport.setOrientation(Orientation.EAST);
                    outport1.setOrientation(Orientation.WEST);
                }
                // if the point is in the next turning point,
                if (x == x1 && y == y1 && i+1 != num1) {
                    mxPoint p2 = list1.get(i+1);
                    double x2 = p2.getX();
                    double y2 = p2.getY();
                    if (x == x2 && y < y2) { // segment: south
                        outport1.setOrientation(Orientation.SOUTH);
                    } else if (x == x2 && y > y2) { // segment: north
                        outport1.setOrientation(Orientation.NORTH);
                    } else if (y == y2 && x < x2) { // segment: east
                        outport1.setOrientation(Orientation.EAST);
                    } else if (y == y2 && x > x2) { // segment: west
                        outport1.setOrientation(Orientation.WEST);
                    }
                }
                break;
            }
        }
        int num2 = list2.size();
        if (num2 <= 1) {
            return;
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
                if (x1 == x0 && y1 > y0) { // segment: south
                    outport2.setOrientation(Orientation.SOUTH);
                } else if (x1 == x0 && y1 < y0) { // segment: north
                    outport2.setOrientation(Orientation.NORTH);
                } else if (y1 == y0 && x1 > x0) { // segment: east
                    outport2.setOrientation(Orientation.EAST);
                } else if (y1 == y0 && x1 < x0) { // segment: west
                    outport2.setOrientation(Orientation.WEST);
                }
                // if the point is in the next turning point,
                if (x == x1 && y == y1 && i+1 != num2) {
                    mxPoint p2 = list2.get(i+1);
                    double x2 = p2.getX();
                    double y2 = p2.getY();
                    if (x == x2 && y < y2) { // segment: south
                        outport2.setOrientation(Orientation.SOUTH);
                    } else if (x == x2 && y > y2) { // segment: north
                        outport2.setOrientation(Orientation.NORTH);
                    } else if (y == y2 && x < x2) { // segment: east
                        outport2.setOrientation(Orientation.EAST);
                    } else if (y == y2 && x > x2) { // segment: west
                        outport2.setOrientation(Orientation.WEST);
                    }
                }
                break;
            }
        }
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
