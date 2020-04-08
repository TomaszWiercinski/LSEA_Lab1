package pl.gda.pg.eti.lsea.lab;

import java.util.Comparator;

/**
 * Comparator for comparing Nodes based on creation dates. Orders nodes chronologically.
 * @author Tomasz Wierciński
 */
public class DateComparator implements Comparator<Node> {

    @Override
    public int compare(Node o1, Node o2) {
        return o1.getDateCreated().compareTo(o2.getDateCreated());
    }
    
}
