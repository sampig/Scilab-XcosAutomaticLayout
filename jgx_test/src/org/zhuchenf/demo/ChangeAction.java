package org.zhuchenf.demo;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.scilab.modules.graph.ScilabComponent;
import org.scilab.modules.graph.ScilabGraph;
import org.zhuchenf.demo.MyConstants.MyOrientation;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;

public class ChangeAction extends AbstractAction {

    private static final long serialVersionUID = 7661628580051142609L;

    private ScilabGraph graph;

    private List<mxPoint> listRoute = new ArrayList<mxPoint>(0);

    public ChangeAction() {
        super();
    }

    public ChangeAction(String name, ScilabGraph graph) {
        super(name);
        this.graph = graph;
    }

    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();
        if (s instanceof ScilabComponent) {
            String name = getValue(Action.NAME).toString();
            Object[] cells = graph.getSelectionCells(); // .getSelectionModel().getCells()
            if ("Straight".equalsIgnoreCase(name)) {
                this.updateLinkStraight(cells);
            } else if ("Horizontal".equalsIgnoreCase(name)) {
                this.updateLinkHorizontal(cells);
            } else if ("Optimal".equalsIgnoreCase(name)) {
                graph.getModel().beginUpdate();
                try {
                    this.updateLinkOptimal(cells);
                } finally {
                    graph.getModel().endUpdate();
                }
            }
        }
    }

    public void updateLinkStraight(Object[] cells) {
        graph.getModel().beginUpdate();
        try {
            graph.resetEdges(cells);
            // mxStylesheet style = graph.getStylesheet();
            // graph.setCellStyle(style.getDefaultEdgeStyle().toString(),
            // cells);
            graph.setCellStyle(null, cells);
            graph.setCellStyles(mxConstants.STYLE_NOEDGESTYLE, "1", cells);
            for (Object edge : cells) {
                graph.resetEdge(edge);
            }
            // graph.resetEdges(cells);
        } finally {
            graph.getModel().endUpdate();
        }
    }

    public void updateLinkHorizontal(Object[] cells) {
        graph.getModel().beginUpdate();
        try {
            graph.resetEdges(cells);
            graph.setCellStyles(mxConstants.STYLE_NOEDGESTYLE, "0", cells);
            graph.setCellStyles(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ELBOW, cells);
            graph.setCellStyles(mxConstants.STYLE_ELBOW, mxConstants.ELBOW_HORIZONTAL, cells);
            // System.out.println(((mxCell)
            // cells[0]).getGeometry().getPoints().size());
            graph.resetEdges(cells);
        } finally {
            graph.getModel().endUpdate();
        }
    }

    public void updateLinkOptimal(Object[] cells) {
        Object[] all = graph.getChildCells(graph.getDefaultParent());
        // graph.getChildVertices(graph.getDefaultParent());
        for (Object o : cells) {
            if (o instanceof mxCell) {
                mxCell c = (mxCell) o;
                if (c.isEdge()) {
                    this.updateRoute(c, all, graph);
                }
            }
        }
    }

    /**
     * Update the Edge.
     * 
     * @param cell
     * @param graph
     */
    protected void updateRoute(mxCell cell, Object[] all, ScilabGraph graph) {
        mxICell src = cell.getSource();
        mxICell tgt = cell.getTarget();
        Object[] allOtherCells = getAllOtherCells(all, cell, src, tgt);
        if (src != null && tgt != null) {
            System.out.println("All other vertices: " + allOtherCells.length);
            System.out.println("Change the edge.");
            // graph.setCellStyle(getPath(cell,all), new Object[] {cell});
            mxGeometry geometry = new mxGeometry();
            boolean flag = computeRoute(cell, allOtherCells);
            if (flag) {
                List<mxPoint> list = new ArrayList<mxPoint>(0);
                list.addAll(listRoute);
                geometry.setPoints(list);
                ((mxGraphModel) (graph.getModel())).setGeometry(cell, geometry);
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
    public boolean computeRoute(mxCell cell, Object[] allCells) {
        listRoute.clear();
        mxICell src = cell.getSource();
        mxICell tgt = cell.getTarget();
        // mxGeometry cg = cell.getGeometry();
        // mxGeometry srcg = src.getGeometry();
        // mxGeometry tgtg = tgt.getGeometry();
        // double cgx = cg.getCenterX();
        // double cgy = cg.getCenterY();
        double srcx = graph.getView().getState(src).getCenterX();
        double srcy = graph.getView().getState(src).getCenterY();
        System.out.println(srcx + "," + srcy + " ; " + src.getGeometry().getCenterX() + ","
                + src.getGeometry().getCenterY());
        mxPoint srcp = new mxPoint(srcx, srcy);
        double tgtx = graph.getView().getState(tgt).getCenterX();
        double tgty = graph.getView().getState(tgt).getCenterY();
        mxPoint tgtp = new mxPoint(tgtx, tgty);
        // get a new point a little away from port.
        mxPoint srcp1 = new mxPoint(srcx, srcy);
        mxPoint tgtp1 = new mxPoint(tgtx, tgty);
        if (src.getParent() != null && src.getParent() != graph.getDefaultParent()) {
            this.getPointAwayPort(srcp1, src);
        }
        if (tgt.getParent() != null && tgt.getParent() != graph.getDefaultParent()) {
            this.getPointAwayPort(tgtp1, tgt);
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
        List<mxPoint> list = this.getSimpleRoute(srcp1, tgtp1, allCells);
        if (list != null && list.size() > 0) {
            listRoute.addAll(list);
            return true;
        } else {
            ;
        }
        // Collections.reverse(listPath);
        // listRoute.addAll(listPath);
        // listRoute.add(tgtp1);
        return true;
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
    public List<mxPoint> getSimpleRoute(mxPoint p1, mxPoint p2, Object[] allCells) {
        // point1 and point2 are not in the vertical or horizontal line.
        List<mxPoint> listRoute = new ArrayList<>(0);
        List<Double> listX = new ArrayList<>(0);
        List<Double> listY = new ArrayList<>(0);
        double distance = MyConstants.BEAUTY_DISTANCE;
        double x1 = p1.getX();
        double y1 = p1.getY();
        double x2 = p2.getX();
        double y2 = p2.getY();
        // simplest situation
        if (!checkObstacle(new mxPoint(x1, y1), new mxPoint(x2, y1), allCells)
                && !checkObstacle(new mxPoint(x2, y1), new mxPoint(x2, y2), allCells)) {
            listRoute.add(p1);
            listRoute.add(new mxPoint(x2, y1));
            listRoute.add(p2);
            return listRoute;
        } else if (!checkObstacle(new mxPoint(x1, y1), new mxPoint(x1, y2), allCells)
                && !checkObstacle(new mxPoint(x1, y2), new mxPoint(x2, y2), allCells)) {
            listRoute.add(p1);
            listRoute.add(new mxPoint(x1, y2));
            listRoute.add(p2);
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
            int x = choosePoint(listX);
            listRoute.add(p1);
            listRoute.add(new mxPoint(x, y1));
            listRoute.add(new mxPoint(x, y2));
            listRoute.add(p2);
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
            int y = choosePoint(listY);
            listRoute.add(p1);
            listRoute.add(new mxPoint(x1, y));
            listRoute.add(new mxPoint(x2, y));
            listRoute.add(p2);
            return listRoute;
        }
        listRoute.add(p1);
        listRoute.add(p2);
        return listRoute;
    }

    /**
     * 
     * @param list
     * @return
     */
    public int choosePoint(List<Double> list) {
        if (list == null || list.size() == 0) {
            return 0;
        }
        double start = list.get(0);
        double start_temp = list.get(0);
        double end = list.get(0);
        double end_temp = list.get(0);
        int counter = 1;
        for (int i = 1; i < list.size(); i++) {
            if (Math.abs(list.get(i) - list.get(i - 1)) <= 1.1) {
                end_temp = list.get(i);
                counter++;
            } else {
                if (counter == 1) {
                    start_temp = list.get(i);
                    continue;
                }
                if (Math.abs(end_temp - start_temp) > Math.abs(end - start)) {
                    start = start_temp;
                    end = end_temp;
                    start_temp = list.get(i);
                    end_temp = list.get(i);
                    counter = 1;
                }
            }
        }
        if (Math.abs(end_temp - start_temp) > Math.abs(end - start)) {
            start = start_temp;
            end = end_temp;
        }
        return (int) ((start + end) / 2);
    }

    private List<mxPoint> listPath = new ArrayList<>(0);
    private int vTurning = 200;
    private int bound = 1000;

    public int pathValue(mxPoint current, mxPoint target, mxPoint last, Object[] allCells) {
        double step = MyConstants.BEAUTY_DISTANCE;
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

    public boolean isInLine(double x1, double y1, double x2, double y2, double x3, double y3) {
        double l12 = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        double l23 = Math.sqrt((x3 - x2) * (x3 - x2) + (y3 - y2) * (y3 - y2));
        double l13 = Math.sqrt((x3 - x1) * (x3 - x1) + (y3 - y1) * (y3 - y1));
        if (l13 == (l12 + l23)) {
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
        double error = MyConstants.SLOPE_ERROR;
        if (Math.abs(x2 - x1) < error) {
            return false;
        }
        if (Math.abs(y2 - y1) < error) {
            return false;
        }
        return true;
    }

    public void getPointAwayPort(mxPoint point, mxICell port) {
        double away = MyConstants.BEAUTY_DISTANCE;
        switch (getRelativeOrientation(port)) {
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
    public MyOrientation getRelativeOrientation(mxICell port) {
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
                    // System.out.println("***Edge.");
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
    public MyOrientation getRelativeOrientation(mxPoint point1, mxPoint point2) {
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
