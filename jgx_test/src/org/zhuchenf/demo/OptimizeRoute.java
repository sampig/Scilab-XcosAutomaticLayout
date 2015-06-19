package org.zhuchenf.demo;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
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

    private mxGraphComponent graphComponent;

    private Random r = new Random(System.currentTimeMillis());
    private List<Object> listVertex = new ArrayList<>(0);

    public OptimizeRoute() {
        super("Test2Block");

        mxGraph graph = new mxGraph();
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
            mxGeometry geo1 = new mxGeometry(1.0, 0.5, 10, 10);
            geo1.setOffset(new mxPoint(-5, -5));
            geo1.setRelative(true);
            mxCell port = new mxCell(null, geo1, "shape=ellipse;perimter=ellipsePerimeter");
            port.setVertex(true);
            graph.addCell(port, v1);
            Object v2 = graph.insertVertex(parent, null, "chenfeng", 340, 350, 80, 30);
            Object v3 = graph.insertVertex(parent, null, "block", 170, 170, 80, 30);
            mxCell e1 = (mxCell) graph.insertEdge(parent, null, "comma", port, v2);
            mxCell e2 = (mxCell) graph.insertEdge(parent, null, "block_link", v3, v2);
            e1.setStyle(ss.getDefaultEdgeStyle().toString());
            e2.setStyle("edgeStyle=elbowEdgeStyle;elbow=horizontal;"
                    + "exitX=1;exitY=0;exitPerimeter=0;entryX=0;entryY=0;entryPerimeter=5;");
            this.createVertices(graph);
        } finally {
            graph.getModel().endUpdate();
        }

        graphComponent = new mxGraphComponent(graph);
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseReleased(e);
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Object cell = graphComponent.getCellAt(e.getX(), e.getY());
                    if (cell != null && cell instanceof mxCell) {
                        if (((mxCell) cell).isEdge()) {
                            System.out.println("Edge: " + cell);
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

    /**
     * Create the menu for clicking.
     * 
     * @param e
     */
    public void showMenu(MouseEvent e) {
        Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), graphComponent);
        JPopupMenu menu = new JPopupMenu();
        menu.add(this.bind("straight", new ChangeAction("straight")));
        menu.add(this.bind("Optimal", new ChangeAction("Optimal")));
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

    public ChangeAction() {
        super();
    }

    public ChangeAction(String name) {
        super(name);
    }

    public void actionPerformed(ActionEvent e) {
        mxGraph graph = null;
        Object s = e.getSource();
        if (s instanceof mxGraphComponent) {
            graph = ((mxGraphComponent) s).getGraph();
            String name = getValue(Action.NAME).toString();
            Object[] cells = graph.getSelectionCells(); // .getSelectionModel().getCells()
            if ("Optimal".equalsIgnoreCase(name)) {
                for (Object o : cells) {
                    if (o instanceof mxCell) {
                        mxCell c = (mxCell) o;
                        this.updateRoute(c, graph);
                    }
                }
            } else if ("straight".equalsIgnoreCase(name)) {
                mxStylesheet style = graph.getStylesheet();
                graph.setCellStyle(style.getDefaultEdgeStyle().toString(), cells);
            }
        }
    }

    /**
     * Update the Edge.
     * 
     * @param cell
     * @param graph
     */
    public void updateRoute(mxCell cell, mxGraph graph) {
        if (cell.isEdge()) {
            Object[] all = graph.getChildCells(graph.getDefaultParent());
            mxICell src = cell.getSource();
            mxICell tgt = cell.getTarget();
            if (src != null && tgt != null) {
                System.out.println("All: " + all.length);
                System.out.println("Change the edge.");
                StringBuffer sbStyle = new StringBuffer();
                sbStyle.append("edgeStyle=elbowEdgeStyle;");
                sbStyle.append("elbow=horizontal;");
                sbStyle.append("exitX=0;");
                sbStyle.append("exitY=0;");
                sbStyle.append("exitPerimeter=2;");
                sbStyle.append("entryX=0;");
                sbStyle.append("entryY=0;");
                sbStyle.append("entryPerimeter=1;");
                graph.getModel().beginUpdate();
                try {
                    graph.setCellStyle(this.getPath(cell, all), new Object[] { cell });
                } finally {
                    graph.getModel().endUpdate();
                }
            }
        }
    }

    /**
     * Calculate the optimal route.
     * 
     * @param cell
     * @param allCells
     * @return
     */
    public String getPath(mxCell cell, Object[] allCells) {
        StringBuffer sbStyle = new StringBuffer();
        mxICell src = cell.getSource();
        mxICell tgt = cell.getTarget();
        mxGeometry srcg = src.getGeometry();
        mxGeometry tgtg = tgt.getGeometry();
        srcg.getCenterX();
        srcg.getCenterY();
        srcg.getWidth();
        srcg.getHeight();
        tgtg.getCenterX();
        tgtg.getCenterY();
        tgtg.getWidth();
        tgtg.getHeight();
        sbStyle.append("edgeStyle=elbowEdgeStyle;");
        sbStyle.append("elbow=horizontal;");
        sbStyle.append("exitX=" + 0.5 + ";");
        sbStyle.append("exitY=" + 0.5 + ";");
        sbStyle.append("exitPerimeter=1;");
        sbStyle.append("entryX=" + 0.5 + ";");
        sbStyle.append("entryY=" + 0.5 + ";");
        sbStyle.append("entryPerimeter=1;");
        return sbStyle.toString();
    }

}
