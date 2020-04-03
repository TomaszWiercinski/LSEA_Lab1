package pl.gda.pg.eti.lsea.lab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Abstract Node class representing every element within the file structure.
 * @author Tomasz Wierciński
 */
public abstract class Node implements Cloneable, Comparable<Node> {
    
    protected String title;  // name of the node
    protected Node parent = null;
    protected Date created;
    protected Date edited;

    //region Constructors
    public Node() {
        this("Untitled");
    }
    public Node(String title) {
        this.title = title;
        this.created = new Date();
        this.edited = this.created;
    }
    //endregion

    //region Getters
    public String getTitle() {
        return title;
    }
    public Node getParent() {
        return parent;
    }
    //endregion

    //region Setters
    public void setTitle(String title) {
        this.title = title;
    }
    public void setParent(Node parent) {
        this.parent = parent;
    }
    //endregion

    @Override
    public String toString() {
        return title;
    }
    
    /**
     * Compares {@link Node} titles lexicographically.
     * @param anotherNode
     * a {@link Node} to compare with.
     * @return 
     * The value 0 if the argument Node's title is equal to this Node's title; 
     * a value less than 0 if this Node's title is lexicographically less than the argument Node's title; 
     * and a value greater than 0 if this Node's title is lexicographically greater than the argument Node's title.
     */
    @Override
    public int compareTo(Node anotherNode) {
        return title.compareTo(anotherNode.getTitle());
    }

    /**
     * Returns file structure from Node as a string to be displayed.
     * @return String
     */
    public String getStructure() {
        return getStructure(0);
    }
    
    /**
     * Helper function of {@link #getStructure()}.
     * @param depth depth of the node in the structure
     * @return file structure as String to be displayed
     */
    protected String getStructure(int depth) {
        return "    ".repeat(depth) + "|-- " + title;
    }
    
    /**
     * Finds a full path for the current Node within a larger hierarchy.
     * @return path to this Node as a String
     */
    public String getPath() {
        return ((parent == null) ? "/" : parent.getPath()) + "/" + title;
    }
    
    public Node getNodeFromPath(String path) {
        String[] path_arr = path.split("/");
        if (path_arr[0].isBlank())
            path_arr = Arrays.copyOfRange(path_arr, 1, path_arr.length);
        return getNodeFromPath(path_arr);
    }
    public Node getNodeFromPath(String[] path) {
        Node out;
        if (path.length == 0)
            out = this;
        else
            out = null;
        return out;
    }
    
    @Override
    public abstract Object clone() throws CloneNotSupportedException;
    public abstract ArrayList<Node> searchTitle(String term);
    public abstract ArrayList<Node> searchContent(String term);
}