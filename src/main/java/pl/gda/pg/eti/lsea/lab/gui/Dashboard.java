package pl.gda.pg.eti.lsea.lab.gui;

import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import pl.gda.pg.eti.lsea.lab.FileTree;
import static pl.gda.pg.eti.lsea.lab.FileTree.displayFolder;
import static pl.gda.pg.eti.lsea.lab.FileTree.displaySnippet;
import pl.gda.pg.eti.lsea.lab.Folder;
import pl.gda.pg.eti.lsea.lab.Node;
import pl.gda.pg.eti.lsea.lab.Snippet;
import pl.gda.pg.eti.lsea.lab.testing.RandomStructure;

/**
 *
 * @author Tomasz Wierciński
 */
public class Dashboard extends JFrame {
    private JTree tree;
    
    public Dashboard(FileTree filetree) {
        super("Dashboard");
        
        tree = new JTree(filetree);
        JScrollPane treeView = new JScrollPane(tree);
        
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.add(treeView);
        this.pack();
        this.setVisible(true);
    }
    public Dashboard() {
        super("Dashboard");
        
        FileTree filetree = new FileTree();
        tree = new JTree(filetree);
        JScrollPane treeView = new JScrollPane(tree);
        
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.add(treeView);
        this.pack();
        this.setVisible(true);
    }
    
    public static void main(String[] args) {
        // Make an example folder structure within the dashboard.
        FileTree file_tree = new FileTree();

        Folder lsea = new Folder("LSEA");
        Folder mas = new Folder("MAS");

        Folder main_folder = (Folder) file_tree.getRoot();

        main_folder.addChild(lsea);
        main_folder.addChild(mas);
        
        // Random filler
        RandomStructure rs = new RandomStructure(5, 4);
        Folder random = rs.generate();
        random.setTitle("RandomStructure");
        main_folder.addChild(random);

        // Add some example snippets.
        lsea.addChild(new Snippet("Folder Add", "Java",
                "public void add(Node node) {\n" +
                "    children.add(node);\n" +
                "}"));
        lsea.addChild(new Snippet("Snippet toString", "Java",
                "@Override\n" +
                "public String toString() {\n" +
                "    return \"Snippet(\" + title + \")\";\n" +
                "}"));
        
        Dashboard db = new Dashboard(file_tree);
        
    }
}
