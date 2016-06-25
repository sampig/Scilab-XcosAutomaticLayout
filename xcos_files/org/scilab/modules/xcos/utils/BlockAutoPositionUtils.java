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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.scilab.modules.graph.ScilabGraph;
import org.scilab.modules.gui.messagebox.ScilabModalDialog;
import org.scilab.modules.gui.messagebox.ScilabModalDialog.IconType;
import org.scilab.modules.xcos.XcosTab;
import org.scilab.modules.xcos.block.SplitBlock;
import org.scilab.modules.xcos.graph.XcosDiagram;
import org.scilab.modules.xcos.graph.swing.handler.SelectionCellsHandler;
import org.scilab.modules.xcos.link.BasicLink;
import org.scilab.modules.xcos.port.BasicPort;
import org.scilab.modules.xcos.port.Orientation;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxConstants;
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
        Object[] selectedCells = selectRootSplitBlock(graph, cells);
        for (Object o : selectedCells) {
            if (o instanceof SplitBlock) {
                SplitBlock cell = (SplitBlock) o;
                if (getSplitBlockNumber(cell) == 1) {
                    changeSplitBlockPosition(cell, all, graph);
                } else {
                    changeSplitBlockPositionMulti(cell, all, graph);
                }
            }
        }
    }

    /**
     * Only select the root Split Block.
     *
     * @param graph
     * @param cells
     * @return all the root Split Blocks
     */
    private static Object[] selectRootSplitBlock(XcosDiagram graph, Object[] cells) {
        List<Object> list = new ArrayList<>(0);
        for (Object o : cells) {
            if (o instanceof SplitBlock) {
                SplitBlock cell = getRootSplitBlock((SplitBlock) o);
                if (!list.contains(cell)) {
                    list.add(cell);
                }
            }
        }
        return list.toArray();
    }

    /**
     * Get the first(root) Split Block in the link.
     *
     * @param splitblock
     * @return the first Split Block
     */
    private static SplitBlock getRootSplitBlock(SplitBlock splitblock) {
        mxICell port = getSplitSourcePort(splitblock);
        while ((port != null) && (port.getParent() instanceof SplitBlock)) {
            port = getSplitSourcePort(((SplitBlock) port.getParent()));
        }
        mxICell edge = port.getEdgeAt(0);
        mxICell cell = ((mxCell) edge).getTarget();
        return ((SplitBlock) cell.getParent());
    }

    /**
     * Get the number of Split Blocks in the link where the Split Block is.
     *
     * @param splitblock
     * @return the number of Split Blocks
     */
    private static int getSplitBlockNumber(SplitBlock splitblock) {
        SplitBlock root = getRootSplitBlock(splitblock);
        return getSelfAndChildrenNumber(root);
    }

    /**
     * Get the number of split block including itself and its all children.
     *
     * @param splitblock
     * @return the number of split block
     */
    private static int getSelfAndChildrenNumber(SplitBlock splitblock) {
        int num1 = 0;
        int num2 = 0;
        BasicPort out1 = splitblock.getOut1();
        BasicPort out2 = splitblock.getOut2();
        mxICell targetPort1 = getSplitTargetPort(splitblock, out1);
        if (targetPort1.getParent() instanceof SplitBlock) {
            num1 = getSelfAndChildrenNumber((SplitBlock) targetPort1.getParent());
        }
        mxICell targetPort2 = getSplitTargetPort(splitblock, out2);
        if (targetPort2.getParent() instanceof SplitBlock) {
            num2 = getSelfAndChildrenNumber((SplitBlock) targetPort2.getParent());
        }
        return 1 + num1 + num2;
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
        mxICell sourcePort = getSplitSourcePort(splitblock);
        mxICell targetPort1 = getSplitTargetPort(splitblock, out1);
        mxICell targetPort2 = getSplitTargetPort(splitblock, out2);
        Object[] allObstacles = getObstacles(splitblock, all);
        List<mxPoint> list1 = getRoute(splitblock, sourcePort, targetPort1, allObstacles, graph);
        List<mxPoint> list2 = getRoute(splitblock, sourcePort, targetPort2, allObstacles, graph);
        adjustRoutes(list1, list2, allObstacles);
        mxPoint point = getSplitPoint(list1, list2);
        if (point == null) {
            ScilabModalDialog.show(XcosTab.get(graph), new String[] { XcosMessages.BASP_POSITION_NOT_FOUND },
                    XcosMessages.BLOCK_AUTO_POSITION_SPLIT_BLOCK, IconType.INFORMATION_ICON);
            return;
        }
        updatePortOrientation(splitblock, list1, list2, point);
        mxGeometry splitGeo = (mxGeometry) graph.getModel().getGeometry(splitblock).clone();
        splitGeo.setX(point.getX() - splitGeo.getWidth() / 2);
        splitGeo.setY(point.getY() - splitGeo.getHeight() / 2);
        graph.getModel().setGeometry(splitblock, splitGeo);
        updateSplitLink(splitblock, allObstacles, graph);
    }

    /**
     * Change the position for multiple SplitBlocks including their links.
     *
     * @param splitblock
     * @param all
     * @param graph
     */
    protected static void changeSplitBlockPositionMulti(SplitBlock splitblock, Object[] all, XcosDiagram graph) {
        adjustSplitBlock(splitblock);
        mxICell sourcePort = getSplitSourcePort(splitblock);
        List<mxICell> listTargetPorts = getSplitAllTargetPort(splitblock);
        List<mxICell> listSplitBlocks = new ArrayList<>(0);
        listSplitBlocks.add(splitblock);
        listSplitBlocks.addAll(getAllChildrenSplitBlockByLevel(splitblock));
        Map<mxICell, List<mxPoint>> mapRoutes = new HashMap<>(0);
        List<List<mxPoint>> listRoutes = new ArrayList<>(0);
        Object[] allObstacles = getObstacles(splitblock, all);
        XcosRoute util = new XcosRoute();
        // get all optimal routes for source to each target
        for (mxICell targetPort : listTargetPorts) {
            List<mxPoint> list = new ArrayList<mxPoint>(0);
            if (sourcePort != null) {
                list.add(getPortPosition(sourcePort));
            }
            boolean flag = util.computeRoute(sourcePort, targetPort, allObstacles, graph);
            if (flag) {
                for (mxPoint point : util.getNonRedundantPoints()) {
                    list.add(new mxPoint(Math.round(point.getX()), Math.round(point.getY())));
                }
            }
            if (targetPort != null) {
                list.add(getPortPosition(targetPort));
            }
            mapRoutes.put(targetPort, list);
            listRoutes.add(list);
        }
        adjustRoutes(listRoutes, allObstacles);
        // set the new position for each Split Block
        for (mxICell cell : listSplitBlocks) {
            SplitBlock split = (SplitBlock) cell;
            List<mxICell> listTargets = getSplitAllTargetPort(split);
            List<List<mxPoint>> listTargetRoutes = new ArrayList<>(0);
            for (mxICell target : listTargets) {
                listTargetRoutes.add(mapRoutes.get(target));
            }
            mxPoint splitPoint = getSplitPoint(listTargetRoutes);
            if (splitPoint == null) {
                ScilabModalDialog.show(XcosTab.get(graph), new String[] { XcosMessages.BASP_POSITION_NOT_FOUND },
                        XcosMessages.BLOCK_AUTO_POSITION_SPLIT_BLOCK, IconType.INFORMATION_ICON);
                return;
            }
            mxGeometry splitGeo = (mxGeometry) graph.getModel().getGeometry(split).clone();
            splitGeo.setX(splitPoint.getX() - splitGeo.getWidth() / 2);
            splitGeo.setY(splitPoint.getY() - splitGeo.getHeight() / 2);
            graph.getModel().setGeometry(split, splitGeo);
        }
        // update the orientation for each Split Block
        for (mxICell cell : listSplitBlocks) {
            SplitBlock split = (SplitBlock) cell;
            List<mxICell> listTargets = getSplitAllTargetPort(split);
            List<List<mxPoint>> listTargetRoutes = new ArrayList<>(0);
            for (mxICell target : listTargets) {
                listTargetRoutes.add(mapRoutes.get(target));
            }
            updatePortOrientation(split, listTargetRoutes, graph);
        }
        // update the relative links
        for (mxICell cell : listSplitBlocks) {
            SplitBlock split = (SplitBlock) cell;
            updateSplitLink(split, allObstacles, graph);
        }
    }

    /**
     * Get the source Port of a SplitBlock.
     *
     * @param splitblock
     * @return
     */
    private static mxICell getSplitSourcePort(SplitBlock splitblock) {
        mxICell cell = null;
        BasicPort in = splitblock.getIn();
        mxICell edge = in.getEdgeAt(0);
        if (edge != null && edge instanceof mxCell) {
            cell = ((mxCell) edge).getSource();
            if (cell == in) {
                cell = ((mxCell) edge).getTarget();
            }
        }
        return cell;
    }

    /**
     * Get the target Port of a SplitBlock according to its Output.
     *
     * @param splitblock
     * @param out
     * @return
     */
    private static mxICell getSplitTargetPort(SplitBlock splitblock, BasicPort out) {
        mxICell cell = null;
        mxICell edge = out.getEdgeAt(0);
        if (edge != null && edge instanceof mxCell) {
            cell = ((mxCell) edge).getTarget();
            if (cell == out) {
                cell = ((mxCell) edge).getSource();
            }
        }
        return cell;
    }

    /**
     * Get all the final target Ports of a root SplitBlock.
     * Add them in an order based on split blocks' order.
     *
     * @param splitblock
     * @return
     */
    private static List<mxICell> getSplitAllTargetPort(SplitBlock splitblock) {
        List<mxICell> list = new ArrayList<>(0);
        BasicPort out1 = splitblock.getOut1();
        BasicPort out2 = splitblock.getOut2();
        mxICell targetPort1 = getSplitTargetPort(splitblock, out1);
        mxICell targetPort2 = getSplitTargetPort(splitblock, out2);
        // add normal block firstly
        if (!(targetPort1.getParent() instanceof SplitBlock)) {
            list.add(targetPort1);
        }
        if (!(targetPort2.getParent() instanceof SplitBlock)) {
            list.add(targetPort2);
        }
        // if it was split block, add their final target.
        if (targetPort1.getParent() instanceof SplitBlock) {
            list.addAll(getSplitAllTargetPort((SplitBlock) targetPort1.getParent()));
        }
        if (targetPort2.getParent() instanceof SplitBlock) {
            list.addAll(getSplitAllTargetPort((SplitBlock) targetPort2.getParent()));
        }
        return list;
    }

    /**
     * Get all the children Split Blocks of a Split Block excluding itself.
     * Add them in an order according to their level.
     *
     * @param splitblock
     * @return
     */
    private static List<mxICell> getAllChildrenSplitBlockByLevel(SplitBlock splitblock) {
        List<mxICell> list = new ArrayList<>(0);
        BasicPort out1 = splitblock.getOut1();
        BasicPort out2 = splitblock.getOut2();
        mxICell targetPort1 = getSplitTargetPort(splitblock, out1);
        mxICell targetPort2 = getSplitTargetPort(splitblock, out2);
        // add Split Blocks in this level.
        if (targetPort1.getParent() instanceof SplitBlock) {
            list.add(targetPort1.getParent());
        }
        if (targetPort2.getParent() instanceof SplitBlock) {
            list.add(targetPort2.getParent());
        }
        // then add the children.
        if (targetPort1.getParent() instanceof SplitBlock) {
            list.addAll(getAllChildrenSplitBlockByLevel((SplitBlock) targetPort1.getParent()));
        }
        if (targetPort2.getParent() instanceof SplitBlock) {
            list.addAll(getAllChildrenSplitBlockByLevel((SplitBlock) targetPort2.getParent()));
        }
        return list;
    }

    /**
     * Get all links on this Split Block.
     * Each Split Block has an IN link and two OUT links which would be also the IN link for its child split block.
     *
     * @param splitblock
     * @return
     */
    private static List<mxICell> getAllLinksOnSplitBlock(SplitBlock splitblock) {
        List<mxICell> listLinks = new ArrayList<>(0);
        List<mxICell> listBlocks = new ArrayList<>(0);
        listBlocks.add(splitblock);
        listBlocks.addAll(getAllChildrenSplitBlockByLevel(splitblock));
        mxICell link1 = splitblock.getIn().getEdgeAt(0);
        if (!listLinks.contains(link1)) {
            listLinks.add(link1);
        }
        for (mxICell block : listBlocks) {
            SplitBlock split = (SplitBlock) block;
            mxICell link2 = split.getOut1().getEdgeAt(0);
            mxICell link3 = split.getOut2().getEdgeAt(0);
            if (!listLinks.contains(link2)) {
                listLinks.add(link2);
            }
            if (!listLinks.contains(link3)) {
                listLinks.add(link3);
            }
        }
        return listLinks;
    }

    /**
     * Adjust the Blocks aligned linked to the split block. Only let SplitBlock
     * aligned to normal Block.
     *
     * @param splitblock
     */
    private static void adjustSplitBlock(SplitBlock splitblock) {
        BasicPort out1 = splitblock.getOut1();
        BasicPort out2 = splitblock.getOut2();
        mxICell sourcePort = getSplitSourcePort(splitblock);
        mxICell targetPort1 = getSplitTargetPort(splitblock, out1);
        mxICell targetPort2 = getSplitTargetPort(splitblock, out2);
        if (sourcePort.getParent() instanceof SplitBlock) {
            // if it is a Split Block
            if (!(targetPort1.getParent() instanceof SplitBlock)) {
                adjustCell(sourcePort, targetPort1);
            }
            if (!(targetPort2.getParent() instanceof SplitBlock)) {
                adjustCell(sourcePort, targetPort2);
            }
        }
        if (targetPort1 instanceof SplitBlock) {
            if (!(sourcePort.getParent() instanceof SplitBlock)) {
                adjustCell(targetPort1, sourcePort);
            }
        }
        if (targetPort2 instanceof SplitBlock) {
            if (!(sourcePort.getParent() instanceof SplitBlock)) {
                adjustCell(targetPort2, sourcePort);
            }
        }
    }

    /**
     * Adjust the cell position align to the base one only if their difference
     * are less than XcosRouteUtils.ALIGN_STRICT_ERROR.
     *
     * @param cell
     * @param cellBase
     */
    private static void adjustCell(mxICell cell, mxICell cellBase) {
        double error = XcosRouteUtils.ALIGN_STRICT_ERROR;
        mxPoint cellPoint = getPortPosition(cell);
        mxGeometry cellGeo = cell.getParent().getGeometry();
        mxPoint cellBasePoint = getPortPosition(cellBase);
        if (Math.abs(cellPoint.getX() - cellBasePoint.getX()) <= error) {
            cellGeo.setX(cellBasePoint.getX() - cellGeo.getWidth() / 2);
        }
        if (Math.abs(cellPoint.getY() - cellBasePoint.getY()) <= error) {
            cellGeo.setY(cellBasePoint.getY() - cellGeo.getHeight() / 2);
        }
    }

    /**
     * Adjust the optimal routes and make them aligned with each other when they are parallel.
     *
     * @param list1
     * @param list2
     * @param allObstacles
     */
    private static void adjustRoutes(List<mxPoint> list1, List<mxPoint> list2, Object[] allObstacles) {
        List<List<mxPoint>> listRoutes = new ArrayList<>(0);
        listRoutes.add(list1);
        listRoutes.add(list2);
        adjustRoutes(listRoutes, allObstacles);
    }

    /**
     * Adjust the optimal routes and make them aligned with each other when they are parallel.
     *
     * @param listRoutes
     * @param allObstacles
     */
    private static void adjustRoutes(List<List<mxPoint>> listRoutes, Object[] allObstacles) {
        for (int i = 0; i < listRoutes.size() - 1; i++) {
            List<mxPoint> list1 = listRoutes.get(i);
            for (int j = i + 1; j < listRoutes.size(); j++) {
                List<mxPoint> list2 = listRoutes.get(j);
                for (int m = 0; m < list1.size() - 1; m++) {
                    mxPoint p11 = list1.get(m);
                    mxPoint p12 = list1.get(m + 1);
                    double x11 = p11.getX();
                    double y11 = p11.getY();
                    double x12 = p12.getX();
                    double y12 = p12.getY();
                    for (int n = 0; n < list2.size() - 1; n++) {
                        if ((m == 0 && n == 0)
                                || ((m == list1.size() - 2) && (n == list2.size() - 2))
                                || (m == 0 && (n == list2.size() - 2))
                                || (n == 0 && (m == list1.size() - 2))) {
                            // if both are source/target, two lines are not movable.
                            continue;
                        }
                        mxPoint p21 = list2.get(n);
                        mxPoint p22 = list2.get(n + 1);
                        double x21 = p21.getX();
                        double y21 = p21.getY();
                        double x22 = p22.getX();
                        double y22 = p22.getY();
                        // if they are already aligned,
                        if (x11 == x12 && x21 == x22 && x11 == x21) {
                            continue;
                        }
                        if (y11 == y12 && y21 == y22 && y11 == y21) {
                            continue;
                        }
                        if (XcosRouteUtils.isLineParallel(x11, y11, x12, y12, x21, y21, x22, y22, true)) {
                            // if two line segments are parallel.
                            if (m == 0 || m == list1.size() - 2) {
                                // if it is source point or target, line1 is not movable.
                                mxPoint p20 = list2.get(n - 1);
                                mxPoint p23 = list2.get(n + 2);
                                if (x11 == x12) {
                                    boolean flag2 = !XcosRouteUtils.checkObstacle(p20.getX(), p20.getY(), x11, y21, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x11, y21, x11, y22, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x11, y22, p23.getX(), p23.getY(), allObstacles);
                                    if (flag2) {
                                        p21.setX(x11);
                                        p22.setX(x11);
                                    }
                                } else if (y11 == y12) {
                                    boolean flag2 = !XcosRouteUtils.checkObstacle(p20.getX(), p20.getY(), x21, y11, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x21, y11, x22, y11, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x22, y11, p23.getX(), p23.getY(), allObstacles);
                                    if (flag2) {
                                        p21.setY(y11);
                                        p22.setY(y11);
                                    }
                                }
                            } else if (n == 0 || n == list2.size() - 2) {
                                // if it is source point or target, line2 is not movable.
                                mxPoint p10 = list1.get(m - 1);
                                mxPoint p13 = list1.get(m + 2);
                                if (x11 == x12) {
                                    boolean flag1 = !XcosRouteUtils.checkObstacle(p10.getX(), p10.getY(), x21, y11, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x21, y11, x21, y12, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x21, y12, p13.getX(), p13.getY(), allObstacles);
                                    if (flag1) {
                                        p11.setX(x21);
                                        p12.setX(x21);
                                    }
                                } else if (y11 == y12) {
                                    boolean flag1 = !XcosRouteUtils.checkObstacle(p10.getX(), p10.getY(), x11, y21, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x11, y21, x12, y21, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x12, y21, p13.getX(), p13.getY(), allObstacles);
                                    if (flag1) {
                                        p11.setY(y21);
                                        p12.setY(y21);
                                    }
                                }
                            } else {
                                // both are movable.
                                mxPoint p20 = list2.get(n - 1);
                                mxPoint p23 = list2.get(n + 2);
                                mxPoint p10 = list1.get(m - 1);
                                mxPoint p13 = list1.get(m + 2);
                                if (x11 == x12) {
                                    boolean flag2 = !XcosRouteUtils.checkObstacle(p20.getX(), p20.getY(), x11, y21, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x11, y21, x11, y22, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x11, y22, p23.getX(), p23.getY(), allObstacles);
                                    boolean flag1 = !XcosRouteUtils.checkObstacle(p10.getX(), p10.getY(), x21, y11, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x21, y11, x21, y12, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x21, y12, p13.getX(), p13.getY(), allObstacles);
                                    if (flag2) {
                                        p21.setX(x11);
                                        p22.setX(x11);
                                    } else if (flag1) {
                                        p11.setX(x21);
                                        p12.setX(x21);
                                    }
                                } else if (y11 == y12) {
                                    boolean flag2 = !XcosRouteUtils.checkObstacle(p20.getX(), p20.getY(), x21, y11, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x21, y11, x22, y11, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x22, y11, p23.getX(), p23.getY(), allObstacles);
                                    boolean flag1 = !XcosRouteUtils.checkObstacle(p10.getX(), p10.getY(), x11, y21, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x11, y21, x12, y21, allObstacles)
                                            && !XcosRouteUtils.checkObstacle(x12, y21, p13.getX(), p13.getY(), allObstacles);
                                    if (flag2) {
                                        p21.setY(y11);
                                        p22.setY(y11);
                                    } else if (flag1) {
                                        p11.setY(y21);
                                        p12.setY(y21);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the route for the source and the target ignoring the SplitBlock.
     *
     * @param source
     * @param target
     * @param all
     * @param graph
     * @return all turning points in a route including the start and end points
     */
    private static List<mxPoint> getRoute(SplitBlock splitblock, mxICell source, mxICell target, Object[] all, XcosDiagram graph) {
        XcosRoute util = new XcosRoute();
        // get all obstacles, excluding splitblock itself or its relative link.
        mxICell link1 = splitblock.getIn().getEdgeAt(0);
        mxICell link2 = splitblock.getOut1().getEdgeAt(0);
        mxICell link3 = splitblock.getOut2().getEdgeAt(0);
        Object[] allOtherCells = util.getAllOtherCells(all, source, target, source.getEdgeAt(0), target.getEdgeAt(0),
                link1, link2, link3);
        List<mxPoint> list = new ArrayList<mxPoint>(0);
        if (source != null) {
            list.add(getPortPosition(source));
        }
        boolean flag = util.computeRoute(source, target, allOtherCells, graph);
        if (flag) {
            for (mxPoint point : util.getNonRedundantPoints()) {
                list.add(new mxPoint(Math.round(point.getX()), Math.round(point.getY())));
            }
        }
        if (target != null) {
            list.add(getPortPosition(target));
        }
        return list;
    }

    /**
     * Get all obstacle blocks.
     *
     * @param splitblock
     * @return
     */
    private static Object[] getObstacles(SplitBlock splitblock, Object[] all) {
        mxICell sourcePort = getSplitSourcePort(splitblock);
        List<mxICell> listTargetPorts = getSplitAllTargetPort(splitblock);
        List<mxICell> listSplitBlocks = new ArrayList<>(0);
        listSplitBlocks.add(splitblock);
        listSplitBlocks.addAll(getAllChildrenSplitBlockByLevel(splitblock));
        List<mxICell> listLinks = getAllLinksOnSplitBlock(splitblock);
        List<mxICell> listNotObstable = new ArrayList<>(0);
        listNotObstable.add(sourcePort);
        listNotObstable.addAll(listTargetPorts);
        listNotObstable.addAll(listSplitBlocks);
        listNotObstable.addAll(listLinks);
        Object[] notObstacles = listNotObstable.toArray();
        XcosRoute util = new XcosRoute();
        Object[] allObstacles = util.getAllOtherCells(all, notObstacles);
        return allObstacles;
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
        point.setX(Math.round(point.getX()));
        point.setY(Math.round(point.getY()));
        return point;
    }

    /**
     * Get the split point for two routes.
     *
     * @param list1 the first route
     * @param list2 the second route
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
     * Get the split point for multiple routes.
     *
     * @param listRoutes the list of routes
     * @return
     */
    private static mxPoint getSplitPoint(List<List<mxPoint>> listRoutes) {
        List<mxPoint> listAllSplitPoints = new ArrayList<>(0);
        for (int i = 0; i < listRoutes.size() - 1; i++) {
            List<mxPoint> list1 = listRoutes.get(i);
            for (int j = i + 1; j < listRoutes.size(); j++) {
                List<mxPoint> list2 = listRoutes.get(j);
                mxPoint point = getSplitPoint(list1, list2);
                if (point == null || listAllSplitPoints.contains(point)) {
                    continue;
                }
                listAllSplitPoints.add(point);
            }
        }
        mxPoint splitPoint = null;
        for (mxPoint point : listAllSplitPoints) {
            double x = point.getX();
            double y = point.getY();
            for (int i = 0; i < listRoutes.size(); i++) {
                if (!XcosRouteUtils.pointInLink(x, y, listRoutes.get(i))) {
                    break;
                }
                if (i == listRoutes.size() - 1) {
                    splitPoint = new mxPoint();
                    splitPoint.setX(x);
                    splitPoint.setY(y);
                }
            }
        }
        return splitPoint;
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
        if (orientationIn != null) {
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
     * @param list1
     *            the first optimal route including start Port and end Port
     * @param list2
     *            the second optimal route including start Port and end Port
     * @param splitPoint
     *            the new position for the Split Block
     * @return
     */
    private static Orientation getInportOrientation(List<mxPoint> list1, List<mxPoint> list2, mxPoint splitPoint) {
        List<List<mxPoint>> list = new ArrayList<>(0);
        list.add(list1);
        list.add(list2);
        mxPoint startPoint = list1.get(0);
        return getInportOrientation(list, startPoint, splitPoint);
    }

    /**
     * Get the orientation for the Input Port of a Split Block.
     *
     * @param list
     *            list of routes
     * @param startPoint
     *            the start point
     * @param splitPoint
     *            the new position for the Split Block
     * @return
     */
    private static Orientation getInportOrientation(List<List<mxPoint>> list, mxPoint startPoint, mxPoint splitPoint) {
        int[] turning = new int[list.size()];
        Orientation[] orientation = new Orientation[list.size()];
        double x = splitPoint.getX();
        double y = splitPoint.getY();
        double xStart = startPoint.getX();
        double yStart = startPoint.getY();
        for (int p = 0; p < list.size(); p++) {
            List<mxPoint> list1 = list.get(p);
            int num1 = list1.size();
            if (num1 <= 1) {
                continue;
            }
            for (int i = 1, count = 0; i < num1; i++, count++) {
                mxPoint p0 = list1.get(i - 1);
                mxPoint p1 = list1.get(i);
                double x0 = p0.getX();
                double y0 = p0.getY();
                double x1 = p1.getX();
                double y1 = p1.getY();
                // start from the start point,
                if (XcosRouteUtils.pointInLineSegment(xStart, yStart, x0, y0, x1, y1)) {
                    count = 0;
                }
                // if the point is in this segment,
                if (XcosRouteUtils.pointInLineSegment(x, y, x0, y0, x1, y1)) {
                    turning[p] = count;
                    if (x1 == x0 && y1 > y0) { // segment: south
                        orientation[p] = Orientation.NORTH;
                    } else if (x1 == x0 && y1 < y0) { // segment: north
                        orientation[p] = Orientation.SOUTH;
                    } else if (y1 == y0 && x1 > x0) { // segment: east
                        orientation[p] = Orientation.WEST;
                    } else if (y1 == y0 && x1 < x0) { // segment: west
                        orientation[p] = Orientation.EAST;
                    }
                    break;
                }
            }
        }
        int index = 0;
        int tmp = turning[0];
        for (int i = 1; i < turning.length; i++) {
            if (turning[i] < tmp) {
                tmp = turning[i];
                index = i;
            }
        }
        return orientation[index];
    }

    /**
     * Get the orientation for the one Output Port of a Split Block according to
     * its optimal route.
     *
     * @param list
     *            the second optimal route including start Port and end Port
     * @param splitPoint
     *            the new position for the Split Block
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
     * Update SplitBlock's input port and output ports.
     *
     * @param split
     * @param list
     * @param graph
     */
    private static void updatePortOrientation(SplitBlock split, List<List<mxPoint>> list, XcosDiagram graph) {
        mxGeometry splitGeo = graph.getModel().getGeometry(split);
        mxPoint splitPoint = new mxPoint(splitGeo.getCenterX(), splitGeo.getCenterY());
        // get Input Port Orientation
        mxICell source = getSplitSourcePort(split);
        mxPoint startPoint = getPortPosition(source);
        split.getIn().setOrientation(getInportOrientation(list, startPoint, splitPoint));
        // get OutPut Port Orientation
        BasicPort outport1 = split.getOut1();
        BasicPort outport2 = split.getOut2();
        mxPoint out1Position = getPortPosition(getSplitTargetPort(split, outport1));
        mxPoint out2Position = getPortPosition(getSplitTargetPort(split, outport2));
        for (int p = 0; p < list.size(); p++) {
            List<mxPoint> list1 = list.get(p);
            if (XcosRouteUtils.pointInLink(out1Position.getX(), out1Position.getY(), list1)) {
                outport1.setOrientation(getOutportOrientation(list1, splitPoint));
            }
            if (XcosRouteUtils.pointInLink(out2Position.getX(), out2Position.getY(), list1)) {
                outport2.setOrientation(getOutportOrientation(list1, splitPoint));
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
        BasicLink linkIn = (BasicLink) split.getIn().getEdgeAt(0);
        BasicLink linkOut1 = (BasicLink) split.getOut1().getEdgeAt(0);
        BasicLink linkOut2 = (BasicLink) split.getOut2().getEdgeAt(0);
        boolean lockPort = true;
        reset(graph, linkIn);
        reset(graph, linkOut1);
        reset(graph, linkOut2);
        graph.setCellStyles(mxConstants.STYLE_NOEDGESTYLE, "1", new BasicLink[] { linkIn, linkOut1, linkOut2 });
        route.updateRoute(linkIn, all, graph, lockPort);
        route.updateRoute(linkOut1, all, graph, lockPort);
        route.updateRoute(linkOut2, all, graph, lockPort);
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
