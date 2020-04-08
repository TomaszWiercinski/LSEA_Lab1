package pl.gda.pg.eti.lsea.lab.gui;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import pl.gda.pg.eti.lsea.lab.Node;

/**
 * WORK IN PROGRESS
 * 
 * Listener for all changes related to the folder structure.
 * 
 * @author Tomasz Wierciński
 */
public class FolderStructureListener implements TreeModelListener {

    @Override
    public void treeNodesChanged(TreeModelEvent e) {
        Node node;
        node = (Node)(e.getTreePath().getLastPathComponent());

        System.out.println("The user has finished editing the node.");
        System.out.println("New value: " + node.getTitle());
    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {
        // TODO: AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
