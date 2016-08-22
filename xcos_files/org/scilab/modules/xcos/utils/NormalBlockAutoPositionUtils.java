/*
 * Scilab ( http://www.scilab.org/ ) - This file is part of Scilab
 * Copyright (C) 2016 - Chenfeng ZHU
 *
 * This file must be used under the terms of the CeCILL.
 * This source file is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at
 * http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.txt
 *
 */
package org.scilab.modules.xcos.utils;

import java.util.ArrayList;
import java.util.List;

import org.scilab.modules.graph.ScilabGraph;
import org.scilab.modules.xcos.block.BasicBlock;
import org.scilab.modules.xcos.block.SplitBlock;
import org.scilab.modules.xcos.block.TextBlock;
import org.scilab.modules.xcos.graph.XcosDiagram;
import org.scilab.modules.xcos.graph.swing.handler.SelectionCellsHandler;
import org.scilab.modules.xcos.link.BasicLink;
import org.scilab.modules.xcos.port.BasicPort;
import org.scilab.modules.xcos.port.Orientation;
import org.scilab.modules.xcos.port.command.CommandPort;
import org.scilab.modules.xcos.port.control.ControlPort;
import org.scilab.modules.xcos.port.input.ImplicitInputPort;
import org.scilab.modules.xcos.port.input.InputPort;
import org.scilab.modules.xcos.port.output.ImplicitOutputPort;
import org.scilab.modules.xcos.port.output.OutputPort;
import org.scilab.modules.xcos.preferences.XcosOptions;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;

/**
 * Provide methods to set the new position for normal Block.
 */
public abstract class NormalBlockAutoPositionUtils {

    /**
     * The distance for a block away to another block.
     */
    public final static int DEFAULT_BEAUTY_BLOCKS_DISTANCE = 40;

    public final static int DEFAULT_ALIGNED_BLOCKS_DISTANCE = 20;

    /**
     * The simple type of port.
     */
    public enum PortType {IN, OUT};

    /**
     * The type of connection.
     */
    public enum ConnectType {FLAT, TREE, REVTREE, CYCLED, OTHER};

    /**
     * Change the position of the Normal Blocks:<br/>
     * <ol>
     * <li>Firstly, deal with connected blocks.</li>
     * <li>Secondly, deal with non-start/end blocks.</li>
     * <li>Thirdly, deal with start/end blocks. (1: Change the blocks which connect to a same block; 2: Change the
     * single blocks. )</li>
     * </ol>
     *
     * @param graph
     * @param selectedCells
     *            all selected cells
     */
    public static void changeNormalBlockPosition(XcosDiagram graph, Object[] selectedCells) {
        Object[] all = graph.getChildCells(graph.getDefaultParent());

        // only normal blocks will be chosen.
        List<BasicBlock> listBlocks = new ArrayList<>(0);
        for (Object o : selectedCells) {
            if (isNormalBlock(o)) {
                BasicBlock basicblock = (BasicBlock) o;
                listBlocks.add(basicblock);
            }
        }

        // Firstly, deal with connected blocks.
        List<List<BasicBlock>> listBlocksConnected = getSeletecedConnectedBlocks(listBlocks);
        List<BasicBlock> listConnected = new ArrayList<>(0);
        for (List<BasicBlock> blocksConnected : listBlocksConnected) {
            changeConnectedBlocksPosition(blocksConnected, all, graph);
            listConnected.addAll(blocksConnected);
        }

        // Secondly, deal with non-start/end blocks.
        BasicBlock[] blocksNonstart = reorderSelectedNonstartBlocks(listBlocks);
        for (BasicBlock block : blocksNonstart) {
            if (!listConnected.contains(block)) {
                changeNonstartBlockPosition(block, all, graph);
            }
        }

        // Thirdly, deal with start/end blocks.
        List<BasicBlock> blocksStartEnd = reorderSelectedStartEndBlocks(listBlocks);
        changeStartEndBlocksPosition(blocksStartEnd, listConnected, all, graph);
    }

    /**
     * Reorder all the start/end selected blocks.
     *
     * @param blocks
     *            all the selected blocks
     * @return all start/end blocks in order
     */
    private static List<BasicBlock> reorderSelectedStartEndBlocks(List<BasicBlock> blocks) {
        List<BasicBlock> listStartEnd = new ArrayList<>(0);
        for (BasicBlock basicblock : blocks) {
            if (isStartBlock(basicblock) || isEndBlock(basicblock)) {
                listStartEnd.add(basicblock);
            }
        }
        reorderBlocks(listStartEnd);
        return listStartEnd;
    }

    /**
     * Reorder all the non start/end selected blocks.
     *
     * @param cells
     *            all the selected blocks
     * @return all non start/end blocks in order
     */
    private static BasicBlock[] reorderSelectedNonstartBlocks(List<BasicBlock> blocks) {
        BasicBlock[] newBlocks = null;
        List<BasicBlock> listNonstart = new ArrayList<>(0);
        for (BasicBlock basicblock : blocks) {
            if (!isStartBlock(basicblock) && !isEndBlock(basicblock)) {
                listNonstart.add(basicblock);
            }
        }
        newBlocks = listNonstart.toArray(new BasicBlock[0]);
        reorderBlocks(newBlocks);
        return newBlocks;
    }

    /**
     *
     * Reorder the selected blocks: <br/>
     * <ol>
     * <li>From left to right. (From up to down.)</li>
     * </ol>
     *
     * @param blocks
     *            all blocks
     */
    private static void reorderBlocks(List<BasicBlock> blocks) {
        BasicBlock tmp;
        int l = blocks.size();
        for (int i = 0; i < l - 1; i++) {
            for (int j = l - 1; j > i; j--) {
                if (isRightBelow(blocks.get(j - 1), blocks.get(j))) {
                    // if blocks[j] is on the right (down) of blocks[j - 1],
                    tmp = blocks.get(j);
                    blocks.set(j, blocks.get(j - 1));
                    blocks.set(j - 1, tmp);
                }
            }
        }
    }

    /**
     *
     * Reorder the selected blocks: <br/>
     * <ol>
     * <li>From left to right. (From up to down.)</li>
     * </ol>
     *
     * @param blocks
     *            all blocks
     */
    private static void reorderBlocks(BasicBlock[] blocks) {
        BasicBlock tmp;
        int l = blocks.length;
        for (int i = 0; i < l - 1; i++) {
            for (int j = l - 1; j > i; j--) {
                if (isRightBelow(blocks[j - 1], blocks[j])) {
                    // if blocks[j] is on the right (down) of blocks[j - 1],
                    tmp = blocks[j];
                    blocks[j] = blocks[j - 1];
                    blocks[j - 1] = tmp;
                }
            }
        }
    }

    /**
     * Get a list which contains the list of connected blocks.
     *
     * @param blocks
     *            all the blocks
     * @return
     */
    private static List<List<BasicBlock>> getSeletecedConnectedBlocks(List<BasicBlock> blocks) {
        List<List<BasicBlock>> list = new ArrayList<>(0);
        List<BasicBlock> listChosen = new ArrayList<>(0);
        for (BasicBlock basicblock : blocks) {
            if (!listChosen.contains(basicblock)) {
                List<BasicBlock> listConnected = new ArrayList<>(0);
                listConnected.add(basicblock);
                addAllConnectedBlocks(basicblock, blocks, listConnected);
                if (listConnected.size() > 1) {
                    list.add(listConnected);
                }
                listChosen.addAll(listConnected);
            }
        }
        return list;
    }

    /**
     * Add blocks which connects to basicblock directly or indirectly into list.
     *
     * @param basicblock
     *            the block
     * @param blocks
     *            all the blocks
     * @param list
     *            a list of blocks which connects to basicblock
     */
    private static void addAllConnectedBlocks(BasicBlock basicblock, List<BasicBlock> blocks, List<BasicBlock> list) {
        for (BasicBlock block : blocks) {
            if (block == basicblock || list.contains(block)) {
                continue;
            }
            if (isBlocksConnected(basicblock, block)) {
                list.add(block);
                addAllConnectedBlocks(block, blocks, list);
            }
        }
    }

    /**
     * Check if the cell is a normal block.
     *
     * @param cell
     *            the cell
     * @return <b>true</b> if it is a normal block
     */
    private static boolean isNormalBlock(Object cell) {
        if (cell instanceof BasicBlock && !(cell instanceof SplitBlock) && !(cell instanceof TextBlock)) {
            return true;
        }
        return false;
    }

    /**
     * Check if the block is a start block (which has only 1 OUT port).
     *
     * @param basicblock
     *            the block
     * @return <b>true</b> if it is a start block
     */
    private static boolean isStartBlock(BasicBlock basicblock) {
        if (isOutBlock(basicblock) && getOutPortNum(basicblock) == 1) {
            return true;
        }
        return false;
    }

    /**
     * Check if the block is an end block (which has only 1 IN port).
     *
     * @param basicblock
     *            the block
     * @return <b>true</b> if it is an end block
     */
    private static boolean isEndBlock(BasicBlock basicblock) {
        if (isInBlock(basicblock) && getInPortNum(basicblock) == 1) {
            return true;
        }
        return false;
    }

    /**
     * Check if the block has only OutputPort/CommandPort and no InputPort/ControlPort.
     *
     * @param block
     *            the block
     * @return <b>true</b> if it has only output
     */
    private static boolean isOutBlock(BasicBlock block) {
        if (getOutPortNum(block) > 0 && getInPortNum(block) == 0) {
            return true;
        }
        return false;
    }

    /**
     * Check if the block has only InputPort/ControlPort and no OutputPort/CommandPort.
     *
     * @param block
     *            the block
     * @return <b>true</b> if it has only input
     */
    private static boolean isInBlock(BasicBlock block) {
        if (getInPortNum(block) > 0 && getOutPortNum(block) == 0) {
            return true;
        }
        return false;
    }

    /**
     * Check if the basic block contains a CommandPort, a ControlPort, a ImplicitInputPort or a ImplicitOutputPort.
     *
     * @param basicblock
     *            the basic block
     * @return <b>true</b> if it does NOT contain any
     */
    private static boolean isExplicitBlock(BasicBlock basicblock) {
        if (containCommandControlPort(basicblock) || containImplicitPort(basicblock)) {
            return false;
        }
        return true;
    }

    /**
     * Check if the basic block contains a CommandPort or a ControlPort.
     *
     * @param basicblock
     *            the basic block
     * @return <b>true</b> if it contains
     */
    private static boolean containCommandControlPort(BasicBlock basicblock) {
        for (int i = 0; i < basicblock.getChildCount(); i++) {
            mxICell port = basicblock.getChildAt(i);
            if ((port instanceof CommandPort) || (port instanceof ControlPort)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the basic block contains a ImplicitInputPort or a ImplicitOutputPort.
     *
     * @param basicblock
     *            the basic block
     * @return <b>true</b> if it contains
     */
    private static boolean containImplicitPort(BasicBlock basicblock) {
        for (int i = 0; i < basicblock.getChildCount(); i++) {
            mxICell port = basicblock.getChildAt(i);
            if ((port instanceof ImplicitInputPort) || (port instanceof ImplicitOutputPort)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the total number of OutputPort/CommandPort in the block.
     *
     * @param block
     *            the block
     * @return the number of out port
     */
    private static int getOutPortNum(BasicBlock block) {
        int count = 0;
        for (int i = 0; i < block.getChildCount(); i++) {
            mxICell port = block.getChildAt(i);
            if ((port instanceof OutputPort) || (port instanceof CommandPort)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get the total number of InputPort/ControlPort in the block.
     *
     * @param block
     *            the block
     * @return the number of in port
     */
    private static int getInPortNum(BasicBlock block) {
        int count = 0;
        for (int i = 0; i < block.getChildCount(); i++) {
            mxICell port = block.getChildAt(i);
            if ((port instanceof InputPort) || (port instanceof ControlPort)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Change the position of the blocks which are connected.
     * <ol>
     * <li>Hierarchical Flat connection: b --> b --> b</li>
     * <li>Tree connection.</li>
     * <li>Reversed Tree connection.</li>
     * <li>Cycled connection.</li>
     * </ol>
     *
     * @param listConnected
     *            the list of the connected blocks
     * @param all
     *            all the cells in graph
     * @param graph
     */
    private static void changeConnectedBlocksPosition(List<BasicBlock> listConnected, Object[] all, XcosDiagram graph) {
        // TODO: should consider SplitBlock.
        int blocksDistance = (XcosOptions.getEdition().getGraphBlockDistance() <= 0) ? DEFAULT_BEAUTY_BLOCKS_DISTANCE
                : XcosOptions.getEdition().getGraphBlockDistance();
        BasicBlock startBlock = chooseStartBlock(listConnected);
        ConnectType connectType = checkConnectType(listConnected);
        switch (connectType) {
            case FLAT: // Hierarchical Flat connection
                changeNonstartBlockPosition(startBlock, all, graph);
                reorderFlatBlocks(listConnected, startBlock);
                mxPoint position = new mxPoint();
                for (int i = 1; i < listConnected.size(); i++) {
                    BasicBlock block = listConnected.get(i);
                    BasicBlock previous = listConnected.get(i - 1);
                    if (block == startBlock) {
                        continue;
                    }
                    BasicPort port = getSelfPort(previous, block);
                    Orientation orien = XcosRouteUtils.getPortOrientation(port);
                    switch (orien) {
                        case EAST:
                            position.setX(previous.getGeometry().getX() + previous.getGeometry().getWidth()
                                    + blocksDistance + block.getGeometry().getWidth() / 2);
                            position.setY(previous.getGeometry().getCenterY());
                            break;
                        case WEST:
                            position.setX(previous.getGeometry().getX() - blocksDistance
                                    - block.getGeometry().getWidth() / 2);
                            position.setY(previous.getGeometry().getCenterY());
                            break;
                        case NORTH:
                            position.setX(previous.getGeometry().getCenterX());
                            position.setY(previous.getGeometry().getY() - blocksDistance
                                    - block.getGeometry().getHeight() / 2);
                            break;
                        case SOUTH:
                            position.setX(previous.getGeometry().getCenterX());
                            position.setY(previous.getGeometry().getY() + previous.getGeometry().getHeight()
                                    + blocksDistance + block.getGeometry().getHeight() / 2);
                            break;
                    }
                    moveBlock(block, position, graph);
                }
                break;
            case TREE:
                break;
            case REVTREE:
                break;
            case CYCLED:
                break;
            case OTHER:
                break;
        }

        // TODO: Is it necessary to change the link to straight?
        // update the link.
        for (BasicBlock block : listConnected) {
            resetLink(block, graph);
        }
    }

    /**
     * Check the type of the connected blocks.
     *
     * @param listConnected
     *            the list of the connected blocks
     * @return
     */
    private static ConnectType checkConnectType(List<BasicBlock> listConnected) {
        // if every block has 0/1 IN connected block in the list and 0/1 OUT connected block in the list,
        int i = 0;
        for (; i < listConnected.size(); i++) {
            BasicBlock block = listConnected.get(i);
            List<BasicBlock> listIn = getConnectedBlocksInOut(block, PortType.IN);
            int numIn = 0;
            for (BasicBlock b : listIn) {
                if (listConnected.contains(b)) {
                    numIn++;
                }
            }
            if (numIn > 1) {
                break;
            }
            List<BasicBlock> listOut = getConnectedBlocksInOut(block, PortType.OUT);
            int numOut = 0;
            for (BasicBlock b : listOut) {
                if (listConnected.contains(b)) {
                    numOut++;
                }
            }
            if (numOut > 1) {
                break;
            }
        }
        if (i == listConnected.size()) {
            return ConnectType.FLAT;
        }
        for (i = 0; i < listConnected.size(); i++) {
            BasicBlock block = listConnected.get(i);
            List<BasicBlock> listIn = getConnectedBlocksInOut(block, PortType.IN);
            int numIn = 0;
            for (BasicBlock b : listIn) {
                if (listConnected.contains(b)) {
                    numIn++;
                }
            }
            if (numIn > 1) {
                break;
            }
        }
        if (i == listConnected.size()) {
            return ConnectType.TREE;
        }
        return ConnectType.OTHER;
    }

    /**
     * Choose a start block from the list.
     * <ol>
     * <li>If it is a out block,</li>
     * <li>If it is an explicit block and there are no blocks in the list which is its parent,</li>
     * <li>If it contains a Command/Control port and there are no blocks in the list which is its parent,</li>
     * </ol>
     *
     * @param listConnected
     *            list of blocks
     * @return
     */
    private static BasicBlock chooseStartBlock(List<BasicBlock> listConnected) {
        BasicBlock left = null;
        for (BasicBlock block : listConnected) {
            if (isOutBlock(block)) {
                return block;
            } else if (isExplicitBlock(block) || containCommandControlPort(block)) {
                List<BasicBlock> listIn = getConnectedBlocksInOut(block, PortType.IN);
                int numIn = 0;
                for (BasicBlock b : listIn) {
                    if (listConnected.contains(b)) {
                        numIn++;
                    }
                }
                if (numIn == 0) {
                    return block;
                }
            }
            // choose the one on the most left
            if (left == null || left.getGeometry().getX() > block.getGeometry().getX()) {
                left = block;
            }
        }
        return left;
    }

    /**
     * Reorder the list of blocks from the start block.
     *
     * @param list
     *            the list of blocks
     * @param start
     *            the start block
     */
    private static void reorderFlatBlocks(List<BasicBlock> list, BasicBlock start) {
        BasicBlock tmp;
        int l = list.size();
        for (int i = 0; i < l; i++) {
            if (list.get(i) == start) {
                tmp = list.get(0);
                list.set(0, list.get(i));
                list.set(i, tmp);
                break;
            }
        }
        for (int i = 0; i < l - 1; i++) {
            for (int j = l - 1; j > i; j--) {
                if (isBlocksConnected(list.get(i), list.get(j))) {
                    // if 2 blocks are connected
                    tmp = list.get(i + 1);
                    list.set(i + 1, list.get(j));
                    list.set(j, tmp);
                }
            }
        }
    }

    private static boolean isStartBlock(BasicBlock block, List<BasicBlock> listBlocks) {
        List<BasicBlock> listChildren = getConnectedBlocks(block, null);
        for (BasicBlock child: listChildren) {
            return true;
        }
        return false;
    }

    private static int getDepth(List<BasicBlock> listConnected) {
        return 0;
    }

    /**
     * Get the directly connected blocks.
     *
     * @param basicblock
     *            the basic block
     * @param exclusion
     *            the exclusion block
     * @return
     */
    private static List<BasicBlock> getConnectedBlocks(BasicBlock basicblock, BasicBlock exclusion) {
        List<BasicBlock> list = new ArrayList<>(0);
        for (int i = 0; i < basicblock.getChildCount(); i++) {
            mxICell port = basicblock.getChildAt(i);
            if (port instanceof BasicPort) {
                BasicBlock other = getConnectedBlock(basicblock, (BasicPort) port);
                if (other == exclusion) {
                    continue;
                }
                list.add(other);
            }
        }
        return list;
    }

    /**
     * Get all IN/OUT directly connected blocks.
     *
     * @param basicblock
     *            the basic block
     * @param type
     *            IN or OUT
     * @return
     */
    private static List<BasicBlock> getConnectedBlocksInOut(BasicBlock basicblock, PortType type) {
        List<BasicBlock> list = new ArrayList<>(0);
        switch (type) {
            case IN:
                for (int i = 0; i < basicblock.getChildCount(); i++) {
                    mxICell port = basicblock.getChildAt(i);
                    if ((port instanceof InputPort) || (port instanceof ControlPort)) {
                        list.add(getConnectedBlock(basicblock, (BasicPort) port));
                    }
                }
                break;
            case OUT:
                for (int i = 0; i < basicblock.getChildCount(); i++) {
                    mxICell port = basicblock.getChildAt(i);
                    if ((port instanceof OutputPort) || (port instanceof CommandPort)) {
                        list.add(getConnectedBlock(basicblock, (BasicPort) port));
                    }
                }
                break;
        }
        return list;
    }

    /**
     * Move the block to the new position (the position is the center point of the block).
     *
     * @param basicblock
     *            the basic block
     * @param newPosition
     *            the new position
     * @param graph
     */
    private static void moveBlock(BasicBlock basicblock, mxPoint newPosition, XcosDiagram graph) {
        mxGeometry blockGeo = (mxGeometry) basicblock.getGeometry().clone();
        blockGeo.setX(newPosition.getX() - blockGeo.getWidth() / 2);
        blockGeo.setY(newPosition.getY() - blockGeo.getHeight() / 2);
        graph.getModel().setGeometry(basicblock, blockGeo);
    }

    /**
     * Change the position of the block.
     * <ol>
     * <li>Move to right if blocks are too closed horizontally.</li>
     * <li>Move downwards if blocks are too closed vertically.</li>
     * </ol>
     *
     * @param basicblock
     *            the block
     * @param all
     *            all the cells in graph
     * @param graph
     */
    private static void changeNonstartBlockPosition(BasicBlock basicblock, Object[] all, XcosDiagram graph) {
        List<BasicBlock> listBlocks = new ArrayList<>(0);
        for (Object o : all) {
            if (isNormalBlock(o) && o != basicblock) {
                listBlocks.add((BasicBlock) o);
            }
        }

        int blocksDistance = (XcosOptions.getEdition().getGraphBlockDistance() <= 0) ? DEFAULT_BEAUTY_BLOCKS_DISTANCE
                : XcosOptions.getEdition().getGraphBlockDistance();
        for (int i = 0; i < listBlocks.size(); i++) {
            if (listBlocks.get(i) == basicblock) {
                continue;
            }
            BasicBlock block = listBlocks.get(i);
            if (isBlocksSuperimposition(basicblock, block)) {
                List<BasicBlock> listRight = getAllRightBlocks(basicblock, listBlocks);
                blocksDistance = blocksDistance(block, basicblock, Orientation.WEST);
                moveBlock(basicblock, blocksDistance, Orientation.EAST, graph);
                moveBlocks(listRight, blocksDistance, Orientation.EAST, graph);
            } else if (isBlocksSuperimposition(block, basicblock)) {
                List<BasicBlock> listBlockRight = getAllRightBlocks(block, listBlocks);
                blocksDistance = blocksDistance(basicblock, block, Orientation.EAST);
                moveBlock(block, blocksDistance, Orientation.EAST, graph);
                moveBlocks(listBlockRight, blocksDistance, Orientation.EAST, graph);
            } else if (isBlocksClosed(basicblock, block)) {
                Orientation position = blockOrientation(basicblock, block);
                switch (position) {
                    case WEST: // Left: if the closed block is on the left of the basicblock, move the basicblock to right
                        if (hasPortOrientation(block, Orientation.EAST) || hasPortOrientation(basicblock, Orientation.WEST)) {
                            // if they have ports in this direction,
                            List<BasicBlock> listRight = getAllRightBlocks(basicblock, listBlocks);
                            blocksDistance = blocksDistance(block, basicblock, position);
                            moveBlock(basicblock, blocksDistance, Orientation.EAST, graph);
                            moveBlocks(listRight, blocksDistance, Orientation.EAST, graph);
                        }
                        break;
                    case NORTH: // Up: if the closed block is above the basicblock, move the basicblock downwards
                        if (hasPortOrientation(block, Orientation.SOUTH) || hasPortOrientation(basicblock, Orientation.NORTH)) {
                            List<BasicBlock> listDown = getAllDownBlocks(basicblock, listBlocks);
                            blocksDistance = blocksDistance(block, basicblock, position);
                            moveBlock(basicblock, blocksDistance, Orientation.SOUTH, graph);
                            moveBlocks(listDown, blocksDistance, Orientation.SOUTH, graph);
                        }
                        break;
                    case EAST: // Right: if the closed block is on the right of the basicblock, move the block to right
                        if (hasPortOrientation(block, Orientation.WEST) || hasPortOrientation(basicblock, Orientation.EAST)) {
                            List<BasicBlock> listBlockRight = getAllRightBlocks(block, listBlocks);
                            blocksDistance = blocksDistance(basicblock, block, position);
                            moveBlock(block, blocksDistance, Orientation.EAST, graph);
                            moveBlocks(listBlockRight, blocksDistance, Orientation.EAST, graph);
                        }
                        break;
                    case SOUTH: // Down: if the closed block is under the basicblock, move the block downwards
                        if (hasPortOrientation(block, Orientation.NORTH) || hasPortOrientation(basicblock, Orientation.SOUTH)) {
                            List<BasicBlock> listDown = getAllDownBlocks(block, listBlocks);
                            blocksDistance = blocksDistance(basicblock, block, position);
                            moveBlock(block, blocksDistance, Orientation.SOUTH, graph);
                            moveBlocks(listDown, blocksDistance, Orientation.SOUTH, graph);
                        }
                        break;
                }
            }
        }
    }

    /**
     * Get all blocks which are on the right of the basicblock.
     *
     * @param basicblock
     *            the block
     * @param listBlocks
     *            all normal blocks
     * @return
     */
    private static List<BasicBlock> getAllRightBlocks(BasicBlock basicblock, List<BasicBlock> listBlocks) {
        mxGeometry geometry = basicblock.getGeometry();
        List<BasicBlock> listRight = new ArrayList<>(0);
        for (Object o : listBlocks) {
            if (isNormalBlock(o) && basicblock != o) {
                BasicBlock block = (BasicBlock) o;
                mxGeometry geometry2 = block.getGeometry();
                if (geometry2.getX() > geometry.getX()) {
                    listRight.add(block);
                }
            }
        }
        return listRight;
    }

    /**
     * Get all blocks which are under the basicblock.
     *
     * @param basicblock
     *            the block
     * @param listBlocks
     *            all normal blocks
     * @return
     */
    private static List<BasicBlock> getAllDownBlocks(BasicBlock basicblock, List<BasicBlock> listBlocks) {
        mxGeometry geometry = basicblock.getGeometry();
        List<BasicBlock> listRight = new ArrayList<>(0);
        for (Object o : listBlocks) {
            if (isNormalBlock(o) && basicblock != o) {
                BasicBlock block = (BasicBlock) o;
                mxGeometry geometry2 = block.getGeometry();
                if (geometry2.getY() > geometry.getY()) {
                    listRight.add(block);
                }
            }
        }
        return listRight;
    }

    /**
     * Check if two blocks are too closed.
     *
     * @param block1
     *            the first block
     * @param block2
     *            the second block
     * @return
     */
    private static boolean isBlocksClosed(BasicBlock block1, BasicBlock block2) {
        int blocksDistance = (XcosOptions.getEdition().getGraphBlockDistance() <= 0) ? DEFAULT_BEAUTY_BLOCKS_DISTANCE
                : XcosOptions.getEdition().getGraphBlockDistance();

        // get the 4 vertices of each block
        mxPoint[] points1 = new mxPoint[4];
        mxGeometry geo1 = block1.getGeometry();
        mxPoint[] points2 = new mxPoint[4];
        mxGeometry geo2 = block2.getGeometry();
        points1[0] = new mxPoint(geo1.getX(), geo1.getY());
        points1[1] = new mxPoint(geo1.getX() + geo1.getWidth(), geo1.getY());
        points1[2] = new mxPoint(geo1.getX() + geo1.getWidth(), geo1.getY() + geo1.getHeight());
        points1[3] = new mxPoint(geo1.getX(), geo1.getY() + geo1.getHeight());
        points2[0] = new mxPoint(geo2.getX(), geo2.getY());
        points2[1] = new mxPoint(geo2.getX() + geo2.getWidth(), geo2.getY());
        points2[2] = new mxPoint(geo2.getX() + geo2.getWidth(), geo2.getY() + geo2.getHeight());
        points2[3] = new mxPoint(geo2.getX(), geo2.getY() + geo2.getHeight());

        for (int i = 0; i < points1.length; i++) {
            for (int j = 0; j < points2.length; j++) {
                double x1 = points1[i].getX();
                double x2 = points2[j].getX();
                double y1 = points1[i].getY();
                double y2 = points2[j].getY();
                if (Math.abs(x1 - x2) <= blocksDistance && Math.abs(y1 - y2) <= blocksDistance) {
                    // if any two of these points are too closed,
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if two blocks are superimposition and the left 2 vertices of the first block are inside the second one.
     *
     * @param block1
     *            the first block
     * @param block2
     *            the second block
     * @return
     */
    private static boolean isBlocksSuperimposition(BasicBlock block1, BasicBlock block2) {
        // get the left 2 vertices of the first block
        mxPoint[] points1 = new mxPoint[2];
        mxGeometry geo1 = block1.getGeometry();
        points1[0] = new mxPoint(geo1.getX(), geo1.getY());
        points1[1] = new mxPoint(geo1.getX(), geo1.getY() + geo1.getHeight());

        for (int i = 0; i < points1.length; i++) {
            if (XcosRouteUtils.checkPointInBlock(points1[i].getX(), points1[i].getY(), block2, true)) {
                // if any point is in the other block,
                return true;
            }
        }
        return false;
    }

    /**
     * Calculate the distance the block should be moved according to the relative distance between it and blockBase.
     *
     * @param blockBase
     *            the base block
     * @param block
     *            the block
     * @param orientation
     *            relative position of block to blockBase
     * @return the distance
     */
    private static int blocksDistance(BasicBlock blockBase, BasicBlock block, Orientation orientation) {
        int blocksDistance = (XcosOptions.getEdition().getGraphBlockDistance() <= 0) ? DEFAULT_BEAUTY_BLOCKS_DISTANCE
                : XcosOptions.getEdition().getGraphBlockDistance();
        mxGeometry geoBase = blockBase.getGeometry();
        mxGeometry geoBlock = block.getGeometry();
        double xBase = geoBase.getX();
        double yBase = geoBase.getY();
        double xBlock = geoBlock.getX();
        double yBlock = geoBlock.getY();
        if (orientation == Orientation.EAST || orientation == Orientation.WEST) {
            xBase += geoBase.getWidth();
            blocksDistance -= (xBlock - xBase);
        } else {
            yBase += geoBase.getHeight();
            blocksDistance -= (yBlock - yBase);
        }
        return blocksDistance;
    }

    /**
     * Get the relative position of block to blockBase. <br/>
     * <ol>
     * <li>EAST: RIGHT</li>
     * <li>WEST: LEFT</li>
     * <li>SOUTH: DOWN</li>
     * <li>NORTH: UP</li>
     * </ol>
     *
     * @param blockBase
     *            the base block
     * @param block
     *            the block
     * @return
     */
    private static Orientation blockOrientation(BasicBlock blockBase, BasicBlock block) {
        mxGeometry geoBase = blockBase.getGeometry();
        mxGeometry geoBlock = block.getGeometry();
        double xBase = geoBase.getX();
        double yBase = geoBase.getY();
        double xBlock = geoBlock.getX();
        double yBlock = geoBlock.getY();
        double x = xBlock - xBase;
        double y = yBlock - yBase;

        if (x >= Math.abs(y)) {
            return Orientation.EAST;
        } else if (x <= -Math.abs(y)) {
            return Orientation.WEST;
        } else if (y >= Math.abs(x)) {
            return Orientation.SOUTH;
        } else if (y <= -Math.abs(x)) {
            return Orientation.NORTH;
        }
        return Orientation.EAST;
    }

    /**
     * Move all blocks.
     *
     * @param listBlocks
     *            a list of blocks
     * @param distance
     *            distance to move
     * @param orientation
     *            orientation to move
     * @param graph
     */
    private static void moveBlocks(List<BasicBlock> listBlocks, int distance, Orientation orientation, XcosDiagram graph) {
        for (BasicBlock basicblock : listBlocks) {
            moveBlock(basicblock, distance, orientation, graph);
        }
    }

    /**
     * Move a block.
     *
     * @param basicblock
     *            the block
     * @param distance
     *            distance to move
     * @param orientation
     *            orientation to move
     * @param graph
     */
    private static void moveBlock(BasicBlock basicblock, int distance, Orientation orientation, XcosDiagram graph) {
        mxGeometry blockGeo = (mxGeometry) basicblock.getGeometry().clone();
        //(mxGeometry) graph.getModel().getGeometry(basicblock).clone();
        mxPoint add = new mxPoint();
        switch (orientation) {
            case EAST:
                add.setX(distance);
                break;
            case WEST:
                add.setX(-distance);
                break;
            case SOUTH:
                add.setY(distance);
                break;
            case NORTH:
                add.setY(-distance);
                break;
        }
        blockGeo.setX(blockGeo.getX() + add.getX());
        blockGeo.setY(blockGeo.getY() + add.getY());
        graph.getModel().setGeometry(basicblock, blockGeo);
    }

    /**
     * Change the position of simple block which has only one In or Out port.
     *
     * @param blocksStartEnd
     *            all the start/end blocks
     * @param listConnected
     *            the list of blocks which connect to other selections
     * @param all
     *            all the cells in graph
     * @param graph
     */
    private static void changeStartEndBlocksPosition(List<BasicBlock> blocksStartEnd, List<BasicBlock> listConnected,
            Object[] all, XcosDiagram graph) {
        List<List<BasicBlock>> list = getSameTargetBlocks(blocksStartEnd);
        List<BasicBlock> listChanged = new ArrayList<>(0);

        // for those blocks which connects to same block,
        for (List<BasicBlock> blocks : list) {
            listChanged.addAll(blocks);
            changeMultipleStartEndBlocksPosition(blocks, all, graph);
        }

        // for those single start/end blocks,
        for (BasicBlock block : blocksStartEnd) {
            if (!listChanged.contains(block) && !listConnected.contains(block)) {
                changeSingleStartEndBlockPosition(block, all, graph);
            }
        }
    }

    /**
     * Get all the blocks which connects to same block group by the same target.
     *
     * @param blocks
     *            the blocks
     * @return
     */
    private static List<List<BasicBlock>> getSameTargetBlocks(List<BasicBlock> blocks) {
        List<List<BasicBlock>> list = new ArrayList<>(0);
        List<BasicBlock> listChosen = new ArrayList<>(0);
        for (int i = 0; i < blocks.size() - 1; i++) {
            BasicBlock basicblock = blocks.get(i);
            if (!listChosen.contains(basicblock)) {
                List<BasicBlock> listTMP = new ArrayList<>(0);
                listTMP.add(basicblock);
                // add the blocks which connect to the same target block.
                for (int j = i + 1; j < blocks.size(); j++) {
                    if (connectSameBlockOrientation(basicblock, blocks.get(j))) {
                        listTMP.add(blocks.get(j));
                    }
                }
                if (listTMP.size() > 1) {
                    list.add(listTMP);
                }
                listChosen.addAll(listTMP);
            }
        }
        return list;
    }

    /**
     * Check if two blocks connect to the ports which are in the same block and in the same orientation of the block.
     *
     * @param block1
     *            the first block
     * @param block2
     *            the second block
     * @return
     */
    private static boolean connectSameBlockOrientation(BasicBlock block1, BasicBlock block2) {
        if ((!isStartBlock(block1) && !(isEndBlock(block1))) || (!isStartBlock(block2) && !(isEndBlock(block2)))) {
            return false;
        }

        // Check their targets.
        BasicPort port1 = (BasicPort) block1.getChildAt(0);
        BasicBlock other1 = getConnectedBlock(block1, port1);
        BasicPort otherPort1 = getConnectedPort(block1, port1);
        BasicPort port2 = (BasicPort) block2.getChildAt(0);
        BasicBlock other2 = getConnectedBlock(block2, port2);
        BasicPort otherPort2 = getConnectedPort(block2, port2);
        if (other1 != other2) {
            return false;
        }

        // Check the orientations.
        Orientation orientation1 = XcosRouteUtils.getPortOrientation(port1);
        Orientation orientation2 = XcosRouteUtils.getPortOrientation(port2);
        Orientation otherOrien1 = XcosRouteUtils.getPortOrientation(otherPort1);
        Orientation otherOrien2 = XcosRouteUtils.getPortOrientation(otherPort2);
        if (orientation1 == orientation2 && otherOrien1 == otherOrien2) {
            return true;
        }
        return false;
    }

    /**
     * Change the positions of multiple start/end blocks which have only one In or Out port and connect to same block:
     * <br/>
     * <ol>
     * <li>Those blocks should be aligned in a line which is in a certain distance away from the same target block.</li>
     * <li>Reorder the blocks according to the order of the ports in the same target block which each of them connects
     * to.</li>
     * <li>The center point is aligned to the center point of the same target block.</li>
     * <li>The blocks are kept from the neighbor of each other in a certain distance in the line.</li>
     * <li>Update the positions of blocks.</li>
     * </ol>
     *
     * @param listBlocks
     *            a list of multiple blocks
     * @param all
     *            all cells in graph
     * @param graph
     */
    private static void changeMultipleStartEndBlocksPosition(List<BasicBlock> listBlocks, Object[] all,
            XcosDiagram graph) {
        if (listBlocks == null || listBlocks.size() < 1) {
            return;
        }
        int num = listBlocks.size();

        // the blocks in the list have the same connected block and the connected ports have the same orientation.
        BasicBlock sameBlock = getConnectedBlock(listBlocks.get(0), (BasicPort) listBlocks.get(0).getChildAt(0));
        if (sameBlock instanceof SplitBlock) {
            return;
        }
        mxGeometry sameGeometry = sameBlock.getGeometry();
        BasicPort onePort = getConnectedPort(listBlocks.get(0), (BasicPort) listBlocks.get(0).getChildAt(0));
        Orientation sameOrientation = XcosRouteUtils.getPortOrientation(onePort);
        reorderStartEndBlocks(listBlocks, sameOrientation);

        // Get the center point of the same target block and the position of one of the ports in the same target block.
        mxPoint portPoint = BlockAutoPositionUtils.getPortPosition(onePort);
        double x = sameGeometry.getCenterX();
        double y = sameGeometry.getCenterY();

        int blocksDistance = (XcosOptions.getEdition().getGraphBlockDistance() <= 0) ? DEFAULT_BEAUTY_BLOCKS_DISTANCE
                : XcosOptions.getEdition().getGraphBlockDistance();
        int distance = DEFAULT_ALIGNED_BLOCKS_DISTANCE;
        mxPoint[] newPoints = new mxPoint[listBlocks.size()];

        if (num % 2 == 0) { // if number of blocks are even,
            // get the center point of the line.
            switch (sameOrientation) {
                case EAST:
                    newPoints[num / 2 - 1] = new mxPoint(portPoint.getX() + blocksDistance,
                            y - distance / 2 - listBlocks.get(num / 2 - 1).getGeometry().getHeight() / 2);
                    newPoints[num / 2] = new mxPoint(portPoint.getX() + blocksDistance,
                            y + distance / 2 + listBlocks.get(num / 2).getGeometry().getHeight() / 2);
                    break;
                case WEST:
                    newPoints[num / 2 - 1] = new mxPoint(portPoint.getX() - blocksDistance,
                            y - distance / 2 - listBlocks.get(num / 2 - 1).getGeometry().getHeight() / 2);
                    newPoints[num / 2] = new mxPoint(portPoint.getX() - blocksDistance,
                            y + distance / 2 + listBlocks.get(num / 2).getGeometry().getHeight() / 2);
                    break;
                case SOUTH:
                    newPoints[num / 2 - 1] = new mxPoint(
                            x - distance / 2 - listBlocks.get(num / 2 - 1).getGeometry().getWidth() / 2,
                            portPoint.getY() + blocksDistance);
                    newPoints[num / 2] = new mxPoint(
                            x + distance / 2 + listBlocks.get(num / 2).getGeometry().getWidth() / 2,
                            portPoint.getY() + blocksDistance);
                    break;
                case NORTH:
                    newPoints[num / 2 - 1] = new mxPoint(
                            x - distance / 2 - listBlocks.get(num / 2 - 1).getGeometry().getWidth() / 2,
                            portPoint.getY() - blocksDistance);
                    newPoints[num / 2] = new mxPoint(
                            x + distance / 2 + listBlocks.get(num / 2).getGeometry().getWidth() / 2,
                            portPoint.getY() - blocksDistance);
                    break;
            }

            // calculate the positions of blocks according to the center point of the line and the orientation.
            for (int i = num / 2 - 2; i >= 0; i--) {
                mxGeometry geo1 = listBlocks.get(i + 1).getGeometry();
                mxGeometry geo2 = listBlocks.get(i).getGeometry();
                switch (sameOrientation) {
                    case EAST:
                    case WEST:
                        double iY = newPoints[i + 1].getY() - geo1.getHeight() / 2 - distance - geo2.getHeight() / 2;
                        newPoints[i] = new mxPoint(newPoints[num / 2 - 1].getX(), iY);
                        break;
                    case SOUTH:
                    case NORTH:
                        double iX = newPoints[i + 1].getX() - geo1.getWidth() / 2 - distance - geo2.getWidth() / 2;
                        newPoints[i] = new mxPoint(iX, newPoints[num / 2 - 1].getY());
                        break;
                }
            }
            for (int i = num / 2 + 1; i < num; i++) {
                mxGeometry geo1 = listBlocks.get(i - 1).getGeometry();
                mxGeometry geo2 = listBlocks.get(i).getGeometry();
                switch (sameOrientation) {
                    case EAST:
                    case WEST:
                        double iY = newPoints[i - 1].getY() + geo1.getHeight() / 2 + distance + geo2.getHeight() / 2;
                        newPoints[i] = new mxPoint(newPoints[num / 2].getX(), iY);
                        break;
                    case SOUTH:
                    case NORTH:
                        double iX = newPoints[i - 1].getX() + geo1.getWidth() / 2 + distance + geo2.getWidth() / 2;
                        newPoints[i] = new mxPoint(iX, newPoints[num / 2].getY());
                        break;
                }
            }
        } else { // if number of blocks are odd,
            // get the center point of the line.
            switch (sameOrientation) {
                case EAST:
                    newPoints[num / 2] = new mxPoint(portPoint.getX() + blocksDistance, y);
                    break;
                case WEST:
                    newPoints[num / 2] = new mxPoint(portPoint.getX() - blocksDistance, y);
                    break;
                case SOUTH:
                    newPoints[num / 2] = new mxPoint(x, portPoint.getY() + blocksDistance);
                    break;
                case NORTH:
                    newPoints[num / 2] = new mxPoint(x, portPoint.getY() - blocksDistance);
                    break;
            }

            // calculate the positions of blocks according to the center point of the line and the orientation.
            for (int i = num / 2 - 1; i >= 0; i--) {
                mxGeometry geo1 = listBlocks.get(i + 1).getGeometry();
                mxGeometry geo2 = listBlocks.get(i).getGeometry();
                switch (sameOrientation) {
                    case EAST:
                    case WEST:
                        double iY = newPoints[i + 1].getY() - geo1.getHeight() / 2 - distance - geo2.getHeight() / 2;
                        newPoints[i] = new mxPoint(newPoints[num / 2].getX(), iY);
                        break;
                    case SOUTH:
                    case NORTH:
                        double iX = newPoints[i + 1].getX() - geo1.getWidth() / 2 - distance - geo2.getWidth() / 2;
                        newPoints[i] = new mxPoint(iX, newPoints[num / 2].getY());
                        break;
                }
            }
            for (int i = num / 2 + 1; i < num; i++) {
                mxGeometry geo1 = listBlocks.get(i - 1).getGeometry();
                mxGeometry geo2 = listBlocks.get(i).getGeometry();
                switch (sameOrientation) {
                    case EAST:
                    case WEST:
                        double iY = newPoints[i - 1].getY() + geo1.getHeight() / 2 + distance + geo2.getHeight() / 2;
                        newPoints[i] = new mxPoint(newPoints[num / 2].getX(), iY);
                        break;
                    case SOUTH:
                    case NORTH:
                        double iX = newPoints[i - 1].getX() + geo1.getWidth() / 2 + distance + geo2.getWidth() / 2;
                        newPoints[i] = new mxPoint(iX, newPoints[num / 2].getY());
                        break;
                }
            }
        }

        // get all obstacles: blocks, ports in blocks, links, target ports, target block.
        XcosRoute util = new XcosRoute();
        List<Object> listExclude = new ArrayList<>(0);
        listExclude.addAll(listBlocks); // blocks
        listExclude.add(sameBlock); // target block
        for (BasicBlock block : listBlocks) {
            listExclude.add(block.getChildAt(0)); // port in block
            listExclude.add(block.getChildAt(0).getEdgeAt(0)); // link
            listExclude.add(getConnectedPort(block, (BasicPort) block.getChildAt(0))); // target port
        }
        Object[] allObstacles = util.getAllOtherCells(all, listExclude.toArray());

        // update the positions of blocks
        for (int i = 0; i < listBlocks.size(); i++) {
            mxGeometry blockGeo = (mxGeometry) graph.getModel().getGeometry(listBlocks.get(i)).clone();
            mxPoint blockPortPoint = newPoints[i];
            mxGeometry portGeo = ((BasicPort) listBlocks.get(i).getChildAt(0)).getGeometry();
            double portX = portGeo.getX();
            double portY = portGeo.getY();
            double portW = portGeo.getWidth();
            double portH = portGeo.getHeight();
            blockGeo.setX(blockPortPoint.getX() - portW / 2 - portX);
            blockGeo.setY(blockPortPoint.getY() - portH / 2 - portY);
            if (checkObstacles(blockGeo, allObstacles)) {
                continue;
            }
            graph.getModel().setGeometry(listBlocks.get(i), blockGeo);

            // TODO: Is it necessary to change the link to straight?
            // update the link.
            BasicLink link = (BasicLink) listBlocks.get(i).getChildAt(0).getEdgeAt(0);
            reset(graph, link);
            graph.setCellStyles(mxConstants.STYLE_NOEDGESTYLE, "1", new BasicLink[] { link });
        }
    }

    /**
     * Reorder the start/end blocks according to the order of the ports of the same block which they connect to.
     *
     * @param list
     *            the list of start/end blocks
     */
    private static void reorderStartEndBlocks(List<BasicBlock> list, Orientation orien) {
        List<BasicPort> listPorts = new ArrayList<>(0);
        List<mxPoint> listPoints = new ArrayList<>(0);
        for (BasicBlock block : list) {
            BasicPort port = getConnectedPort(block, (BasicPort) block.getChildAt(0));
            listPorts.add(port);
            listPoints.add(BlockAutoPositionUtils.getPortPosition(port));
        }

        // change the order of the blocks according to the positions of the ports which they connect to.
        BasicBlock tmpBlock;
        mxPoint tmpPoint;
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = list.size() - 1; j > i; j--) {
                mxPoint p1 = listPoints.get(j - 1);
                mxPoint p2 = listPoints.get(j);
                if (((orien == Orientation.EAST || orien == Orientation.WEST) && p1.getY() > p2.getY())
                        || ((orien == Orientation.SOUTH || orien == Orientation.NORTH) && p1.getX() > p2.getX())) {
                    tmpBlock = list.get(j);
                    list.set(j, list.get(j - 1));
                    list.set(j - 1, tmpBlock);
                    tmpPoint = listPoints.get(j);
                    listPoints.set(j, listPoints.get(j - 1));
                    listPoints.set(j - 1, tmpPoint);
                }
            }
        }
    }

    /**
     * Change the position of simple block which has only one In or Out port.
     *
     * @param block
     *            the block
     * @param all
     *            all cells in graph
     * @param graph
     */
    private static void changeSingleStartEndBlockPosition(BasicBlock block, Object[] all, XcosDiagram graph) {
        BasicPort port = (BasicPort) block.getChildAt(0);
        BasicLink link = (BasicLink) port.getEdgeAt(0);
        if (link == null) {
            // if the port is not connected,
            return;
        }
        mxPoint point = BlockAutoPositionUtils.getPortPosition(port);
        double x1 = point.getX();
        double y1 = point.getY();
        BasicPort otherPort = getConnectedPort(block, port);
        mxPoint otherPoint = BlockAutoPositionUtils.getPortPosition(otherPort);
        double x2 = otherPoint.getX();
        double y2 = otherPoint.getY();
        if (otherPort.getParent() instanceof SplitBlock) {
            return;
        }

        // if two ports are unable to be opposite.
        Orientation orientation1 = XcosRouteUtils.getPortOrientation(port);
        Orientation orientation2 = XcosRouteUtils.getPortOrientation(otherPort);
        if (!XcosRouteUtils.isOrientationOpposite(orientation1, orientation2)) {
            return;
        }

        XcosRoute util = new XcosRoute();
        Object[] allObstacles = util.getAllOtherCells(all, block, port, link, otherPort);

        if (isPortsOpposite(port, otherPort) && XcosRouteUtils.checkObstacle(x1, y1, x2, y2, allObstacles)) {
            // if two ports are opposite and there are no obstacles between two ports, keep them in original positions.
            return;
        } else {
            // update the position of the block.
            mxGeometry blockGeo = (mxGeometry) graph.getModel().getGeometry(block).clone();
            mxPoint portPoint = getPortNewPosition(otherPort, allObstacles);
            mxGeometry portGeo = port.getGeometry();
            double portX = portGeo.getX();
            double portY = portGeo.getY();
            double portW = portGeo.getWidth();
            double portH = portGeo.getHeight();
            blockGeo.setX(portPoint.getX() - portW / 2 - portX);
            blockGeo.setY(portPoint.getY() - portH / 2 - portY);
            if (checkObstacles(blockGeo, allObstacles)) {
                return;
            }
            graph.getModel().setGeometry(block, blockGeo);

            // TODO: Is it necessary to change the link to straight?
            // update the link.
            reset(graph, link);
            graph.setCellStyles(mxConstants.STYLE_NOEDGESTYLE, "1", new BasicLink[] { link });
            // util.updateRoute(link, allObstacles, graph, false);
        }
    }

    /**
     * Get the new position of the block according to the port which it connects to.
     *
     * @param other
     *            the port which the block connects to
     * @param obstacles
     *            all the obstacles
     * @return the new position
     */
    private static mxPoint getPortNewPosition(BasicPort other, Object[] obstacles) {
        int blocksDistance = (XcosOptions.getEdition().getGraphBlockDistance() <= 0) ? DEFAULT_BEAUTY_BLOCKS_DISTANCE
                : XcosOptions.getEdition().getGraphBlockDistance();
        mxPoint otherPoint = BlockAutoPositionUtils.getPortPosition(other);
        double x = otherPoint.getX();
        double y = otherPoint.getY();
        Orientation orientation = XcosRouteUtils.getPortOrientation(other);

        mxPoint point = new mxPoint(x, y);
        switch (orientation) {
            case EAST:
                point.setX(x + blocksDistance);
                while (Math.abs(point.getX() - x) > XcosRouteUtils.BEAUTY_AWAY_REVISION
                        && (XcosRouteUtils.checkObstacle(x, y, point.getX(), point.getY(), obstacles)
                                || XcosRouteUtils.checkPointInBlocks(point.getX(), point.getY(), obstacles))) {
                    point.setX(point.getX() - XcosRouteUtils.BEAUTY_AWAY_REVISION);
                }
                break;
            case WEST:
                point.setX(x - blocksDistance);
                while (Math.abs(point.getX() - x) > XcosRouteUtils.BEAUTY_AWAY_REVISION
                        && (XcosRouteUtils.checkObstacle(x, y, point.getX(), point.getY(), obstacles)
                                || XcosRouteUtils.checkPointInBlocks(point.getX(), point.getY(), obstacles))) {
                    point.setX(point.getX() + XcosRouteUtils.BEAUTY_AWAY_REVISION);
                }
                break;
            case SOUTH:
                point.setY(y + blocksDistance);
                while (Math.abs(point.getY() - y) > XcosRouteUtils.BEAUTY_AWAY_REVISION
                        && (XcosRouteUtils.checkObstacle(x, y, point.getX(), point.getY(), obstacles)
                                || XcosRouteUtils.checkPointInBlocks(point.getX(), point.getY(), obstacles))) {
                    point.setY(point.getY() - XcosRouteUtils.BEAUTY_AWAY_REVISION);
                }
                break;
            case NORTH:
                point.setY(y - blocksDistance);
                while (Math.abs(point.getY() - y) > XcosRouteUtils.BEAUTY_AWAY_REVISION
                        && (XcosRouteUtils.checkObstacle(x, y, point.getX(), point.getY(), obstacles)
                                || XcosRouteUtils.checkPointInBlocks(point.getX(), point.getY(), obstacles))) {
                    point.setY(point.getY() + XcosRouteUtils.BEAUTY_AWAY_REVISION);
                }
                break;
        }
        return point;
    }

    /**
     * Check if two ports are opposite and aligned.
     *
     * @param port1
     *            the first port
     * @param port2
     *            the second port
     * @return <b>true</b> if two ports are opposite and aligned
     */
    private static boolean isPortsOpposite(BasicPort port1, BasicPort port2) {
        mxPoint point1 = BlockAutoPositionUtils.getPortPosition(port1);
        double x1 = point1.getX();
        double y1 = point1.getY();
        Orientation orientation1 = XcosRouteUtils.getPortOrientation(port1);
        mxPoint point2 = BlockAutoPositionUtils.getPortPosition(port2);
        double x2 = point2.getX();
        double y2 = point2.getY();
        Orientation orientation2 = XcosRouteUtils.getPortOrientation(port2);

        if (!XcosRouteUtils.isStrictlyAligned(x1, y1, x2, y2)) {
            return false;
        }

        if (x1 == x2) {
            if (orientation1 == Orientation.SOUTH && orientation2 == Orientation.NORTH && y1 < y2) {
                return true;
            }
            if (orientation1 == Orientation.NORTH && orientation2 == Orientation.SOUTH && y1 > y2) {
                return true;
            }
        } else if (y1 == y2) {
            if (orientation1 == Orientation.EAST && orientation2 == Orientation.WEST && x1 < x2) {
                return true;
            }
            if (orientation1 == Orientation.WEST && orientation2 == Orientation.EAST && x1 > x2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if block2 is on the right (below) of block1.<br/>
     * If block2 is on the right of block1 or block2 is exactly below block2, return true.
     *
     * @param block1
     *            the first block
     * @param block2
     *            the second block
     * @return
     */
    private static boolean isRightBelow(BasicBlock block1, BasicBlock block2) {
        mxGeometry geometry1 = block1.getGeometry();
        mxGeometry geometry2 = block2.getGeometry();
        if ((geometry1.getX() < geometry2.getX())
                || (geometry1.getX() == geometry2.getX() && geometry1.getY() < geometry2.getY())) {
            return true;
        }
        return false;
    }

    /**
     * Check if two blocks are connected.
     *
     * @param block1
     *            the first block
     * @param block2
     *            the second block
     * @return <b>true</b> if two blocks are contected
     */
    private static boolean isBlocksConnected(BasicBlock block1, BasicBlock block2) {
        for (int i = 0; i < block1.getChildCount(); i++) {
            mxICell child = block1.getChildAt(i);
            if (child instanceof BasicPort) {
                BasicBlock other = getConnectedBlock(block1, (BasicPort) child);
                if (other == block2) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get a block which connects to the port in the block.
     *
     * @param block
     *            the block
     * @param port
     *            the port in the block
     * @return a block which connects to the port in the block
     */
    private static BasicBlock getConnectedBlock(BasicBlock block, BasicPort port) {
        BasicPort otherPort = getConnectedPort(block, port);
        if (otherPort == null) {
            return null;
        }
        return (BasicBlock) otherPort.getParent();
    }

    /**
     * Get the linked Port of a Block according to its Port.
     *
     * @param block
     *            the Block
     * @param port
     *            the port in the Block
     * @return the other port in the link
     */
    private static BasicPort getConnectedPort(BasicBlock block, BasicPort port) {
        mxICell cell = null;
        mxICell edge = port.getEdgeAt(0);
        if (edge != null && edge instanceof mxCell) {
            cell = ((mxCell) edge).getTarget();
            if (cell == port) {
                cell = ((mxCell) edge).getSource();
            }
        }
        return (BasicPort) cell;
    }

    /**
     * Get the port in the block which connects to the target.
     *
     * @param block
     *            the block
     * @param target
     *            the target block
     * @return the port in the block
     */
    private static BasicPort getSelfPort(BasicBlock block, BasicBlock target) {
        mxICell cell = null;
        for (int i = 0; i < block.getChildCount(); i++) {
            mxICell port = block.getChildAt(i);
            if (port instanceof BasicPort) {
                BasicBlock other = getConnectedBlock(block, (BasicPort) port);
                if (other == target) {
                    cell = port;
                    break;
                }
            }
        }
        return (BasicPort) cell;
    }

    /**
     * Check if the block has a port in a given Orientation.
     *
     * @param block
     *            the basic block
     * @param orientation
     *            the corresponded orientation
     * @return <b>true</b> if it has
     */
    private static boolean hasPortOrientation(BasicBlock block, Orientation orientation) {
        for (int i = 0; i < block.getChildCount(); i++) {
            mxICell child = block.getChildAt(i);
            if (child instanceof BasicPort) {
                BasicPort port = (BasicPort) child;
                if (XcosRouteUtils.getPortOrientation(port) == orientation) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether there are blocks in the block.
     *
     * @param block
     *            the block
     * @param allObstacles
     *            all the obstacles
     * @return <b>true</b> if there is at least one block(s) in the block.
     */
    protected static boolean checkObstacles(BasicBlock block, Object[] allObstacles) {
        mxGeometry geometry = block.getGeometry();
        return checkObstacles(geometry, allObstacles);
    }

    protected static boolean checkObstacles(mxGeometry geometry, Object[] allObstacles) {
        // Get the four edges of the block.
        double[] x = new double[5];
        double[] y = new double[5];
        x[0] = geometry.getX();
        y[0] = geometry.getY();
        x[1] = geometry.getX();
        y[1] = geometry.getY() + geometry.getHeight();
        x[2] = geometry.getX() + geometry.getWidth();
        y[2] = geometry.getY();
        x[3] = geometry.getX() + geometry.getWidth();
        y[3] = geometry.getY() + geometry.getHeight();
        x[4] = geometry.getX();
        y[4] = geometry.getY();

        // Check the obstacles for each edge.
        for (int i = 1; i < 5; i++) {
            if (XcosRouteUtils.checkObstacle(x[i - 1], y[i - 1], x[i], y[i], allObstacles)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reset all the links of the block.
     *
     * @param block
     *            the block
     * @param graph
     */
    private static void resetLink(BasicBlock block, XcosDiagram graph) {
        for (int i = 0; i < block.getChildCount(); i++) {
            BasicLink link = (BasicLink) block.getChildAt(i).getEdgeAt(0);
            reset(graph, link);
            graph.setCellStyles(mxConstants.STYLE_NOEDGESTYLE, "1", new BasicLink[] { link });
        }
    }

    /**
     * Reset the link.
     *
     * @param graph
     * @param edge
     */
    private static void reset(final ScilabGraph graph, final Object edge) {
        final SelectionCellsHandler selectionCellsHandler = (SelectionCellsHandler) graph.getAsComponent()
                .getSelectionCellsHandler();
        graph.resetEdge(edge);
        selectionCellsHandler.clearCellHandler(edge);
    }

}
