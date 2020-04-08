package pl.gda.pg.eti.lsea.lab;

import java.util.ArrayList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Handles tree structure containing Snippets and Folders. Implements TreeModel.
 * Used to display the structure with {@link javax.swing.JTree}.
 * 
 * @author Tomasz Wierciński
 */
public class FolderTree implements TreeModel {

    //region Fields
    // parent of all Folders and Snippets belonging to user
    private Folder root_folder = new Folder("Root");
    // list of all tree model listeners
    private ArrayList<TreeModelListener> listeners = new ArrayList<>(); 
    //endregion

    /**
     * Retrieves the root of the folder tree.
     * @return the root Folder
     */
    @Override
    public Object getRoot() {
        return this.root_folder;
    }

    /**
     * Retrieves the child from passed parent node and child index.
     * @param parent parent node of searched child
     * @param index index of child
     * @return child node
     */
    @Override
    public Object getChild(Object parent, int index) {
        Node child = null;
        
        // Only Folders have children
        if (parent instanceof Folder) {
            child = ((Folder) parent).getChild(index);
        }
        
        return child;
    }

    /**
     * Retrieves the number of children of a node.
     * @param parent parent node of counted children
     * @return number of children
     */
    @Override
    public int getChildCount(Object parent) {
        int count = 0;
        
        // Only Folders have children
        if (parent instanceof Folder) {
            count = ((Folder) parent).getChildren().size();
        }
        
        return count;
    }

    /**
     * Identifies whether the node is a leaf. In this case it simply checks if 
     * the passed object is an instance of Snippet.
     * @param node tree object to be checked
     * @return true if node is a leaf, false otherwise.
     */
    @Override
    public boolean isLeaf(Object node) {
        return (node instanceof Snippet);
    }

    /**
     * Returns the index of a child node.
     * @param parent parent node, Folder
     * @param child child node
     * @return index
     */
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((Folder) parent).getChildren().indexOf(child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        System.out.println("INFO: Adding tree model listener " + l);
        this.listeners.add(l);
    }

    /**
     * Removes passed {@link javax.swing.event.TreeModelListener} from list of
     * listeners.
     * @param l listener
     */
    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        System.out.println("INFO: Removing tree model listener " + l);
        this.listeners.remove(l);
    }
    
    /**
     * Inserts node as child of a parent node. Sends 
     * {@link javax.swing.event.TreeModelEvent} event to all listeners.
     * @param child child Node
     * @param parent parent Node, Folder
     */
    public void insertNodeInto(Node child, Folder parent) {
        parent.addChild(child);
        
        TreePath path = new TreePath(parent.getPathArray());
        int[] child_indices = {parent.getChildren().size()-1};
        Object[] children = {child};
        
        TreeModelEvent e = new TreeModelEvent(this, path, child_indices, children);
        for (TreeModelListener l : listeners) {
            
            l.treeNodesInserted(new TreeModelEvent(this, path, child_indices, children));
            System.out.println(e + ";" + l);
        }
    }
    
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}