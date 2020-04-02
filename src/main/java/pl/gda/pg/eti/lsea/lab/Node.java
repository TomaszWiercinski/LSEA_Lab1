package pl.gda.pg.eti.lsea.lab;

import java.util.ArrayList;

/**
 * Abstract Node class representing every element within the file structure.
 * @author Tomasz Wierciński
 */
public abstract class Node implements Cloneable {
    
    protected String title;  // name of the node
    protected Node parent = null;

    //region Constructors
    public Node() {
        this("Untitled");
    }
    public Node(String title) {
        this.title = title;
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
    
    @Override
    public abstract Object clone() throws CloneNotSupportedException;

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
    
    public abstract ArrayList<Node> searchTitle(String term);
    public abstract ArrayList<Node> searchContent(String term);
}