package pl.gda.pg.eti.lsea.lab;

import java.util.ArrayList;

/**
 * Abstract Node class representing every element within the file structure.
 * @author Tomasz Wierciński
 */
public abstract class Node implements Cloneable, Comparable {
    
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
    
    /**
     * Casts Object to {@link Node} and compares using {@link Node#compareTo(pl.gda.pg.eti.lsea.lab.Node)}.
     * @param o an Object to compare with
     * @return 
     * {@code null} if the Object argument cannot be cast to {@link Node};
     * the value 0 if the argument Node's title is equal to this Node's title; 
     * a value less than 0 if this Node's title is lexicographically less than the argument Node's title; 
     * a value greater than 0 if this Node's title is lexicographically greater than the argument Node's title;.
     */
    @Override
    public int compareTo(Object o) {
        return this.compareTo((Node) o);
    }
    /**
     * Compares {@link Node} titles lexicographically.
     * @param anotherNode
     * A {@link Node} to compare with.
     * @return 
     * The value 0 if the argument Node's title is equal to this Node's title; 
     * a value less than 0 if this Node's title is lexicographically less than the argument Node's title; 
     * and a value greater than 0 if this Node's title is lexicographically greater than the argument Node's title.
     */
    public int compareTo(Node anotherNode) {
        return title.compareTo(anotherNode.getTitle());
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