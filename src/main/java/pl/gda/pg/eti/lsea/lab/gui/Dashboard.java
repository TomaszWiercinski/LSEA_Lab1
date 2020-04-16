package pl.gda.pg.eti.lsea.lab.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import javax.swing.*;
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
 * @author Tomasz Wierciński
 */
public class Dashboard extends JFrame implements TreeSelectionListener, ActionListener {

    enum FileAction {
        COPY("Copy", KeyEvent.VK_C, "Copy selected") {
            @Override
            void execute(Node node, FolderTree tree_model, TreePath node_path, JTree tree) {
                if (node == null)
                    return;
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
            }
        }, DELETE("Delete", KeyEvent.VK_D, "Delete selected") {
            @Override
            void execute(Node node, FolderTree tree_model, TreePath node_path, JTree tree) {
                // Remove selected node from the structure.
                if (node == null) return;
                System.out.println("INFO: Deleting " + node.getPath());
                Folder node_parent = (Folder) node.getParent();
                tree_model.removeChild(node, node_parent);
            }
        }, RENAME("Rename", KeyEvent.VK_R, "Rename selected") {
            @Override
            void execute(Node node, FolderTree tree_model, TreePath node_path, JTree tree) {
                // Rename the selected node (can also be done via triple click).
                if (node == null) return;
                tree.startEditingAtPath(node_path);
            }
        }, SORT_NAME("By name", KeyEvent.VK_N, "Sort by name") {
            @Override
            void execute(Node node, FolderTree tree_model, TreePath node_path, JTree tree) {
                // Sort nodes within the selected folder lexicographically.
                if (node == null) // if no node selected, sort at root folder
                    tree_model.sortChildren((Folder) tree_model.getRoot());
                else if (node instanceof Folder) {
                    Folder node_folder = (Folder) node;
                    // Comparable implementation usage.
                    tree_model.sortChildren(node_folder);
                }
            }
        }, SORT_DATE("By date", KeyEvent.VK_D, "Sort by date") {
            @Override
            void execute(Node node, FolderTree tree_model, TreePath node_path, JTree tree) {
                // Sort nodes within the selected folder based on creation date.
                if (node == null) // if no node selected, sort at root folder
                    tree_model.sortChildren((Folder) tree_model.getRoot(), new DateComparator());
                else if (node instanceof Folder) {
                    Folder node_folder = (Folder) node;
                    // Comparator implementation usage.
                    tree_model.sortChildren(node_folder, new DateComparator());
                }
            }
        }, NEW_FOLDER("Folder", KeyEvent.VK_F, "Insert new folder") {
            @Override
            void execute(Node node, FolderTree tree_model, TreePath node_path, JTree tree) {
                // Insert a new folder into the selected folder.
                if (node == null) // if no node selected, insert into root folder
                    tree_model.insertNodeInto(new Folder("New Folder"), (Folder) tree_model.getRoot());
                else if (node instanceof  Folder) {
                    tree_model.insertNodeInto(new Folder("New Folder"), (Folder) node);
                }
            }
        }, NEW_SNIPPET("Snippet", KeyEvent.VK_S, "Insert new snippet") {
            @Override
            void execute(Node node, FolderTree tree_model, TreePath node_path, JTree tree) {
                // Insert a new snippet into the selected folder.
                if (node == null) // if no node selected, insert into root folder
                    tree_model.insertNodeInto(new Snippet("New Snippet", "java"), (Folder) tree_model.getRoot());
                else if (node instanceof  Folder) {
                    tree_model.insertNodeInto(new Snippet("New Snippet", "java"), (Folder) node);
                }
            }
        }, SEARCH_TITLE("By title", KeyEvent.VK_T, "Search by title") {
            @Override
            void execute(Node node, FolderTree tree_model, TreePath node_path, JTree tree) {
                // Search the entire structure by title.
                // Pop-up asking for search term.
                String term = JOptionPane.showInputDialog(this, "Type search term:");
                if (term != null && !term.isBlank()) {
                    // example multithreading usage / implementation - for more information inquire within MultithreadedSearch.java
                    MultithreadedSearch search = new MultithreadedSearch(5, term, ((Folder)tree_model.getRoot()).getAllChildren());
                    search.search();
                }
            }
        }, EXPORT_SELECTED("Export selected...", KeyEvent.VK_S, "Export selected") {
            @Override
            void execute(Node node, FolderTree tree_model, TreePath node_path, JTree tree) {
                ImportExportManager file_chooser = new ImportExportManager();
                if (node == null) // if no node selected, export entire dashboard
                file_chooser.export_node((Node) tree_model.getRoot());
                else
                file_chooser.export_node(node);
            }
        }, EXPORT_DASHBOARD("Export dashboard...", KeyEvent.VK_S, "Export entire dashboard") {
            @Override
            void execute(Node node, FolderTree tree_model, TreePath node_path, JTree tree) {
                ImportExportManager file_chooser = new ImportExportManager();
                file_chooser.export_node((Node) tree_model.getRoot());
            }
        }, IMPORT("Import...", KeyEvent.VK_I, "Import from file") {
            @Override
            void execute(Node node, FolderTree tree_model, TreePath node_path, JTree tree) {
                ImportExportManager file_chooser = new ImportExportManager();
                Node imported_node = file_chooser.import_node();
                if (imported_node != null) {
                    if (node == null) // if no node selected, insert into root folder
                        tree_model.insertNodeInto(imported_node, (Folder) tree_model.getRoot());
                    else if (node instanceof  Snippet) {  // if Snippet selected, insert into parent
                        tree_model.insertNodeInto(imported_node, (Folder) node.getParent());
                    } else if (node instanceof  Folder) {
                        tree_model.insertNodeInto(imported_node, (Folder) node);
                    }
                }
            }
        };

        private final String accessible_desc;
        private final int mnemonic;
        private final String title;

        FileAction(String title, int mnemonic, String accessible_desc) {
            this.accessible_desc = accessible_desc;
            this.mnemonic = mnemonic;
            this.title = title;
        }

        public JMenuItem getJMenuItem() {
            JMenuItem menu_item = new JMenuItem(this.title);
            menu_item.setMnemonic(this.mnemonic);
            menu_item.getAccessibleContext().setAccessibleDescription(this.accessible_desc);
            return menu_item;
        }

        public static FileAction getEnum(String value) {
            for(FileAction v : values())
                if(v.toString().equalsIgnoreCase(value)) return v;
            throw new IllegalArgumentException();
        }

        @Override
        public String toString() {
            return this.title;
        }
        abstract void execute(Node node, FolderTree tree_model, TreePath node_path, JTree tree);
    }
    
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
    private ImportExportManager file_chooser = new ImportExportManager();

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

        // region SETUP - MENU BAR
        menu_bar = new JMenuBar();

        // File menu
        JMenu menu_file = new JMenu("File");
        menu_file.setMnemonic(KeyEvent.VK_F);
        menu_file.getAccessibleContext().setAccessibleDescription("File menu");

        // File -> Copy
        JMenuItem menu_file_copy = FileAction.COPY.getJMenuItem();
        menu_file_copy.addActionListener(this);
        menu_file.add(menu_file_copy);

        // File -> Delete
        JMenuItem menu_file_delete = FileAction.DELETE.getJMenuItem();
        menu_file_delete.addActionListener(this);
        menu_file.add(menu_file_delete);

        // File -> Rename
        JMenuItem menu_file_rename = FileAction.RENAME.getJMenuItem();
        menu_file_rename.addActionListener(this);
        menu_file.add(menu_file_rename);

        // File -> Sort submenu
        JMenu menu_file_sort = new JMenu("Sort");
        menu_file_sort.setMnemonic(KeyEvent.VK_S);
        menu_file_sort.getAccessibleContext().setAccessibleDescription("Sort snippets");

        // File -> Sort -> By name
        JMenuItem menu_file_sort_name = FileAction.SORT_NAME.getJMenuItem();
        menu_file_sort_name.addActionListener(this);
        menu_file_sort.add(menu_file_sort_name);

        // File -> Sort -> By date
        JMenuItem menu_file_sort_date = FileAction.SORT_DATE.getJMenuItem();
        menu_file_sort_date.addActionListener(this);
        menu_file_sort.add(menu_file_sort_date);

        // File -> Add new submenu
        JMenu menu_file_add = new JMenu("Add new");
        menu_file_add.setMnemonic(KeyEvent.VK_A);
        menu_file_add.getAccessibleContext().setAccessibleDescription("Add new element");

        // File -> Add new -> Folder
        JMenuItem menu_file_add_folder = FileAction.NEW_FOLDER.getJMenuItem();
        menu_file_add_folder.addActionListener(this);
        menu_file_add.add(menu_file_add_folder);

        // File -> Add new -> Snippet
        JMenuItem menu_file_add_snippet = FileAction.NEW_SNIPPET.getJMenuItem();
        menu_file_add_snippet.addActionListener(this);
        menu_file_add.add(menu_file_add_snippet);

        menu_file.add(menu_file_sort);
        menu_file.add(menu_file_add);

        // Separator
        menu_file.add(new JSeparator());

        // File -> Export selected
        JMenuItem menu_file_export_snippet = FileAction.EXPORT_SELECTED.getJMenuItem();
        menu_file_export_snippet.addActionListener(this);
        menu_file.add(menu_file_export_snippet);

        // File -> Export dashboard
        JMenuItem menu_file_export_dash = FileAction.EXPORT_DASHBOARD.getJMenuItem();
        menu_file_export_dash.addActionListener(this);
        menu_file.add(menu_file_export_dash);

        // File -> Import
        JMenuItem menu_file_import = FileAction.IMPORT.getJMenuItem();
        menu_file_import.addActionListener(this);
        menu_file.add(menu_file_import);

        // Search menu
        JMenu menu_search = new JMenu("Search");
        menu_search.setMnemonic(KeyEvent.VK_S);
        menu_search.getAccessibleContext().setAccessibleDescription("Search menu");

        // Search -> By title
        JMenuItem menu_search_title = FileAction.SEARCH_TITLE.getJMenuItem();
        menu_search_title.addActionListener(this);
        menu_search.add(menu_search_title);

        menu_bar.add(menu_file);
        menu_bar.add(menu_search);
        // endregion

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

        // The big switch IS NO MORE :crab::crab::crab:
        FileAction.getEnum(s).execute(node, tree_model, node_path, tree);
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
