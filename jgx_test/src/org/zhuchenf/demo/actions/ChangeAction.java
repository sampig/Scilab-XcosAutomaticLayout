package org.zhuchenf.demo.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.scilab.modules.graph.ScilabComponent;
import org.scilab.modules.graph.ScilabGraph;
import org.zhuchenf.demo.utils.RouteUtils;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxConstants;

public class ChangeAction extends AbstractAction {

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
                graph.getModel().beginUpdate();
                try {
                    double scale = graph.getView().getScale();
                    graph.getView().setScale(1.0);
                    graph.setCellStyle(null, cells);
                    this.updateLinkOptimal(cells);
                    graph.getView().setScale(scale);
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
            System.out.println("H: " + ((mxCell) cells[0]).getGeometry().getPoints());
            graph.resetEdges(cells);
        } finally {
            graph.getModel().endUpdate();
        }
    }

    public void updateLinkOptimal(Object[] cells) {
        Object[] all = graph.getChildCells(graph.getDefaultParent());
        // graph.getChildVertices(graph.getDefaultParent());
        RouteUtils route = new RouteUtils();
        for (Object o : cells) {
            if (o instanceof mxCell) {
                mxCell c = (mxCell) o;
                if (c.isEdge()) {
                    route.updateRoute(c, all, graph);
                }
            }
        }
    }

}
