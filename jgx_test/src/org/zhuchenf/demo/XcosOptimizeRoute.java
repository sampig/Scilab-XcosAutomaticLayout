package org.zhuchenf.demo;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import org.scilab.modules.graph.ScilabComponent;
import org.scilab.modules.graph.ScilabGraph;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxStylesheet;

/**
 * A simple test for Clicking to find an Optimal Route.
 * 
 * @author Chenfeng ZHU
 *
 */
public class XcosOptimizeRoute extends JFrame {

    private static final long serialVersionUID = 8452610294410264983L;

    private ScilabComponent graphComponent;
    private ScilabGraph graph;

    private Random r = new Random(System.currentTimeMillis());
    private List<Object> listVertex = new ArrayList<>(0);

    public XcosOptimizeRoute() {
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
            Object v1 = graph.insertVertex(parent, null, "zhu", 20, 20, 80, 30, "zhu_style");
            Object v2 = graph.insertVertex(parent, null, "chenfeng", 340, 350, 80, 30);
            graph.insertEdge(parent, null, "comma", v1, v2);
            graph.insertEdge(parent, null, "null", null, null);
        } finally {
            graph.getModel().endUpdate();
        }

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        getContentPane().add(graphComponent);
    }

    public static void main(String... strings) {
        Test2Block frame = new Test2Block();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
}
