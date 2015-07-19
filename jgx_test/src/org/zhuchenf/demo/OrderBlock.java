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

import org.scilab.modules.graph.ScilabComponent;
import org.scilab.modules.graph.ScilabGraph;
import org.zhuchenf.demo.actions.ActionProperty;
import org.zhuchenf.demo.actions.ActionZoom;
import org.zhuchenf.demo.actions.ChangeAction;

import com.mxgraph.analysis.mxAnalysisGraph;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

/**
 * A simple test for Clicking to order all the blocks.
 * 
 * @author Chenfeng ZHU
 *
 */
public class OrderBlock extends JFrame {

    private static final long serialVersionUID = 8452610294410264983L;

    private ScilabComponent graphComponent;
    private ScilabGraph graph;
    private mxAnalysisGraph aGraph = new mxAnalysisGraph();

    private Random r = new Random(System.currentTimeMillis());
    private List<Object> listVertex = new ArrayList<>(0);

    public OrderBlock() {
        super("OptimizeRoute");

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
            mxCell v1 = (mxCell) graph.insertVertex(parent, null, "zhu", 20, 120, 80, 30,
                    "zhu_style");
            v1.setConnectable(false);
            mxGeometry geo = graph.getModel().getGeometry(v1);
            geo.setAlternateBounds(new mxRectangle(20, 20, 80, 30));
            mxGeometry geo1 = new mxGeometry(1.0, 0.5, 5, 5);
            // geo1.setOffset(new mxPoint(0, 0));
            geo1.setRelative(true);
            mxCell port1 = new mxCell("P1", geo1, "shape=ellipse;perimter=ellipsePerimeter");
            port1.setVertex(true);
            graph.addCell(port1, v1);
            mxCell v2 = (mxCell) graph
                    .insertVertex(parent, null, "chenfeng", 340, 120, 80, 30);
            v2.setConnectable(false);
            mxGeometry geo2 = new mxGeometry(0, 0.5, 2, 2);
            // geo2.setOffset(new mxPoint(0, 0));
            geo2.setRelative(true);
            mxCell port2 = new mxCell("P2", geo2, "");
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
                            mxGeometry g = ((mxCell) cell).getGeometry();
                            System.out.println("Edge Geometry: (" + g.getX() + ", " + g.getY()
                                    + "), (" + g.getWidth() + ", " + g.getHeight() + ")");
                            aGraph.setGraph(graph);
                            // System.out.println(mxGraphStructure.isCutEdge(aGraph,
                            // cell));
                        }
                        showMenu(e);
                    }
                }
            }
        });
        getContentPane().add(graphComponent);

        // mxIGraphLayout layout = new mxFastOrganicLayout(graph);
        // graph.getModel().beginUpdate();
        // try {
        // layout.execute(graph.getDefaultParent());
        // } finally {
        // graph.getModel().endUpdate();
        // }
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
    private String strZoomIn = "ZoomIn";
    private String strZoomOut = "ZoomOut";
    private String strZoomDefault = "ZoomDefault";
    private String strProperty = "Property";

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
        menu.addSeparator();
        menu.add(this.bind(strZoomIn, new ActionZoom(strZoomIn, graph)));
        menu.add(this.bind(strZoomDefault, new ActionZoom(strZoomDefault, graph)));
        menu.add(this.bind(strZoomOut, new ActionZoom(strZoomOut, graph)));
        menu.addSeparator();
        menu.add(this.bind(strProperty, new ActionProperty(strProperty, graph)));
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
        OrderBlock frame = new OrderBlock();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
}
