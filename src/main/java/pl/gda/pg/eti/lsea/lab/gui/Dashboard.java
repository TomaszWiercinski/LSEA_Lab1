package pl.gda.pg.eti.lsea.lab.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;
import pl.gda.pg.eti.lsea.lab.FolderTree;
import pl.gda.pg.eti.lsea.lab.Folder;
import pl.gda.pg.eti.lsea.lab.Node;
import pl.gda.pg.eti.lsea.lab.Snippet;
import pl.gda.pg.eti.lsea.lab.testing.RandomStructure;

/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ WORK IN PROGRESS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * 
 * The point of contact between the user and the application.
 * 
 * @author Tomasz Wierciński
 */
public class Dashboard extends JFrame implements TreeSelectionListener, ActionListener {
    
    private JTree tree;
    private FolderTree tree_model;
    private JScrollPane tree_view;
    private JEditorPane snippet;
    private JScrollPane snippet_view;
    private JMenuBar menu_bar;
    
    public Dashboard(FolderTree filetree) {
        super("Dashboard");
        
        // Tree scroll pane
        tree_model = filetree;
        tree = new JTree(tree_model);
        tree.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        tree.setEditable(true);
        tree.setRootVisible(false);
        tree_view = new JScrollPane(tree);
        
        // Snippet editor pane
        snippet = new JEditorPane();
        snippet_view = new JScrollPane(snippet);
        
        // Layout
        GridLayout experimentLayout = new GridLayout(1, 2);
        this.setLayout(experimentLayout);
        
        // Menu bar
        menu_bar = new JMenuBar();
        
        // Edit menu
        JMenu menu_edit = new JMenu("Edit");
        menu_edit.setMnemonic(KeyEvent.VK_E);
        menu_edit.getAccessibleContext().setAccessibleDescription("Edit menu");
        
        // Edit -> Copy
        JMenuItem menu_edit_copy = new JMenuItem("Copy");
        menu_edit_copy.setMnemonic(KeyEvent.VK_C);
        menu_edit_copy.getAccessibleContext().setAccessibleDescription("Copy selected");
        menu_edit_copy.addActionListener(this);
        menu_edit.add(menu_edit_copy);
        
        // Edit -> Delete
        JMenuItem menu_edit_delete = new JMenuItem("Delete");
        menu_edit_delete.setMnemonic(KeyEvent.VK_D);
        menu_edit_delete.getAccessibleContext().setAccessibleDescription("Delete selected");
        menu_edit_delete.addActionListener(this);
        menu_edit.add(menu_edit_delete);
        
        // Edit -> Rename
        JMenuItem menu_edit_rename = new JMenuItem("Rename");
        menu_edit_rename.setMnemonic(KeyEvent.VK_R);
        menu_edit_rename.getAccessibleContext().setAccessibleDescription("Rename selected");
        menu_edit_rename.addActionListener(this);
        menu_edit.add(menu_edit_rename);
        
        // Edit -> Sort
        JMenu menu_edit_sort = new JMenu("Sort");
        menu_edit_sort.setMnemonic(KeyEvent.VK_S);
        menu_edit_sort.getAccessibleContext().setAccessibleDescription("Sort snippets");
        
        // Edit -> Sort -> By name
        JMenuItem menu_edit_sort_name = new JMenuItem("By name");
        menu_edit_sort_name.setMnemonic(KeyEvent.VK_N);
        menu_edit_sort_name.getAccessibleContext().setAccessibleDescription("Sort by name");
        menu_edit_sort_name.addActionListener(this);
        menu_edit_sort.add(menu_edit_sort_name);
        
        // Edit -> Sort -> By date
        JMenuItem menu_edit_sort_date = new JMenuItem("By date");
        menu_edit_sort_date.setMnemonic(KeyEvent.VK_D);
        menu_edit_sort_date.getAccessibleContext().setAccessibleDescription("Sort by date");
        menu_edit_sort_date.addActionListener(this);
        menu_edit_sort.add(menu_edit_sort_date);
        
        menu_edit.add(menu_edit_sort);
        
        menu_bar.add(menu_edit);
        
        this.setSize(400, 300);
        this.setJMenuBar(menu_bar);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.add(tree_view);
        this.add(snippet_view);
        this.pack();
        this.setVisible(true);
    }
    public Dashboard() {
        this(new FolderTree());
    }
    
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        Node node = (Node)tree.getLastSelectedPathComponent();

        if (node == null) return;

        if (node instanceof Snippet) {
            snippet.setText(((Snippet) node).get());
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) { 
        String s = e.getActionCommand();
        Node node = (Node)tree.getLastSelectedPathComponent();
        
        switch (s) {
            case "Copy":
                if (node == null)
                    break;
                try {
                    System.out.println("INFO: Creating copy of " + node.getPath());
                    Node node_copy = (Node) node.clone();
                    Folder node_parent = (Folder) node.getParent();
                    node_copy.setTitle(node.getTitle() + "_copy");
                    tree_model.insertNodeInto(node_copy, node_parent);
                    tree.repaint();
                } catch (CloneNotSupportedException ex) {
                    System.out.println("Something went wrong!");
                }
                break;
            case "Delete":
                if (node == null)
                    break;
                System.out.println("INFO: Deleting " + node.getPath());
                Folder node_parent = (Folder) node.getParent();
                tree_model.removeChild(node, node_parent);
                break;
        }
    } 
    
    /**
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~ WORK IN PROGRESS ~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Displays an interactive dashboard with an example folder structure.
     * 
     * @param args 
     */
    public static void main(String[] args) {
        // Make an example folder structure within the dashboard.
        FolderTree file_tree = new FolderTree();

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
