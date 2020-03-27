package pl.gda.pg.eti.lsea.lab1;

import java.util.ArrayList;

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
    //endregion

    @Override
    public String toString() {
        return "Folder(" + title + ")";
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

    /**
     * Adds Node as child of this Folder.
     * @param node
     */
    public void add(Node node) {
        children.add(node);
        node.setParent(this);
    }

    /**
     * Creates and shows an example folder structure.
     * @param args 
     */
    public static void main(String args[]){
        // Folder structure example
        System.out.println("Example folder structure:");
        Folder folder_main = new Folder();
        Folder folder_sub_a = new Folder("subA");
        Folder folder_sub_b = new Folder("subB");
        Folder folder_sub_c = new Folder("subC");
        Folder folder_sub_c_a = new Folder("subC_A");
        Folder folder_sub_b_a = new Folder("subB_A");
        Folder folder_sub_b_b = new Folder("subB_B");
        Folder folder_sub_b_a_a = new Folder("subB_A_A");

        folder_main.add(folder_sub_a);
        folder_main.add(folder_sub_b);
        folder_main.add(folder_sub_c);

        folder_sub_c.add(folder_sub_c_a);
        folder_sub_b.add(folder_sub_b_a);
        folder_sub_b.add(folder_sub_b_b);

        folder_sub_b_a.add(folder_sub_b_a_a);

        // Display example structure
        System.out.println(folder_main.getStructure());

        // Title search examples
        System.out.println("\nSearch results for term \"sub\":");
        System.out.println(folder_main.searchTitle("sub"));

        System.out.println("\nSearch results for term \"A\" (not case-sensitive):");
        System.out.println(folder_main.searchTitle("A"));
    }
}