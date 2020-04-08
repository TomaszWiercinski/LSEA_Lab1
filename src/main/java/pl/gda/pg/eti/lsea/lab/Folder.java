package pl.gda.pg.eti.lsea.lab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * A Folder which can contain any number of other Nodes (other Folder or a 
 * Snippet).
 * @see Node
 * @author Tomasz Wierciński
 */
public class Folder extends Node {

    ArrayList<Node> children = new ArrayList<>();  // all Nodes contained in Folder

    //region Constructors
    public Folder() {
        super();
    }
    public Folder(String title) {
        super(title);
    }
    //endregion

    //region Getters
    public ArrayList<Node> getChildren() {
        return children;
    }
    public Node getChild(int index) {
        Node child = null;
        
        if (index >= 0 && index < this.children.size()) {
            child = this.children.get(index);
        }
        
        return child;
    }
    public Node getChildFromTitle(String title) {
        Node out = null;
        for (Node child : children)
            if (child.getTitle().equals(title)) {
                out = child;
                break;
            }
        return out;
    }
    //endregion

    //region Setters
    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }
    //endregion
    
    //region Mutators
    /**
     * Adds Node as child of this Folder.
     * @param node
     */
    public void addChild(Node node) {
        children.add(node);
        node.setParent(this);
    }
    
    /**
     * Removes child node with picked index.
     * @param index child index
     * @return removed child Node
     */
    public Node removeChild(int index) {
        return children.remove(index);
    }
    
    /**
     * Removes passed child node from list of children.
     * @param child child Node to be removed
     * @return true if the child Node was removed
     */
    public boolean removeChild(Node child) {
        return children.remove(child);
    }
    //endregion
    
    /**
     * Sorts all nodes within a folder lexicographically. Uses title string to
     * sort. Sorts recursively over all sub-folders.
     */
    public void sort() {
        Collections.sort(children);
        for (Node child : children) {
            if (child instanceof Folder) {
                ((Folder)child).sort();
            }
        }
    }
    
    /**
     * Sorts all nodes within a folder using provided Comparator implementation.
     * Sorts recursively over all sub-folders.
     * @param <T> implementation of Comparator interface
     * @param comparator provided Comparator
     */
    public <T extends Comparator> void sort(T comparator) {
        Collections.sort(children, comparator);
        for (Node child : children) {
            if (child instanceof Folder) {
                ((Folder)child).sort(comparator);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public String toString() {
        return title;
    }
    
    @Override
    public Folder clone() throws CloneNotSupportedException {
        Folder folder = new Folder(this.title);
        
        // ArrayList cloning constructor won't work here due to parent reference.
        ArrayList<Node> children_clone = new ArrayList<>(this.children.size());
        for (Node child : this.children) {
            // Can't addChild directly to folder due to concurrent modification issues. :<
            Node child_clone = (Node) child.clone();
            folder.addChild(child_clone);
        }
        
        return folder;
    }

    /**
     * {@inheritDoc}
     * @param depth depth of the node in the structure
     * @return file structure as String to be displayed
     */
    @Override
    protected String getStructure(int depth) {
        String out = super.getStructure(depth);

        for (Node child : children) {
            out += "\n" + child.getStructure(depth + 1);
        }

        return out;
    }

    /**
     * Checks if the title of the Folder - or any titles of child Nodes - 
     * contains the term. Non-case-sensitive. Returns the Folder and any child
     * Nodes that are a match.
     * @see Node#searchTitle(java.lang.String)
     * @see Snippet#searchTitle(java.lang.String)
     * @param term String to be searched for
     * @return ArrayList of matching Nodes, empty if no matches found.
     */
    @Override
    public ArrayList<Node> searchTitle(String term) {
        ArrayList<Node> found = new ArrayList<>();

        // checks this Folder
        if (title.toLowerCase().contains(term.toLowerCase())) {
            found.add(this);
        }

        // checks all child nodes - usage of polymorphism
        for (Node child : children) {
            found.addAll(child.searchTitle(term));
        }

        return found;
    }

    /**
     * Non-case-sensitive search on contents of all nodes within folder. 
     * Currently, only returns Snippets.
     * @param term search term to be compared against node contents
     * @return ArrayList of Nodes matching search term
     */
    @Override
    public ArrayList<Node> searchContent(String term) {
        ArrayList<Node> found = new ArrayList<>();

        // checks all child nodes - usage of polymorphism
        for (Node child : children) {
            found.addAll(child.searchContent(term));
        }

        return found;
    }
    
    @Override
    public Node getNodeFromPath(String[] path) {
        Node out = null;
        
        if (path.length > 0) {
            out = getChildFromTitle(path[0]);
            if (out != null && path.length > 1)
                out = out.getNodeFromPath(Arrays.copyOfRange(path, 1, path.length));
        }
        else if (path[0].equals(title))
            out = this;
            
        return out;
    }

    /**
     * Creates and shows an example folder structure.
     * @param args 
     */
    public static void main(String args[]){
        // Folder structure example
        Folder folder_main = new Folder();
        Folder folder_sub_a = new Folder("subA");
        Folder folder_sub_b = new Folder("subB");
        Folder folder_sub_c = new Folder("subC");
        Folder folder_sub_c_a = new Folder("subC_A");
        Folder folder_sub_b_a = new Folder("subB_A");
        Folder folder_sub_b_b = new Folder("subB_B");
        Folder folder_sub_b_a_a = new Folder("subB_A_A");

        folder_main.addChild(folder_sub_b);
        folder_main.addChild(folder_sub_c);
        folder_main.addChild(folder_sub_a);

        folder_sub_c.addChild(folder_sub_c_a);
        folder_sub_b.addChild(folder_sub_b_b);
        folder_sub_b.addChild(folder_sub_b_a);

        folder_sub_b_a.addChild(folder_sub_b_a_a);
        
        // Deep cloning example
        try {
            Folder folder_sub_b_clone = (Folder) folder_sub_b.clone();
            folder_main.addChild(folder_sub_b_clone);
            folder_sub_b_clone.setTitle("subB_copy"); // changed title of clone
            folder_sub_b_clone.removeChild(0); // removed child from clone
            folder_sub_b_clone.addChild(new Folder("subB_copy_new_child")); // added new child to clone
            //further testing can be done via interactive dashboard
        } catch (CloneNotSupportedException ex) {
            System.out.println("This ain't Dolly, that's for sure.");
        }

        // Display example structure
        System.out.println("Example folder structure:");
        System.out.println(folder_main.getStructure());
        
        // Display structure after sorting
        folder_main.sort();
        System.out.println("\nAfter sorting:");
        System.out.println(folder_main.getStructure());

        // Title search examples
        System.out.println("\nSearch results for term \"sub\":");
        System.out.println(folder_main.searchTitle("sub"));

        System.out.println("\nSearch results for term \"A\" (not case-sensitive):");
        System.out.println(folder_main.searchTitle("A"));
        
        // Path usage examples
        String path = "/subA";
        System.out.println("\nContents of folder \"" + path + "\" (works without trailing slash)");
        System.out.println(folder_main.getNodeFromPath(path).getStructure());
        
        path = "subC/";
        System.out.println("\nContents of folder \"" + path + "\" (works without leading slash)" );
        System.out.println(folder_main.getNodeFromPath(path).getStructure());
        
        path = "/subB/subB_A/";
        System.out.println("\nContents of folder \"" + path + "\"");
        System.out.println(folder_main.getNodeFromPath(path).getStructure());
        
        System.out.println("\nGetting path to node: " + folder_sub_b_a_a.toString());
        System.out.println(folder_sub_b_a_a.getPath());
    }
}