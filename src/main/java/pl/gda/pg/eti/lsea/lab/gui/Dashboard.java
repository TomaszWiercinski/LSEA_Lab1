package pl.gda.pg.eti.lsea.lab.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import pl.gda.pg.eti.lsea.lab.*;
import pl.gda.pg.eti.lsea.lab.testing.MultithreadedSearch;
import pl.gda.pg.eti.lsea.lab.testing.RandomStructure;

/**
 * The point of contact between the user and the application.
 *
 * Uses:
 * Deep cloning
 *  - non-interactive example in {@link Folder#main(String[])}
 * Multithreading
 *  - better example and implementation in {@link MultithreadedSearch}
 * Sorting with a Comparable
 *  - implementation in {@link Node#compareTo(Node)}
 *  - non-interactive example in {@link Folder#main(String[])}
 * Sorting with a Comparator
 *  - implementation in {@link DateComparator}
 *  - comparator sorts based on date of last edit, renaming nodes counts as editing
 *
 * Used elsewhere:
 * enum
 *  - implementation and usage in {@link pl.gda.pg.eti.lsea.lab.cli.Dashboard}
 *
 * @author Tomasz Wierciński
 */
public class Dashboard extends JFrame implements TreeSelectionListener, ActionListener {
    
    private JTree tree; // JTree component - displays the tree
    private FolderTree tree_model; // TreeModel - manages tree structure
    private JScrollPane tree_view; // left JScrollPane for JTree
    private JEditorPane snippet; // displays snippet
    private JScrollPane snippet_view; // right JScrollPane for snippet
    private JMenuBar menu_bar; // the dashboard menu bar
    private BorderLayout border_layout; // organizes dashboard elements
    private JPanel label_panel = new JPanel(); // JPanel for holding labels
    private JLabel label_title = new JLabel("-"); // title of selected Node
    private JLabel label_date = new JLabel(""); // date of creation of selected Node

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a"); // date format used in dashboard

    //region Constructors
    public Dashboard(FolderTree filetree) {
        super("Dashboard");
        
        // Tree scroll pane
        tree_model = filetree;
        tree_model.addTreeModelListener(new FolderStructureListener());
        tree = new JTree(tree_model);
        tree.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        tree.setEditable(true);
        tree.setRootVisible(false);
        tree_view = new JScrollPane(tree);
        
        // Snippet scroll pane
        snippet = new JEditorPane();
        snippet_view = new JScrollPane(snippet);
        
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

        // Edit -> Add new
        JMenu menu_edit_add = new JMenu("Add new");
        menu_edit_add.setMnemonic(KeyEvent.VK_A);
        menu_edit_add.getAccessibleContext().setAccessibleDescription("Add new element");

        // Edit -> Add new -> Folder
        JMenuItem menu_edit_add_folder = new JMenuItem("Folder");
        menu_edit_add_folder.setMnemonic(KeyEvent.VK_F);
        menu_edit_add_folder.getAccessibleContext().setAccessibleDescription("Add folder");
        menu_edit_add_folder.addActionListener(this);
        menu_edit_add.add(menu_edit_add_folder);

        // Edit -> Add new -> Snippet
        JMenuItem menu_edit_add_snippet = new JMenuItem("Snippet");
        menu_edit_add_snippet.setMnemonic(KeyEvent.VK_S);
        menu_edit_add_snippet.getAccessibleContext().setAccessibleDescription("Add snippet");
        menu_edit_add_snippet.addActionListener(this);
        menu_edit_add.add(menu_edit_add_snippet);
        
        menu_edit.add(menu_edit_sort);
        menu_edit.add(menu_edit_add);

        // Search
        JMenu menu_search = new JMenu("Search");
        menu_search.setMnemonic(KeyEvent.VK_S);
        menu_search.getAccessibleContext().setAccessibleDescription("Search menu");

        // Edit -> Copy
        JMenuItem menu_search_title = new JMenuItem("By title");
        menu_search_title.setMnemonic(KeyEvent.VK_T);
        menu_search_title.getAccessibleContext().setAccessibleDescription("Search by title");
        menu_search_title.addActionListener(this);
        menu_search.add(menu_search_title);
        
        menu_bar.add(menu_edit);
        menu_bar.add(menu_search);

        // Layout
        border_layout = new BorderLayout();
        this.setLayout(border_layout);
        tree_view.setPreferredSize(new Dimension(220, 400));
        this.add(tree_view, BorderLayout.LINE_START);

        snippet_view.setPreferredSize(new Dimension(380, 400));
        this.add(snippet_view, BorderLayout.CENTER);
        label_panel.setLayout(new GridLayout(0,2));
        label_panel.add(label_title);
        label_panel.add(label_date);
        this.add(label_panel, BorderLayout.PAGE_END);

        this.setSize(400, 300);
        this.setJMenuBar(menu_bar);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }
    public Dashboard() {
        this(new FolderTree());
    }
    //endregion

    /**
     * {@inheritDoc} Sets the two labels at the bottom of the GUI to the title and date of creation of selected Node.
     * @param e the event that characterizes the change.
     */
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        Node node = (Node)tree.getLastSelectedPathComponent();
        String label_title_str = " -";
        String label_date_str = "";

        // Make sure a node is selected.
        if (node != null) {
            label_title_str = node.getTitle();
            if (node instanceof Snippet) {
                Snippet node_snippet = (Snippet) node;
                snippet.setText(node_snippet.get());
                label_title_str += "." + node_snippet.getLang();
            }
            label_date_str += "last edited: " + sdf.format(node.getDateEdited());
        }

        label_title.setText(label_title_str);
        label_date.setText(label_date_str);
    }

    /**
     * {@inheritDoc} Performs appropriate action in response to a selected menu bar item.
     * TODO: Simplify! Maybe use an enum? Like in {@link pl.gda.pg.eti.lsea.lab.cli.Dashboard}.
     * @param e the event to be processed.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Get action and related Node + TreePath.
        String s = e.getActionCommand();
        TreePath node_path = tree.getSelectionPath();
        Node node = null;
        if (node_path != null)
            node = (Node)node_path.getLastPathComponent();

        // The big switch
        switch (s) {
            // Copy the selected node and insert in the same folder as old_title + "_copy".
            case "Copy":
                if (node == null)
                    break;
                try {
                    System.out.println("INFO: Creating copy of " + node.getPath());

                    // Create a copy of selected node - deep cloning implementation usage.
                    Node node_copy = (Node) node.clone();
                    Folder node_parent = (Folder) node.getParent();
                    node_copy.setTitle(node.getTitle() + "_copy");

                    // Insert copy into structure.
                    tree_model.insertNodeInto(node_copy, node_parent);
                } catch (CloneNotSupportedException ex) {
                    System.out.println("EXCEPTION: This ain't Dolly, that's for sure.");
                }
                break;
            // Remove selected node from the structure.
            case "Delete":
                if (node == null) break;
                System.out.println("INFO: Deleting " + node.getPath());
                Folder node_parent = (Folder) node.getParent();
                tree_model.removeChild(node, node_parent);
                break;
            // Rename the selected node (can also be done via triple click).
            case "Rename":
                if (node == null) break;
                tree.startEditingAtPath(node_path);
                break;
            // Sort nodes within the selected folder lexicographically.
            case "By name":
                if (node == null) // if no node selected, sort at root folder
                    tree_model.sortChildren((Folder) tree_model.getRoot());
                else if (node instanceof Folder) {
                    Folder node_folder = (Folder) node;
                    // Comparable implementation usage.
                    tree_model.sortChildren(node_folder);
                }
                break;
            // Sort nodes within the selected folder based on creation date.
            case "By date":
                if (node == null) // if no node selected, sort at root folder
                    tree_model.sortChildren((Folder) tree_model.getRoot(), new DateComparator());
                else if (node instanceof Folder) {
                    Folder node_folder = (Folder) node;
                    // Comparator implementation usage.
                    tree_model.sortChildren(node_folder, new DateComparator());
                }
                break;
            // Insert a new folder into the selected folder.
            case "Folder":
                if (node == null) // if no node selected, insert into root folder
                    tree_model.insertNodeInto(new Folder("New Folder"), (Folder) tree_model.getRoot());
                else if (node instanceof  Folder) {
                    tree_model.insertNodeInto(new Folder("New Folder"), (Folder) node);
                }
                break;
            // Insert a new snippet into the selected folder.
            case "Snippet":
                if (node == null) // if no node selected, insert into root folder
                    tree_model.insertNodeInto(new Snippet("New Snippet", "java"), (Folder) tree_model.getRoot());
                else if (node instanceof  Folder) {
                    tree_model.insertNodeInto(new Snippet("New Snippet", "java"), (Folder) node);
                }
                break;
            // Search the entire structure by title.
            case "By title":
                // Pop-up asking for search term.
                String term = JOptionPane.showInputDialog(this, "Type search term:");
                if (term != null && !term.isBlank()) {
                    // example multithreading usage / implementation - for more information inquire within MultithreadedSearch.java
                    MultithreadedSearch search = new MultithreadedSearch(5, term, ((Folder)tree_model.getRoot()).getAllChildren());
                    search.search();
                }
                break;
        }
    } 
    
    /**
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

        // Create and display GUI.
        Dashboard db = new Dashboard(file_tree);
    }
}
