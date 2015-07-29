package org.zhuchenf.demo.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.scilab.modules.graph.ScilabComponent;
import org.scilab.modules.graph.ScilabGraph;
import org.zhuchenf.demo.utils.RouteUtils;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
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
                            System.out.println("Points of Line: " + RouteUtils.getLinePoints(c));
                        } else {
                            System.out.println("Cal Geo: " + this.getGeometry(c));
                        }
                    }
                }
            }
        }
    }

    public mxGeometry getGeometry(mxCell block) {
        double ix = 0;
        double iy = 0;
        double iw = 0;
        double ih = 0;
        mxGeometry g = block.getGeometry();
        for (int i = 0; i < block.getChildCount(); i++) {
            mxICell child = block.getChildAt(i);
            if (child.getGeometry() == null) {
                continue;
            }
            mxGeometry childGeo = new mxGeometry(child.getGeometry().getX(), child.getGeometry().getY(), child
                    .getGeometry().getWidth(), child.getGeometry().getHeight());
            if (child.getGeometry().isRelative()) {
                childGeo.setX(g.getWidth() * childGeo.getX());
                childGeo.setY(g.getHeight() * childGeo.getY());
            }
            if (childGeo.getX() < 0) {
                ix = childGeo.getX();
                iw = Math.abs(ix);
            }
            if (childGeo.getX() + childGeo.getWidth() > g.getWidth()) {
                iw += childGeo.getX() + childGeo.getWidth() - g.getWidth();
            }
            if (childGeo.getY() < 0) {
                iy = childGeo.getY();
                ih = Math.abs(iy);
            }
            if (childGeo.getY() + childGeo.getHeight() > Math.max(block.getGeometry().getHeight(), ih)) {
                ih += childGeo.getY() + childGeo.getHeight() - g.getHeight();
            }
        }
        double blockx = g.getX() + ix;
        double blocky = g.getY() + iy;
        double width = g.getWidth() + iw;
        double height = g.getHeight() + ih;
        mxGeometry geo = new mxGeometry(blockx, blocky, width, height);
        return geo;
    }

}
