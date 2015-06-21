package org.zhuchenf.demo;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.scilab.modules.graph.ScilabComponent;
import org.scilab.modules.graph.ScilabGraph;
import org.zhuchenf.demo.MyConstants.MyOrientation;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

/**
 * A simple test for Clicking to find an Optimal Route.
 * 
 * @author Chenfeng ZHU
 *
 */
public class OptimizeRoute extends JFrame {

    private static final long serialVersionUID = 8452610294410264983L;

    private ScilabComponent graphComponent;
    private ScilabGraph graph;

    private Random r = new Random(System.currentTimeMillis());
    private List<Object> listVertex = new ArrayList<>(0);

    public OptimizeRoute() {
        super("Test2Block");

        graph = new ScilabGraph();
        Object parent = graph.getDefaultParent();

        mxStylesheet ss = graph.getStylesheet();
        Hashtable<String, Object> style = new Hashtable<>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
        style.put(mxConstants.STYLE_OPACITY, 50);
        style.put(mxConstants.STYLE_FONTCOLOR, "#774400");
        style.put(mxConstants.STYLE_FILLCOLOR, "blue");
        ss.putCellStyle("zhu_style", style);

        graph.getModel().beginUpdate();
        try {
            mxCell v1 = (mxCell) graph.insertVertex(parent, null, "zhu", 20, 20, 80, 30,
                    "zhu_style");
            v1.setConnectable(false);
            mxGeometry geo = graph.getModel().getGeometry(v1);
            geo.setAlternateBounds(new mxRectangle(20, 20, 80, 30));
            mxGeometry geo1 = new mxGeometry(1.0, 0.5, 5, 5);
            geo1.setOffset(new mxPoint(-2.5, 0));
            geo1.setRelative(true);
            mxCell port1 = new mxCell("Port1", geo1, "shape=ellipse;perimter=ellipsePerimeter");
            port1.setVertex(true);
            graph.addCell(port1, v1);
            mxCell v2 = (mxCell) graph.insertVertex(parent, null, "chenfeng", 340, 20, 80, 30);
            v2.setConnectable(false);
            mxGeometry geo2 = new mxGeometry(0, 0.5, 2, 2);
            geo2.setOffset(new mxPoint(-15, 0));
            geo2.setRelative(true);
            mxCell port2 = new mxCell("Port2", geo2, "");
            port2.setVertex(true);
            graph.addCell(port2, v2);
            Object v3 = graph.insertVertex(parent, null, "block", 170, 170, 80, 30);
            mxCell e1 = (mxCell) graph.insertEdge(parent, "TestLink", "comma", port1, port2);
            mxCell e2 = (mxCell) graph.insertEdge(parent, null, "block_link", v3, v2);
            e1.setStyle(ss.getDefaultEdgeStyle().toString());
            // e2.setStyle("edgeStyle=elbowEdgeStyle;elbow=horizontal;"+
            // "exitX=1;exitY=0;exitPerimeter=0;entryX=0;entryY=0;entryPerimeter=5;");
            mxGeometry g = ((mxGraphModel) (graph.getModel())).getGeometry(e2);
            g = (mxGeometry) g.clone();
            List<mxPoint> listPoints = g.getPoints();
            if (listPoints == null) {
                listPoints = new ArrayList<mxPoint>();
            }
            listPoints.add(new mxPoint(100, 100));
            listPoints.add(new mxPoint(250, 250));
            listPoints.add(new mxPoint(300, 300));
            g.setPoints(listPoints);
            ((mxGraphModel) (graph.getModel())).setGeometry(e2, g);
            this.createVertices(graph);
        } finally {
            graph.getModel().endUpdate();
        }

        graphComponent = (ScilabComponent) graph.getAsComponent();
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseReleased(e);
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Object cell = graphComponent.getCellAt(e.getX(), e.getY());
                    if (cell != null && cell instanceof mxCell) {
                        if (((mxCell) cell).isEdge()) {
                            // System.out.println("Edge: " + cell);
                        }
                        showMenu(e);
                    }
                }
            }
        });
        getContentPane().add(graphComponent);
    }

    /**
     * Create some Vertices in random position.
     * 
     * @param graph
     */
    public void createVertices(mxGraph graph) {
        for (int i = 0; i < 5; i++) {
            int x = r.nextInt(400);
            int y = r.nextInt(400);
            Object o = graph.insertVertex(graph.getDefaultParent(), null, "block" + i, x, y,
                    50, 50);
            listVertex.add(o);
        }
    }

    private String strStraight = "Straight";
    private String strHorizontal = "Horizontal";
    private String strOptimal = "Optimal";

    /**
     * Create the menu for clicking.
     * 
     * @param e
     */
    public void showMenu(MouseEvent e) {
        Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), graphComponent);
        JPopupMenu menu = new JPopupMenu();
        menu.add(this.bind(strStraight, new ChangeAction(strStraight, graph)));
        menu.add(this.bind(strHorizontal, new ChangeAction(strHorizontal, graph)));
        menu.add(this.bind(strOptimal, new ChangeAction(strOptimal, graph)));
        menu.show(graphComponent, pt.x, pt.y);
        e.consume();
    }

    public Action bind(String name, final Action action) {
        AbstractAction newAction = new AbstractAction(name) {
            private static final long serialVersionUID = -8340518045347205918L;

            public void actionPerformed(ActionEvent e) {
                action.actionPerformed(new ActionEvent(graphComponent, e.getID(), e
                        .getActionCommand()));
            }
        };
        return newAction;
    }

    public static void main(String... strings) {
        OptimizeRoute frame = new OptimizeRoute();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
}

class ChangeAction extends AbstractAction {

    private static final long serialVersionUID = 7661628580051142609L;

    private ScilabGraph graph;

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
                    graph.setCellStyles(mxConstants.STYLE_NOEDGESTYLE, "1",
                            new Object[] { cell });
                    graph.resetEdge(cell);
                    System.out.println("Optimal is Straight.");
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
        mxGeometry srcg = src.getGeometry();
        mxGeometry tgtg = tgt.getGeometry();
        // double cgx = cg.getCenterX();
        // double cgy = cg.getCenterY();
        double srcx = graph.getView().getState(src).getCenterX();
        double srcy = graph.getView().getState(src).getCenterY();
        srcg.getWidth();
        srcg.getHeight();
        double tgtx = graph.getView().getState(tgt).getCenterX();
        double tgty = graph.getView().getState(tgt).getCenterY();
        tgtg.getWidth();
        tgtg.getHeight();
        // if two ports are not oblique and not in the same direction,
        // use straight route.
        if (!checkOblique(src, tgt) && !checkDirection(src, tgt)
                && !checkObstacle(new mxPoint(), new mxPoint(), allCells)) {
            return null;
        }
        listPoints.add(new mxPoint(250, 250));
        listPoints.add(new mxPoint(100, 100));
        return listPoints;
    }

    /**
     * Check whether the center points of two cells are oblique or not.
     * 
     * @param cell1
     * @param cell2
     * @return <b>true</b> if two points are obviously oblique.
     */
    public boolean checkOblique(mxICell cell1, mxICell cell2) {
        double x1 = graph.getView().getState(cell1).getCenterX();
        double y1 = graph.getView().getState(cell1).getCenterY();
        double x2 = graph.getView().getState(cell2).getCenterX();
        double y2 = graph.getView().getState(cell2).getCenterY();
        double error = 5;
        if (Math.abs(x2 - x1) < error) {
            return false;
        }
        if (Math.abs(y2 - y1) < error) {
            return false;
        }
        return true;
    }

    /**
     * Check whether the directions of two ports are the same or not.
     * 
     * @param port1
     * @param port2
     * @return <b>true</b> if they are in the same direction.
     */
    public boolean checkDirection(mxICell port1, mxICell port2) {
        MyOrientation pos1 = getRelativePosition(port1, port1.getParent());
        MyOrientation pos2 = getRelativePosition(port2, port2.getParent());
        if (pos1 == pos2) {
            System.out.println("Same: " + pos1);
            return true;
        }
        return false;
    }

    /**
     * 
     * @param port1
     * @param port2
     * @return <b>true</b> if
     */
    public boolean checkOpposite(mxICell port1, mxICell port2) {
        MyOrientation pos1 = getRelativePosition(port1, port1.getParent());
        MyOrientation pos2 = getRelativePosition(port2, port2.getParent());
        if (pos1 == MyOrientation.DOWN && pos2 == MyOrientation.UP) {
            return true;
        }
        if (pos1 == MyOrientation.RIGHT && pos2 == MyOrientation.LEFT) {
            return true;
        }
        if (pos1 == MyOrientation.LEFT && pos2 == MyOrientation.RIGHT) {
            return true;
        }
        if (pos1 == MyOrientation.UP && pos2 == MyOrientation.DOWN) {
            return true;
        }
        return false;
    }

    /**
     * Get the relative position for the port to the parent.
     * 
     * @param port
     * @param parent
     * @return MyOrientation
     */
    public MyOrientation getRelativePosition(mxICell port, mxICell parent) {
        MyOrientation pos = MyConstants.MyOrientation.RIGHT;
        double portx = port.getGeometry().getCenterX();
        double porty = port.getGeometry().getCenterY();
        if (portx >= 0 && Math.abs(portx) >= Math.abs(porty)) {
            pos = MyOrientation.RIGHT;
        } else if (porty <= 0 && Math.abs(portx) <= Math.abs(porty)) {
            pos = MyOrientation.DOWN;
        } else if (portx <= 0 && Math.abs(portx) >= Math.abs(porty)) {
            pos = MyOrientation.LEFT;
        } else if (porty >= 0 && Math.abs(portx) <= Math.abs(porty)) {
            pos = MyOrientation.UP;
        }
        return pos;
    }

    public boolean checkObstacle(mxPoint cell1, mxPoint cell2, Object[] allCells) {
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

}
