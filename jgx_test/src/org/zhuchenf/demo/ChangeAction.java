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

    // private List<mxPoint> listPath = new ArrayList<>(0);

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
                this.updateLinkOptimal(cells);
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
        for (Object o : cells) {
            if (o instanceof mxCell) {
                mxCell c = (mxCell) o;
                if (c.isEdge()) {
                    this.updateRoute(c, graph);
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
    protected void updateRoute(mxCell cell, ScilabGraph graph) {
        Object[] all = graph.getChildCells(graph.getDefaultParent());
        mxICell src = cell.getSource();
        mxICell tgt = cell.getTarget();
        all = removeMyself(all, cell, src, tgt);
        if (src != null && tgt != null) {
            System.out.println("All other cells: " + all.length);
            System.out.println("Change the edge.");
            graph.getModel().beginUpdate();
            try {
                // graph.setCellStyle(getPath(cell,all), new Object[] {cell});
                mxGeometry geometry = new mxGeometry();
                List<mxPoint> list = getTurningPoints(cell, all);
                if (list == null) {
                    graph.setCellStyle("", new Object[] { cell });
                    // graph.setCellStyles(mxConstants.STYLE_NOEDGESTYLE, "1",
                    // new Object[] { cell });
                    graph.setCellStyles(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ELBOW,
                            new Object[] { cell });
                    graph.setCellStyles(mxConstants.STYLE_ELBOW, mxConstants.ELBOW_HORIZONTAL,
                            new Object[] { cell });
                    graph.resetEdge(cell);
                    System.out.println("Optimal is Straight.");
                } else if (list.size() == 0) {
                    // if no optimal route is found, keep the original one.
                    ;
                } else {
                    geometry.setPoints(list);
                    ((mxGraphModel) (graph.getModel())).setGeometry(cell, geometry);
                }
            } finally {
                graph.getModel().endUpdate();
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
    public List<mxPoint> getTurningPoints(mxCell cell, Object[] allCells) {
        List<mxPoint> listPoints = new ArrayList<>(0);
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
            return null;
        }
        listPoints.add(srcp1);
        listPoints.add(tgtp1);
        return listPoints;
    }

    public void check(mxPoint p1, mxPoint p2) {
        // point1 and point2 are not in the vertical or horizontal line.
        
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
        case RIGHT:
            point.setX(point.getX() + away);
            break;
        case DOWN:
            point.setY(point.getY() - away);
            break;
        case LEFT:
            point.setX(point.getX() - away);
            break;
        case UP:
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
        MyOrientation pos = MyConstants.MyOrientation.RIGHT;
        double portx = port.getGeometry().getX() - 0.5;
        double porty = port.getGeometry().getY() - 0.5;
        if ((portx) >= Math.abs(porty)) {
            pos = MyOrientation.RIGHT;
        } else if (porty <= -Math.abs(portx)) {
            pos = MyOrientation.DOWN;
        } else if (portx <= -Math.abs(porty)) {
            pos = MyOrientation.LEFT;
        } else if (porty >= Math.abs(portx)) {
            pos = MyOrientation.UP;
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
     * @param me
     * @return a new array of all objects excluding me
     */
    public Object[] removeMyself(Object[] all, Object... me) {
        List<Object> listme = Arrays.asList(me);
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
        System.out.println("Size: " + newAll.length);
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
        MyOrientation pos = MyConstants.MyOrientation.RIGHT;
        double x1 = point1.getX();
        double y1 = point1.getY();
        double x2 = point1.getX();
        double y2 = point2.getY();
        double x = x2 - x1;
        double y = y2 - y1;
        if (x >= Math.abs(y)) {
            pos = MyOrientation.RIGHT;
        } else if (y <= -Math.abs(x)) {
            pos = MyOrientation.DOWN;
        } else if (x <= -Math.abs(y)) {
            pos = MyOrientation.LEFT;
        } else if (y >= Math.abs(x)) {
            pos = MyOrientation.UP;
        }
        return pos;
    }

}
