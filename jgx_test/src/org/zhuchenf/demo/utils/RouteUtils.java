package org.zhuchenf.demo.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.scilab.modules.graph.ScilabGraph;
import org.zhuchenf.demo.MyConstants;
import org.zhuchenf.demo.MyConstants.MyOrientation;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxUtils;

public class RouteUtils {

    private List<mxPoint> listRoute = new ArrayList<mxPoint>(0);

    /**
     * Update the Edge.
     * 
     * @param cell
     * @param graph
     */
    public void updateRoute(mxCell cell, Object[] all, ScilabGraph graph) {
        mxICell src = cell.getSource();
        mxICell tgt = cell.getTarget();
        Object[] allOtherCells = getAllOtherCells(all, cell, src, tgt);
        if (src != null && tgt != null) {
            System.out.println("All other vertices: " + allOtherCells.length);
            List<mxPoint> ps = cell.getGeometry().getPoints();
            System.out.println("Edge current turning points: "
                    + ((ps == null) ? 0 : ps.size()));
            // graph.setCellStyle(getPath(cell,all), new Object[] {cell});
            mxGeometry geometry = new mxGeometry();
            boolean flag = computeRoute(cell, allOtherCells, graph);
            if (flag) {
                List<mxPoint> list = new ArrayList<mxPoint>();
                list.addAll(listRoute);
                // double scale = graph.getView().getScale();
                // for (mxPoint p : list) {
                // p.setX(p.getX() / scale);
                // p.setY(p.getY() / scale);
                // }
                geometry.setPoints(list);
                ((mxGraphModel) (graph.getModel())).setGeometry(cell, geometry);
                System.out.println(getLinePoints(cell));
                listRoute.clear();
            } else {
                System.out.println("No optimal");
                graph.setCellStyle("", new Object[] { cell });
                // graph.setCellStyles(mxConstants.STYLE_NOEDGESTYLE, "1",
                // new Object[] { cell });
                graph.setCellStyles(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ELBOW,
                        new Object[] { cell });
                graph.setCellStyles(mxConstants.STYLE_ELBOW, mxConstants.ELBOW_HORIZONTAL,
                        new Object[] { cell });
                graph.resetEdge(cell);
                System.out.println("Optimal is Straight.");
            }
        }
    }

    /**
     * Get the turning points for the optimal route. If the straight route is
     * the optimal route, return null.
     * 
     * @param cell
     * @param allCells
     * @return list of turning points
     */
    public boolean computeRoute(mxCell cell, Object[] allCells, ScilabGraph graph) {
        listRoute.clear();
        mxICell src = cell.getSource();
        mxICell tgt = cell.getTarget();
        // mxGeometry cg = cell.getGeometry();
        // mxGeometry srcg = src.getGeometry();
        // mxGeometry tgtg = tgt.getGeometry();
        // double cgx = cg.getCenterX();
        // double cgy = cg.getCenterY();
        MyOrientation sourcePortOrien = null;
        MyOrientation targetPortOrien = null;
        double srcx = graph.getView().getState(src).getCenterX();
        double srcy = graph.getView().getState(src).getCenterY();
        System.out.println("source: " + srcx + "," + srcy + " ; "
                + src.getGeometry().getCenterX() + "," + src.getGeometry().getCenterY());
        mxPoint srcp = new mxPoint(srcx, srcy);
        double tgtx = graph.getView().getState(tgt).getCenterX();
        double tgty = graph.getView().getState(tgt).getCenterY();
        mxPoint tgtp = new mxPoint(tgtx, tgty);
        // get a new point a little away from port.
        mxPoint srcp1 = new mxPoint(srcx, srcy);
        mxPoint tgtp1 = new mxPoint(tgtx, tgty);
        if (src.getParent() != null && src.getParent() != graph.getDefaultParent()) {
            this.getPointAwayPort(srcp1, src, graph);
            sourcePortOrien = this.getPortRelativeOrientation(src);
            // sourcePortOrien = this.getRelativeOrientation(src, graph);
        }
        if (tgt.getParent() != null && tgt.getParent() != graph.getDefaultParent()) {
            this.getPointAwayPort(tgtp1, tgt, graph);
            targetPortOrien = this.getPortRelativeOrientation(tgt);
            // targetPortOrien = this.getRelativeOrientation(tgt, graph);
        }
        // if two ports are not oblique and not in the same direction,
        // use straight route.
        if (!checkOblique(srcp, tgtp) && !checkObstacle(srcp, tgtp, allCells)) {
            return true;
        }
        if (!checkOblique(srcp1, tgtp1) && !checkObstacle(srcp1, tgtp1, allCells)) {
            listRoute.add(srcp1);
            listRoute.add(tgtp1);
            return true;
        }
        // listPath.clear();
        // System.out.println(pathValue(srcp1, tgtp1, new mxPoint((srcx +
        // srcp1.getX()) / 2,
        // (srcy + srcp1.getY()) / 2), allCells));
        // listRoute.add(srcp1);
        System.out.println("Orientation: " + sourcePortOrien + ", " + targetPortOrien);
        List<mxPoint> list = this.getSimpleRoute(srcp1, sourcePortOrien, tgtp1,
                targetPortOrien, allCells);
        System.out.println("SimpleRoute: " + list);
        if (list != null && list.size() > 0) {
            listRoute.addAll(list);
            return true;
        } else {
            list = this.getComplexRoute(srcp1, sourcePortOrien, tgtp1, targetPortOrien,
                    allCells, 3);
            System.out.println("ComplexRoute: " + list);
            if (list != null && list.size() > 0) {
                listRoute.addAll(list);
                return true;
            }
        }
        // Collections.reverse(listPath);
        // listRoute.addAll(listPath);
        // listRoute.add(tgtp1);
        return true;
    }

    public List<mxPoint> getSimpleRoute(mxPoint p1, mxPoint p2, Object[] allCells) {
        return getSimpleRoute(p1, null, p2, null, allCells);
    }

    /**
     * In the method, only 4 turning points at most are supported.
     * 
     * @param p1
     *            the source point
     * @param p2
     *            the target point
     * @param allCells
     *            all the possible
     * @return
     */
    public List<mxPoint> getSimpleRoute(mxPoint p1, MyOrientation o1, mxPoint p2,
            MyOrientation o2, Object[] allCells) {
        // point1 and point2 are not in the vertical or horizontal line.
        List<mxPoint> listRoute = new ArrayList<>(0);
        List<Double> listX = new ArrayList<>(0);
        List<Double> listY = new ArrayList<>(0);
        double distance = MyConstants.NORMAL_BLOCK_SIZE;
        double x1 = p1.getX();
        double y1 = p1.getY();
        double x2 = p2.getX();
        double y2 = p2.getY();
        // simplest situation
        if (!checkObstacle(new mxPoint(x1, y1), new mxPoint(x2, y1), allCells)
                && !checkObstacle(new mxPoint(x2, y1), new mxPoint(x2, y2), allCells)) {
            if (o1 != MyOrientation.EAST && o1 != MyOrientation.WEST) {
                listRoute.add(p1);
            }
            listRoute.add(new mxPoint(x2, y1));
            if (o2 != MyOrientation.NORTH && o2 != MyOrientation.SOUTH) {
                listRoute.add(p2);
            }
            return listRoute;
        } else if (!checkObstacle(new mxPoint(x1, y1), new mxPoint(x1, y2), allCells)
                && !checkObstacle(new mxPoint(x1, y2), new mxPoint(x2, y2), allCells)) {
            if (o1 != MyOrientation.NORTH && o1 != MyOrientation.SOUTH) {
                listRoute.add(p1);
            }
            listRoute.add(new mxPoint(x1, y2));
            if (o2 != MyOrientation.EAST && o2 != MyOrientation.WEST) {
                listRoute.add(p2);
            }
            return listRoute;
        }
        // check the nodes in x-coordinate
        double xmax = Math.max(x1 + distance, x2 + distance);
        double xmin = Math.min(x1 - distance, x2 - distance);
        for (double xi = xmin; xi <= xmax; xi++) {
            if (!checkObstacle(new mxPoint(x1, y1), new mxPoint(xi, y1), allCells)
                    && !checkObstacle(new mxPoint(xi, y1), new mxPoint(xi, y2), allCells)
                    && !checkObstacle(new mxPoint(xi, y2), new mxPoint(x2, y2), allCells)) {
                listX.add(xi);
            }
        }
        if (listX.size() > 0) {
            int x = choosePoint(listX, x1, x2);
            if (o1 != MyOrientation.EAST && o1 != MyOrientation.WEST) {
                listRoute.add(p1);
            }
            listRoute.add(new mxPoint(x, y1));
            listRoute.add(new mxPoint(x, y2));
            if (o2 != MyOrientation.EAST && o2 != MyOrientation.WEST) {
                listRoute.add(p2);
            }
            return listRoute;
        }
        // check the nodes in y-coordinate
        double ymax = Math.max(y1 + distance, y2 + distance);
        double ymin = Math.min(y1 - distance, y2 - distance);
        for (double yi = ymin; yi <= ymax; yi++) {
            if (!checkObstacle(new mxPoint(x1, y1), new mxPoint(x1, yi), allCells)
                    && !checkObstacle(new mxPoint(x1, yi), new mxPoint(x2, yi), allCells)
                    && !checkObstacle(new mxPoint(x2, yi), new mxPoint(x2, y2), allCells)) {
                listY.add(yi);
            }
        }
        if (listY.size() > 0) {
            int y = choosePoint(listY, y1, y2);
            if (o1 != MyOrientation.NORTH && o1 != MyOrientation.SOUTH) {
                listRoute.add(p1);
            }
            listRoute.add(new mxPoint(x1, y));
            listRoute.add(new mxPoint(x2, y));
            if (o2 != MyOrientation.NORTH && o2 != MyOrientation.SOUTH) {
                listRoute.add(p2);
            }
            return listRoute;
        }
        // listRoute.add(p1);
        // listRoute.add(p2);
        return listRoute;
    }

    /**
     * Choose the point which is in the middle of the longest continuous points.
     * 
     * @param list
     * @param p1
     * @param p2
     * @return
     */
    public int choosePoint(List<Double> list, double p1, double p2) {
        if (list == null || list.size() == 0) {
            return 0;
        }
        Collections.sort(list);
        double nMax = Math.max(p1, p2);
        double nMin = Math.min(p1, p2);
        double start = list.get(0);
        double start_temp = list.get(0);
        double end = list.get(0);
        double end_temp = list.get(0);
        boolean restart = true;
        double tmp = (end_temp + start_temp) / 2;
        double mid = (end + start) / 2;
        for (int i = 1; i < list.size(); i++) {
            if (Math.abs(list.get(i) - list.get(i - 1)) <= 1.1) {
                end_temp = list.get(i);
                restart = false;
            } else {
                if (restart) {
                    start_temp = list.get(i);
                    continue;
                }
                tmp = (end_temp + start_temp) / 2;
                mid = (end + start) / 2;
                if ((tmp < nMin || tmp > nMax) && (mid < nMax && mid > nMin)) {
                    // if the new one is out of two points and the previous one
                    // is inside,
                    start_temp = list.get(i);
                    end_temp = list.get(i);
                    restart = true;
                } else if ((Math.abs(end_temp - start_temp) > Math.abs(end - start))
                        || (tmp < nMax && tmp > nMin) && (mid < nMin || mid > nMax)) {
                    // if the new one in between two points and the previous one
                    // is out of them, or if the new one is longer than the
                    // previous one,
                    start = start_temp;
                    end = end_temp;
                    start_temp = list.get(i);
                    end_temp = list.get(i);
                    restart = true;
                }
            }
        }
        tmp = (end_temp + start_temp) / 2;
        mid = (end + start) / 2;
        if ((tmp < nMin || tmp > nMax) && (mid < nMax && mid > nMin)) {
            ;
        } else if ((Math.abs(end_temp - start_temp) > Math.abs(end - start))
                || ((tmp < nMax && tmp > nMin) && (mid < nMin || mid > nMax))) {
            start = start_temp;
            end = end_temp;
        }
        return (int) ((start + end) / 2);
    }

    public List<mxPoint> getComplexRoute(mxPoint p1, MyOrientation o1, mxPoint p2,
            MyOrientation o2, Object[] allCells, int times) {
        if (times <= 0) {
            return null;
        }
        List<mxPoint> listRoute = new ArrayList<>(0);
        List<mxPoint> listTmp = new ArrayList<>(0);
        listRoute.add(p1);
        List<mxPoint> listNewP1 = new ArrayList<>(0);
        if (o1 != MyOrientation.EAST) {
            mxPoint np1 = new mxPoint(p1.getX() - MyConstants.NORMAL_BLOCK_SIZE, p1.getY());
            if (!checkObstacle(p1, np1, allCells)) {
                listTmp = this.getSimpleRoute(np1, MyOrientation.WEST, p2, o2, allCells);
                if (listTmp != null && listTmp.size() > 0) {
                    listRoute.addAll(listTmp);
                    return listRoute;
                }
                listNewP1.add(np1);
            }
        }
        if (o1 != MyOrientation.WEST) {
            mxPoint np1 = new mxPoint(p1.getX() + MyConstants.NORMAL_BLOCK_SIZE, p1.getY());
            if (!checkObstacle(p1, np1, allCells)) {
                listTmp = this.getSimpleRoute(np1, MyOrientation.EAST, p2, o2, allCells);
                if (listTmp != null && listTmp.size() > 0) {
                    listRoute.addAll(listTmp);
                    return listRoute;
                }
                listNewP1.add(np1);
            }
        }
        if (o1 != MyOrientation.SOUTH) {
            mxPoint np1 = new mxPoint(p1.getX(), p1.getY() - MyConstants.NORMAL_BLOCK_SIZE);
            if (!checkObstacle(p1, np1, allCells)) {
                listTmp = this.getSimpleRoute(np1, MyOrientation.NORTH, p2, o2, allCells);
                if (listTmp != null && listTmp.size() > 0) {
                    listRoute.addAll(listTmp);
                    return listRoute;
                }
                listNewP1.add(np1);
            }
        }
        if (o1 != MyOrientation.NORTH) {
            mxPoint np1 = new mxPoint(p1.getX(), p1.getY() + MyConstants.NORMAL_BLOCK_SIZE);
            if (!checkObstacle(p1, np1, allCells)) {
                listTmp = this.getSimpleRoute(np1, MyOrientation.SOUTH, p2, o2, allCells);
                if (listTmp != null && listTmp.size() > 0) {
                    listRoute.addAll(listTmp);
                    return listRoute;
                }
                listNewP1.add(np1);
            }
        }
        for (mxPoint np1 : listNewP1) {
            listTmp = this.getComplexRoute(np1, null, p2, o2, allCells, times - 1);
            if (listTmp != null && listTmp.size() > 1) {
                listRoute.addAll(listTmp);
                return listRoute;
            } else {
                // listRoute.addAll(this.getSimpleRoute(np1, p2, allCells));
            }
        }
        listRoute.clear();
        return listRoute;
    }

    private List<mxPoint> listPath = new ArrayList<>(0);
    private int vTurning = 200;
    private int bound = 1000;

    public int pathValue(mxPoint current, mxPoint target, mxPoint last, Object[] allCells) {
        double step = MyConstants.BEAUTY_AWAY_DISTANCE;
        int value = (int) step;
        int vEast = 0; // value in east direction
        int vSouth = 0;// value in south direction
        int vWest = 0;// value in west direction
        int vNorth = 0;// value in north direction
        mxPoint next = new mxPoint(current);
        double x1 = current.getX();
        double y1 = current.getY();
        double x2 = target.getX();
        double y2 = target.getY();
        double x3 = last.getX();
        double y3 = last.getY();
        double dx = current.getX() - last.getX();
        double dy = current.getY() - last.getY();
        // if it goes out of bounds, it will be a dead route.
        if (x1 > bound || y1 > bound || x1 < 0 || y1 < 0) {
            return Integer.MAX_VALUE;
        }
        // if there is a block, it will be a dead route.
        if (checkObstacle(last, current, allCells)) {
            return Integer.MAX_VALUE;
        }
        // if it can "see" the target, get this point.
        if (x1 == x2 || y1 == y2) {
            if (!checkObstacle(current, target, allCells)) {
                listPath.add(current);
                return 0;
            }
        }
        if (x1 != x2 && y1 != y2) {
            if (Math.abs(x1 - x2) == (Math.abs(x1 - x3) + Math.abs(x3 - x2))) {
                if (!checkObstacle(new mxPoint(x2, y1), target, allCells)) {
                    listPath.add(new mxPoint(x2, y1));
                    return 0;
                }
            }
            if (Math.abs(y1 - y2) == (Math.abs(y1 - y3) + Math.abs(y3 - y2))) {
                if (!checkObstacle(new mxPoint(x1, y2), target, allCells)) {
                    listPath.add(new mxPoint(x1, y2));
                    return 0;
                }
            }
        }
        // it will never go back. And it takes more effort to turn.
        if (dx >= 0 && dy == 0) { // EAST→
            vWest = Integer.MAX_VALUE;
            vSouth = vTurning;
            vNorth = vTurning;
        } else if (dx == 0 && dy > 0) { // SOUTH↓
            vNorth = Integer.MAX_VALUE;
            vEast = vTurning;
            vWest = vTurning;
        } else if (dx < 0 && dy == 0) { // WEST←
            vEast = Integer.MAX_VALUE;
            vSouth = vTurning;
            vNorth = vTurning;
        } else if (dx == 0 && dy < 0) { // NORHT↑
            vSouth = Integer.MAX_VALUE;
            vEast = vTurning;
            vWest = vTurning;
        }
        if (vEast < Integer.MAX_VALUE) {
            vEast += pathValue(new mxPoint(x1 + step, y1), target, current, allCells);
        }
        if (vSouth < Integer.MAX_VALUE) {
            vSouth += pathValue(new mxPoint(x1, y1 + step), target, current, allCells);
        }
        if (vWest < Integer.MAX_VALUE) {
            vWest += pathValue(new mxPoint(x1 - step, y1), target, current, allCells);
        }
        if (vNorth < Integer.MAX_VALUE) {
            vNorth += pathValue(new mxPoint(x1, y1 - step), target, current, allCells);
        }
        if (vEast <= vSouth && vEast <= vWest && vEast <= vNorth) {
            next.setX(x1 + step);
            value += vEast;
        } else if (vSouth <= vEast && vSouth <= vWest && vSouth <= vNorth) {
            next.setY(y1 + step);
            value += vSouth;
        } else if (vWest <= vEast && vWest <= vSouth && vWest <= vNorth) {
            next.setX(x1 - step);
            value += vWest;
        } else if (vNorth <= vEast && vNorth <= vSouth && vNorth <= vWest) {
            next.setY(y1 - step);
            value += vNorth;
        }
        listPath.add(next);
        return value;
    }

    public double distance(mxPoint p1, mxPoint p2) {
        double x1 = p1.getX();
        double y1 = p1.getY();
        double x2 = p2.getX();
        double y2 = p2.getY();
        double d = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        return d;
    }

    public boolean checkLinesIntersection(double x1, double y1, double x2, double y2,
            mxCell edge) {
        if (edge.isVertex()) {
            return false;
        }
        List<mxPoint> listPoints = getLinePoints(edge);
        if (listPoints == null || listPoints.size() <= 1) {
            ;
        } else {
            for (int i = 1; i < listPoints.size(); i++) {
                mxPoint point3 = listPoints.get(i - 1);
                mxPoint point4 = listPoints.get(i);
                double x3 = point3.getX();
                double y3 = point3.getY();
                double x4 = point4.getX();
                double y4 = point4.getY();
                mxPoint point = mxUtils.intersection(x1, y1, x2, y2, x3, y3, x4, y4);
                if (point != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether two lines coincide or not. The lines are vertical or
     * horizontal. <br/>
     * <b>NOTE:</b> This method is used to check coincidence, NOT intersection!
     * 
     * @param x1
     *            the x-coordinate of the first point of the first line
     * @param y1
     *            the y-coordinate of the first point of the first line
     * @param x2
     *            the x-coordinate of the second point of the first line
     * @param y2
     *            the y-coordinate of the second point of the first line
     * @param edge
     *            the second line
     * @return <b>true</b> if two lines coincide completely or partly.
     */
    public static boolean checkLinesCoincide(double x1, double y1, double x2, double y2,
            mxCell edge) {
        if (edge.isVertex()) {
            return false;
        }
        // mxICell source = line.getSource();
        // mxICell target = line.getTarget();
        List<mxPoint> listPoints = edge.getGeometry().getPoints();
        if (listPoints == null || listPoints.size() == 0) {
            // if the edge is straight or vertical or horizontal style, there is
            // no way to check.
        } else if (listPoints.size() == 1) {
        } else {
            for (int i = 1; i < listPoints.size(); i++) {
                mxPoint point3 = listPoints.get(i - 1);
                mxPoint point4 = listPoints.get(i);
                double x3 = point3.getX();
                double y3 = point3.getY();
                double x4 = point4.getX();
                double y4 = point4.getY();
                if (x1 == x2) {
                    if (x3 != x1 || x4 != x1) {
                        return false;
                    }
                }
                if (y1 == y2) {
                    if (y3 != y1 || y4 != y1) {
                        return false;
                    }
                }
                if (linesCoincide(x1, y1, x2, y2, x3, y3, x4, y4)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether two lines coincide or not.
     * 
     * @param x1
     *            the x-coordinate of the first point of the first line
     * @param y1
     *            the y-coordinate of the first point of the first line
     * @param x2
     *            the x-coordinate of the second point of the first line
     * @param y2
     *            the y-coordinate of the second point of the first line
     * @param x3
     *            the x-coordinate of the first point of the second line
     * @param y3
     *            the y-coordinate of the first point of the second line
     * @param x4
     *            the x-coordinate of the second point of the second line
     * @param y4
     *            the y-coordinate of the second point of the second line
     * @return <b>true</b> if two lines coincide.
     */
    public static boolean linesCoincide(double x1, double y1, double x2, double y2, double x3,
            double y3, double x4, double y4) {
        // the first line is inside the second line.
        if (pointInLineSegment(x1, y1, x3, y3, x4, y4)
                && pointInLineSegment(x2, y2, x3, y3, x4, y4)) {
            return true;
        }
        // the second line is inside the first line.
        if (pointInLineSegment(x3, y3, x1, y1, x2, y2)
                && pointInLineSegment(x4, y4, x1, y1, x2, y2)) {
            return true;
        }
        double i = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        // two lines are parallel.
        if (i == 0) {
            if (pointInLineSegment(x1, y1, x3, y3, x4, y4)
                    || pointInLineSegment(x2, y2, x3, y3, x4, y4)
                    || pointInLineSegment(x3, y3, x1, y1, x2, y2)
                    || pointInLineSegment(x4, y4, x1, y1, x2, y2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the point is in the line segment or not.
     * 
     * @param x1
     *            the x-coordinate of the point
     * @param y1
     *            the y-coordinate of the point
     * @param x2
     *            the x-coordinate of the first point of the line
     * @param y2
     *            the y-coordinate of the first point of the line
     * @param x3
     *            the x-coordinate of the second point of the line
     * @param y3
     *            the y-coordinate of the second point of the line
     * @return <b>true</b> if the point is in the line segment.
     */
    public static boolean pointInLineSegment(double x1, double y1, double x2, double y2,
            double x3, double y3) {
        // double l12 = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 -
        // y1));
        // double l23 = Math.sqrt((x3 - x2) * (x3 - x2) + (y3 - y2) * (y3 -
        // y2));
        // double l13 = Math.sqrt((x3 - x1) * (x3 - x1) + (y3 - y1) * (y3 -
        // y1));
        // if (l23 == (l12 + l13)) {
        if (((x3 - x2) * (y1 - y2) == (x1 - x2) * (y3 - y2))
                && (x1 >= Math.min(x2, x3) && x1 <= Math.max(x2, x3))
                && (y1 >= Math.min(y2, y3) && y1 <= Math.max(y2, y3))) {
            return true;
        }
        return false;
    }

    /**
     * Check whether the center points of two cells are oblique or not.
     * 
     * @param point1
     * @param point2
     * @return <b>true</b> if two points are obviously oblique.
     */
    public boolean checkOblique(mxPoint point1, mxPoint point2) {
        double x1 = point1.getX();
        double y1 = point1.getY();
        double x2 = point2.getX();
        double y2 = point2.getY();
        double error = MyConstants.ALIGN_ERROR;
        if (Math.abs(x2 - x1) < error) {
            return false;
        }
        if (Math.abs(y2 - y1) < error) {
            return false;
        }
        return true;
    }

    public void getPointAwayPort(mxPoint point, mxICell port, ScilabGraph graph) {
        double away = MyConstants.BEAUTY_AWAY_DISTANCE;
        switch (getPortRelativeOrientation(port)) {
        // switch (getRelativeOrientation(port, graph)) {
        case EAST:
            point.setX(point.getX() + away);
            break;
        case SOUTH:
            point.setY(point.getY() - away);
            break;
        case WEST:
            point.setX(point.getX() - away);
            break;
        case NORTH:
            point.setY(point.getY() + away);
            break;
        }
    }

    /**
     * Get the relative position for the port to the parent.
     * 
     * @param port
     * @param parent
     * @return MyOrientation
     */
    public MyOrientation getRelativeOrientation(mxICell port, ScilabGraph graph) {
        MyOrientation pos = MyConstants.MyOrientation.EAST;
        // the coordinate (x,y) for the port.
        double portx = graph.getView().getState(port).getCenterX();
        double porty = graph.getView().getState(port).getCenterY();
        // the coordinate (x,y) and the width-height for the parent block
        mxICell parent = port.getParent();
        if (parent == null || parent == graph.getDefaultParent()) { //
            return MyOrientation.EAST;
        }
        double blockx = graph.getView().getState(parent).getCenterX();
        double blocky = graph.getView().getState(parent).getCenterY();
        double blockw = parent.getGeometry().getWidth();
        double blockh = parent.getGeometry().getHeight();
        // calculate relative coordinate based on the center of parent block.
        portx -= blockx;
        porty -= blocky;
        if ((portx) >= blockw * Math.abs(porty) / blockh) { // x>=w*|y|/h
            pos = MyOrientation.EAST;
        } else if (porty >= blockh * Math.abs(portx) / blockw) { // y>=h*|x|/w
            pos = MyOrientation.SOUTH;
        } else if (portx <= -blockw * Math.abs(porty) / blockh) { // x<=-w*|y|/h
            pos = MyOrientation.WEST;
        } else if (porty <= -blockh * Math.abs(portx) / blockw) { // y<=-h*|x|/w
            pos = MyOrientation.NORTH;
        }
        return pos;
    }

    /**
     * Check whether there is block between two points.
     * 
     * @param cell0
     * @param cell1
     * @param allCells
     * @return
     */
    public boolean checkObstacle(mxPoint cell0, mxPoint cell1, Object[] allCells) {
        double x0 = cell0.getX();
        double y0 = cell0.getY();
        double x1 = cell1.getX();
        double y1 = cell1.getY();
        for (Object o : allCells) {
            if (o instanceof mxCell) {
                mxCell c = (mxCell) o;
                mxPoint interction = c.getGeometry().intersectLine(x0, y0, x1, y1);
                if (c.isEdge()) {
                    if (checkLinesIntersection(x0, y0, x1, y1, c)) {
                        return true;
                    }
                    if (checkLinesCoincide(x0, y0, x1, y1, c)) {
                        return true;
                    }
                }
                if (interction != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Remove the relative cell from the array.
     * 
     * @param all
     * @param self
     * @return a new array of all objects excluding me
     */
    public Object[] getAllOtherCells(Object[] all, Object... self) {
        List<Object> listme = Arrays.asList(self);
        List<Object> listnew = new ArrayList<>(0);
        System.out.println(all.length + ", " + listme.size());
        // Iterator<Object> iterator = list.iterator();
        // while (iterator.hasNext()) {
        // Object o = iterator.next();
        // if (listme.contains(o)) {
        // iterator.remove();
        // }
        // }
        for (Object o : all) {
            if (!listme.contains(o)) {
                listnew.add(o);
            }
        }
        Object[] newAll = listnew.toArray();
        // Object[] newAll = new Object[all.length];
        // int i = 0;
        // for (Object o : all) {
        // boolean flag = true;
        // for (Object s : self) {
        // if (o.equals(s)) {
        // flag = false;
        // break;
        // }
        // }
        // if (flag) {
        // newAll[i] = o;
        // i++;
        // }
        // }
        // return Arrays.copyOf(all, i);
        return newAll;
    }

    /**
     * Get the direction from point1 to point2.
     * 
     * @param point1
     *            the starting point
     * @param point2
     *            the ending point
     * @return MyOrientation
     */
    public MyOrientation getPointRelativeOrientation(mxPoint point1, mxPoint point2) {
        MyOrientation pos = MyConstants.MyOrientation.EAST;
        double x1 = point1.getX();
        double y1 = point1.getY();
        double x2 = point1.getX();
        double y2 = point2.getY();
        double x = x2 - x1;
        double y = y2 - y1;
        if (x >= Math.abs(y)) {
            pos = MyOrientation.EAST;
        } else if (y <= -Math.abs(x)) {
            pos = MyOrientation.SOUTH;
        } else if (x <= -Math.abs(y)) {
            pos = MyOrientation.WEST;
        } else if (y >= Math.abs(x)) {
            pos = MyOrientation.NORTH;
        }
        return pos;
    }

    public static List<mxPoint> getLinePoints(mxCell edge) {
        if (!edge.isEdge()) {
            return null;
        }
        List<mxPoint> list = new ArrayList<mxPoint>(0);
        mxICell source = edge.getSource();
        mxGeometry sourceGeo = source.getGeometry();
        double srcx = sourceGeo.getCenterX();
        double srcy = sourceGeo.getCenterY();
        mxICell sourceParent = source.getParent();
        mxGeometry srcParGeo = sourceParent.getGeometry();
        if (srcParGeo == null) {
            srcParGeo = new mxGeometry(0, 0, 0, 0);
        }
        mxPoint offset = sourceGeo.getOffset();
        if (offset == null) {
            offset = new mxPoint(0, 0);
        }
        if (sourceGeo.isRelative()) {
            srcx = srcParGeo.getX() + sourceGeo.getX() * srcParGeo.getWidth() + offset.getX();
            srcy = srcParGeo.getY() + sourceGeo.getY() * srcParGeo.getHeight() + offset.getY();
        } else {
            srcx = srcParGeo.getX() + sourceGeo.getX() + offset.getX();
            srcy = srcParGeo.getY() + sourceGeo.getY() + offset.getY();
        }
        // System.out.println("Source state: " +
        // graph.getView().getState(source).getCenterX()
        // + ", " + graph.getView().getState(source).getCenterY());
        // System.out.println("Source Geo: " + srcx + ", " + srcy);
        list.add(new mxPoint(srcx, srcy));
        if (edge.getGeometry().getPoints() != null) {
            list.addAll(edge.getGeometry().getPoints());
        }
        mxICell target = edge.getTarget();
        mxGeometry targetGeo = target.getGeometry();
        double tgtx = targetGeo.getCenterX();
        double tgty = targetGeo.getCenterY();
        mxICell targetParent = target.getParent();
        mxGeometry tgGeo = targetParent.getGeometry();
        if (tgGeo == null) {
            tgGeo = new mxGeometry(0, 0, 0, 0);
        }
        offset = targetGeo.getOffset();
        if (offset == null) {
            offset = new mxPoint(0, 0);
        }
        if (targetGeo.isRelative()) {
            tgtx = tgGeo.getX() + targetGeo.getX() * tgGeo.getWidth() + offset.getX();
            tgty = tgGeo.getY() + targetGeo.getY() * tgGeo.getHeight() + offset.getY();
        } else {
            tgtx = tgGeo.getX() + targetGeo.getX() + offset.getX();
            tgty = tgGeo.getY() + targetGeo.getY() + offset.getY();
        }
        list.add(new mxPoint(tgtx, tgty));
        return list;
    }

    protected MyOrientation getPortRelativeOrientation(mxICell port) {
        if (port.getParent() == null) {
            return MyOrientation.EAST;
        }
        // the coordinate (x,y) for the port.
        mxGeometry portGeo = port.getGeometry();
        double portx = portGeo.getCenterY();
        double porty = portGeo.getCenterY();
        // the coordinate (x,y) and the width-height for the parent block
        mxICell parent = port.getParent();
        mxGeometry parentGeo = parent.getGeometry();
        double blockw = parentGeo.getWidth();
        double blockh = parentGeo.getHeight();
        if (portGeo.isRelative()) {
            portx *= blockw;
            porty *= blockh;
        }
        // calculate relative coordinate based on the center of parent block.
        portx -= blockw / 2;
        porty -= blockh / 2;
        MyOrientation orientation = MyOrientation.EAST;
        if ((portx) >= blockw * Math.abs(porty) / blockh) { // x>=w*|y|/h
            orientation = MyOrientation.EAST;
        } else if (porty >= blockh * Math.abs(portx) / blockw) { // y>=h*|x|/w
            orientation = MyOrientation.SOUTH;
        } else if (portx <= -blockw * Math.abs(porty) / blockh) { // x<=-w*|y|/h
            orientation = MyOrientation.WEST;
        } else if (porty <= -blockh * Math.abs(portx) / blockw) { // y<=-h*|x|/w
            orientation = MyOrientation.NORTH;
        }
        return orientation;
    }

    /**
     * 
     * @param a
     * @param b
     * @return
     */
    public boolean isAcceptableError(double a, double b) {
        if (Math.abs(a - b) <= 1) {
            return true;
        }
        return false;
    }
}
