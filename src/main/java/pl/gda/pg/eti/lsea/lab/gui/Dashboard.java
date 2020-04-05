package pl.gda.pg.eti.lsea.lab.gui;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;
import pl.gda.pg.eti.lsea.lab.FileTree;
import pl.gda.pg.eti.lsea.lab.Folder;
import pl.gda.pg.eti.lsea.lab.Node;
import pl.gda.pg.eti.lsea.lab.Snippet;
import pl.gda.pg.eti.lsea.lab.testing.RandomStructure;

/**
 * The point of contact between the user and the application.
 * @author Tomasz Wierciński
 */
public class Dashboard extends JFrame implements TreeSelectionListener {
    
    private JTree tree;
    private JEditorPane label;
    
    public Dashboard(FileTree filetree) {
        super("Dashboard");
        
        tree = new JTree(filetree);
        JScrollPane treeView = new JScrollPane(tree);
        tree.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        
        label = new JEditorPane();
        
        GridLayout experimentLayout = new GridLayout(1, 2);
        this.setLayout(experimentLayout);
        
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.add(treeView);
        this.add(label);
        this.pack();
        this.setVisible(true);
    }
    public Dashboard() {
        this(new FileTree());
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

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        Node node = (Node)tree.getLastSelectedPathComponent();

        if (node == null)
            return;

        if (node instanceof Snippet) {
            label.setText(((Snippet) node).get());
        }
    }
}
