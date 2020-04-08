package pl.gda.pg.eti.lsea.lab.cli;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;
import pl.gda.pg.eti.lsea.lab.DateComparator;
import pl.gda.pg.eti.lsea.lab.Folder;
import pl.gda.pg.eti.lsea.lab.Node;
import pl.gda.pg.eti.lsea.lab.Snippet;
import pl.gda.pg.eti.lsea.lab.testing.RandomStructure;

/**
 * The point of contact between the user and the application. Provides a command
 * line interface for traversing an example file structure.
 * @author Tomasz Wierciński
 */
public class Dashboard {
    
    /**
     * Enum used to execute command line instructions.
     */
    enum ConsoleAction {
        MOVE("move [id]: Move to the Folder, or view the Snippet.") {
            @Override
            Node execute(Node current, String[] args) {
                current = ((Folder) current).getChildren().get(Integer.parseInt(args[1]) - 1);
                return current;
            }
        }, 
        UP("up: Move up in the structure.") {
            @Override
            Node execute(Node current, String[] args) {
                Node parent = current.getParent();
                if (parent != null) {
                    current = current.getParent();
                }
                return current;
            }
        }, 
        DEL("del [id]: Delete an element in structure.") {
            @Override
            Node execute(Node current, String[] args) {
                ((Folder) current).removeChild(Integer.parseInt(args[1]) - 1);
                return current;
            }
        }, 
        COPY("copy [id]: Copy an element in structure.") {
            @Override
            Node execute(Node current, String[] args) throws CloneNotSupportedException  {
                Node selected = ((Folder)current).getChildren().get(Integer.parseInt(args[1]) - 1);
                Node selected_copy = (Node) selected.clone();
                selected_copy.setTitle(selected.getTitle() + "_copy");
                ((Folder) current).addChild(selected_copy);
                return current;
            }
        },
        SORT("sort: Sort elements lexicographically.") {
            @Override
            Node execute(Node current, String[] argd) {
                ((Folder) current).sort();
                return current;
            }
        },
        SORTDATE("sortDate: Sort elements based on date of creation.") {
            @Override
            Node execute(Node current, String[] args) {
                ((Folder) current).sort(new DateComparator());
                return current;
            }
        };
        
        private final String desc;
        
        ConsoleAction(String desc) {
            this.desc = desc;
        }
        
        @Override
        public String toString() {
            return this.desc;
        }
        abstract Node execute(Node current, String[] args) throws CloneNotSupportedException;
    }

    private Folder main_folder = new Folder("Main");  // parent of all Folders and Snippets belonging to user

    //region Getters
    public Folder getMain() {
        return main_folder;
    }
    //endregion

    /**
     * Display the snippet in the console, in a neat little box.
     * @param snippet
     */
    public static void displaySnippet(Snippet snippet) {
        // make sure the snippet isn't empty
        if (snippet.get().isEmpty()) {
            System.out.println("This snippet is empty.");
            return;
        }
        
        // split text into lines
        String[] snippet_text = snippet.get().split("\n");
        
        // get title and length of longest line (Q: What happens if title is the longest line? A: Uh)
        String snippet_title = snippet.getTitle();
        int box_width = getLongestLength(snippet_text) + 2;

        // draw a neat little box around the snippet
        System.out.println("+" + "-".repeat(box_width+1) + "+");
        System.out.println("| " + snippet.getTitle() + " ".repeat(box_width - snippet_title.length()) + "|");
        System.out.println("+" + "-".repeat(box_width+1) + "+");
        for (String line : snippet_text) {
            System.out.println("| " + line + " ".repeat(box_width-line.length()) + "|");
        }
        System.out.println("+" + "-".repeat(box_width+1) + "+");
    }
    
    /**
     * Helper function of {@link #displaySnippet(pl.gda.pg.eti.lsea.lab1.Snippet)}.
     * Checks if Node is instance of Snippet and sends it over.
     * @param node 
     */
    public static void displaySnippet(Node node) {
        if (node  instanceof Snippet) {
            displaySnippet((Snippet) node);
        }
    }

    /**
     * Display the current folder in the console with ids from 1.
     * @param folder
     */
    public static void displayFolder(Folder folder) {
        System.out.println(folder.getTitle());
        Integer i = 1;
        for (Iterator<Node> child_it = folder.getChildren().iterator(); child_it.hasNext(); i++) {
            Node child = child_it.next();
            System.out.println("|-" + i.toString() + "- " + child.getTitle());
        }
    }
    
    /**
     * Helper function of {@link #displayFolder(pl.gda.pg.eti.lsea.lab1.Folder)}.
     * Checks if Node is instance of Folder and sends it over.
     * @param node 
     */
    public static void displayFolder(Node node) {
        if (node instanceof  Folder) {
            displayFolder((Folder) node);
        }
    }

    /**
     * Finds the longest string in an array.
     * @param text array of Strings - individual lines of text
     * @return integer - length of longest string
     */
    private static int getLongestLength(String[] text) {
        int max = Integer.MIN_VALUE;

        for (String line : text) {
            if (max < line.length()) {
                max = line.length();
            }
        }

        return max;
    }

    /**
     * Creates an example folder structure with some snippets and allows you to 
     * check it out in the console.
     * @param args
     */
    public static void main(String[] args) {
        // Make an example folder structure within the dashboard.
        Dashboard my_dashboard = new Dashboard();

        Folder lsea = new Folder("LSEA");
        Folder mas = new Folder("MAS");
        
        // Random filler
        RandomStructure rs = new RandomStructure(5, 4);
        Folder random = rs.generate();
        random.setTitle("RandomStructure");
        
        Folder main_folder = my_dashboard.getMain();
        main_folder.addChild(random);
        main_folder.addChild(mas);
        main_folder.addChild(lsea);
        
        // Change dates to better show sorting by date
        try {
            SimpleDateFormat sdt = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            random.setDateCreated(sdt.parse("30-07-1998 13:28"));
            lsea.setDateCreated(new Date());
            mas.setDateCreated(sdt.parse("01-12-2034 03:21"));
        } catch (ParseException ex) {
            System.out.println("Something went wrong while parsing dates!");
        }

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

        // Simple console app for traversing the folder structure and viewing snippets.
        String response = "";
        Node current = main_folder;
        Scanner in = new Scanner(System.in);
        
        // Main loop.
        while (!response.toLowerCase().contains("exit")) {

            // Perform selected action.
            String[] response_arr = response.split(" ");
            try {
                current = ConsoleAction.valueOf(response_arr[0].toUpperCase()).execute(current, response_arr);
            } catch (NumberFormatException ex) {
                System.out.println("Second argument must be a valid integer!");
            } catch (IndexOutOfBoundsException ex) {
                System.out.println("Invalid node index!");
            } catch (ClassCastException ex) {
                System.out.println("Command invalid within Snippet!");
            } catch (CloneNotSupportedException ex) {
                System.out.println("Cloning not supported for selected Nodes!");
            } catch (IllegalArgumentException ex) {
                System.out.println("Command doesn't exist!");
            }

            // Check whether Node is Folder or Snippet to display valid commands and file structure/snippet.
            if (current instanceof Folder) {
                displayFolder(current);

                // Show valid actions for folders.
                System.out.println();
                for (ConsoleAction action : ConsoleAction.values()) {
                    System.out.println(action.toString());
                }
                
            } else if (current instanceof Snippet) {
                displaySnippet(current);

                // Show valid options for snippets.
                System.out.println("up: Exit snippet.");
            }

            // Show valid options outside of ConsoleAction
            System.out.println("exit: Quit the app.");

            // Wait for input.
            System.out.print("\nin> ");
            response = in.nextLine().toLowerCase();
        }
    }
}
