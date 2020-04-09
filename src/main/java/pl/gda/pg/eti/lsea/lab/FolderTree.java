package pl.gda.pg.eti.lsea.lab;

import java.util.Comparator;
import java.util.EventListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * WORK IN PROGRESS
 * 
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
    private EventListenerList listener_list = new EventListenerList(); 
    
    //endregion

    //region TreeModel Overrides
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
        System.out.println("INFO: Adding tree model listener " + l.getClass().getCanonicalName());
        listener_list.add(TreeModelListener.class, l);
    }

    /**
     * Removes passed {@link javax.swing.event.TreeModelListener} from list of
     * listeners.
     * @param l listener
     */
    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        System.out.println("INFO: Removing tree model listener " + l);
        this.listener_list.remove(TreeModelListener.class, l);
    }
    //endregion

    /**
     * Inserts node as child of a parent node. Sends 
     * {@link javax.swing.event.TreeModelEvent} event to all listeners.
     * @param child child Node
     * @param parent parent Node, Folder
     */
    public void insertNodeInto(Node child, Folder parent) {     
        parent.addChild(child);
        
        TreePath path = parent.getPathArray();
        int[] child_indices = {parent.getChildren().indexOf(child)};
        Object[] children = {child};
        
        System.out.println(path.getLastPathComponent());
        
        TreeModelEvent e = new TreeModelEvent(this, path, child_indices, children);
        EventListener[] listeners = listener_list.getListeners(TreeModelListener.class);
        for (int i = 0; i < listeners.length; i++)
           ((TreeModelListener) listeners[i]).treeNodesInserted(e);
    }
    
    public void removeChild(Node child, Folder parent) {
        // The documentation for TreePath is so bad, oh my gooooooooooooood
        TreePath path = parent.getPathArray();
        int[] child_indices = {parent.getChildren().indexOf(child)};
        Object[] children = {child};
        
        parent.removeChild(child);
        
        TreeModelEvent e = new TreeModelEvent(this, path, child_indices, children);
        EventListener[] listeners = listener_list.getListeners(TreeModelListener.class);
        for (EventListener listener : listeners) ((TreeModelListener) listener).treeNodesRemoved(e);
    }
    
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        System.out.println("INFO: Changing node at path " + path.toString() + " to " + newValue.toString() + ".");
        Node node = (Node)path.getLastPathComponent();
        node.setTitle(newValue.toString());
    }

    /**
     * Notifies all listeners of a tree structure change. Not used. Was super useful before I figured out how to write
     * my own TreeModelEvents (the documentation >>really<< sucks). It resets the JTree view, which can be quite a
     * headache.
     */
    @Deprecated
    protected void fireTreeStructureChanged()
    {
        TreeModelEvent event = new TreeModelEvent(this, new Object[] { root_folder });
        EventListener[] listeners = listener_list.getListeners(TreeModelListener.class);
        for (EventListener listener : listeners) ((TreeModelListener) listener).treeStructureChanged(event);
    }

    /**
     * Sort children of a parent Folder lexicographically. Sorts recursively over all sub-folders.
     * @param parent parent Folder.
     */
    public void sortChildren(Folder parent) {
        parent.sort();
        TreeModelEvent event = new TreeModelEvent(this, parent.getPathArray());
        EventListener[] listeners = listener_list.getListeners(TreeModelListener.class);
        for (EventListener listener : listeners) ((TreeModelListener) listener).treeStructureChanged(event);
    }

    /**
     * Sort children of a parent Folder using provided Comparator implementation. Sorts recursively over all
     * sub-folders.
     * @param parent parent Folder.
     * @param comparator provided Comparator.
     */
    public void sortChildren(Folder parent, Comparator<Node> comparator) {
        parent.sort(comparator);
        TreeModelEvent event = new TreeModelEvent(this, parent.getPathArray());
        EventListener[] listeners = listener_list.getListeners(TreeModelListener.class);
        for (EventListener listener : listeners) ((TreeModelListener) listener).treeStructureChanged(event);
    }
}