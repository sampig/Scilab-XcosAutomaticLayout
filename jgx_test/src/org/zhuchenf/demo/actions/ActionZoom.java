package org.zhuchenf.demo.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.scilab.modules.graph.ScilabGraph;

public class ActionZoom extends AbstractAction {

    private static final long serialVersionUID = -7315184278600529769L;

    private ScilabGraph graph;

    public ActionZoom() {
        super();
    }

    public ActionZoom(String name, ScilabGraph graph) {
        super(name);
        this.graph = graph;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = getValue(Action.NAME).toString();
        if ("ZoomIn".equalsIgnoreCase(name)) {
            // System.out.println(graph.getAsComponent().getZoomFactor());
            System.out.println("Scale orginal:" + graph.getView().getScale());
            graph.getAsComponent().zoomIn();
            System.out.println("Scale:" + graph.getView().getScale());
        } else if ("ZoomOut".equalsIgnoreCase(name)) {
            System.out.println("Scale orginal:" + graph.getView().getScale());
            graph.getAsComponent().zoomOut();
            System.out.println("Scale:" + graph.getView().getScale());
        } else if ("ZoomDefault".equalsIgnoreCase(name)) {
            System.out.println("Scale orginal:" + graph.getView().getScale());
            graph.getAsComponent().zoomTo(1, true);
            System.out.println("Scale:" + graph.getView().getScale());
        }
    }

}
