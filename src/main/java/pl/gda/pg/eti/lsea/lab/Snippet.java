package pl.gda.pg.eti.lsea.lab;

import java.util.ArrayList;

/**
 * Node within the file structure containing a String - snippet of code.
 * @see Node
 * @author Tomasz Wierciński
 */
public class Snippet extends Node{
    private String snippet;
    private String lang;

    //region Constructors
    public Snippet(String title, String lang, String snippet) {
        super(title);
        this.lang = lang;
        this.snippet = snippet;
    }
    public Snippet(String title, String lang) {
        super(title);
        this.lang = lang;
    }
    //endregion

    //region Getters
    public String get() {
        return snippet;
    }
    public String getLang() { return lang; }
    //endregion
    
    //region Setters
    public void set(String snippet) {
        this.snippet = snippet;
    }
    public void setLang(String lang) {
        this.lang = lang;
    }
    //endregion

    /**
     * Checks if the title of the Snippet contains the term. Non-case-sensitive.
     * Returns the Snippet if it's a match.
     * @see Node#searchTitle(java.lang.String)
     * @see Folder#searchTitle(java.lang.String)
     * @param term String to be searched for
     * @return ArrayList of Nodes, empty or with Snippet
     */
    @Override
    public ArrayList<Node> searchTitle(String term) {
        ArrayList<Node> found = new ArrayList<>();

        if (title.toLowerCase().contains(term.toLowerCase())) {
            found.add(this);
        }

        return found;
    }

    /**
     * Non-case-sensitive search on contents of Snippet. 
     * @see Node#searchContent(java.lang.String) 
     * @see Folder#searchContent(java.lang.String) 
     * @param term search term to be compared against snippet
     * @return ArrayList of Nodes, empty or with Snippet
     */
    @Override
    public ArrayList<Node> searchContent(String term) {
        ArrayList<Node> found = new ArrayList<>();

        if (snippet.toLowerCase().contains(term.toLowerCase())) {
            found.add(this);
        }

        return found;
    }

    @Override
    public String toString() {
        return "Snippet(" + title + ")";
    }
}