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
                geometry.setPoints(listRoute);
                ((mxGraphModel) (graph.getModel())).setGeometry(cell, geometry);
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
        mxPoint srcp = new mxPoint(srcx, srcy);
        double tgtx = graph.getView().getState(tgt).getCenterX();
        double tgty = graph.getView().getState(tgt).getCenterY();
        mxPoint tgtp = new mxPoint(tgtx, tgty);
        // get a new point a little away from port.
        mxPoint srcp1 = new mxPoint(srcx, srcy);
        mxPoint tgtp1 = new mxPoint(tgtx, tgty);
        this.getPointAwayPort(srcp1, src);
        this.getPointAwayPort(tgtp1, tgt);
        // if two ports are not oblique and not in the same direction,
        // use straight route.
        if ((!checkOblique(srcp, tgtp) || !checkOblique(srcp1, tgtp1))
                && !checkObstacle(srcp1, tgtp1, allCells)) {
            return true;
        }
        // listPath.clear();
        // System.out.println(pathValue(srcp1, tgtp1, new mxPoint((srcx +
        // srcp1.getX()) / 2,
        // (srcy + srcp1.getY()) / 2), allCells));
        listRoute.add(srcp1);
        // Collections.reverse(listPath);
        // listRoute.addAll(listPath);
        listRoute.add(tgtp1);
        return true;
    }

    public List<mxPoint> check(mxPoint p1, mxPoint p2) {
        // point1 and point2 are not in the vertical or horizontal line.
        List<mxPoint> list = new ArrayList<>(0);

        return list;
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
        double away = MyConstants.SLOPE_ERROR;
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
        if (parent == null || parent == graph.getDefaultParent()) {
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

}
