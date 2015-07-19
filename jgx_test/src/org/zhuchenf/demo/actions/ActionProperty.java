package org.zhuchenf.demo.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.scilab.modules.graph.ScilabComponent;
import org.scilab.modules.graph.ScilabGraph;
import org.zhuchenf.demo.utils.RouteUtils;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.view.mxCellState;

public class ActionProperty extends AbstractAction {

    private static final long serialVersionUID = 6273489688720600037L;
    private ScilabGraph graph;

    public ActionProperty() {
    }

    public ActionProperty(String name, ScilabGraph graph) {
        super(name);
        this.graph = graph;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();
        if (s instanceof ScilabComponent) {
            String name = getValue(Action.NAME).toString();
            Object[] cells = graph.getSelectionCells();
            if ("Property".equals(name)) {
                for (Object o : cells) {
                    if (o instanceof mxCell) {
                        mxCell c = (mxCell) o;
                        System.out.println(c);
                        mxGeometry g = c.getGeometry();
                        System.out.println("Geometry: " + g);
                        mxCellState state = graph.getView().getState(c);
                        if (state != null) {
                            System.out.println("State: " + state);
                        }
                        if (c.isEdge()) {
                            System.out.println("Points of Line: "
                                    + RouteUtils.getLinePoints(c));
                        }
                    }
                }
            }
        }
    }

}
