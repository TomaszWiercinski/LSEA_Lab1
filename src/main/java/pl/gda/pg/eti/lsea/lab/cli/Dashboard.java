package pl.gda.pg.eti.lsea.lab.cli;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import pl.gda.pg.eti.lsea.lab.Folder;
import pl.gda.pg.eti.lsea.lab.Node;
import pl.gda.pg.eti.lsea.lab.Snippet;
import pl.gda.pg.eti.lsea.lab.testing.RandomStructure;

/**
 * The point of contact between the user and the application. Currently only 
 * provides a command line interface for traversing an example file structure.
 * @author Tomasz Wierciński
 */
public class Dashboard {

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
     * Creates an example folder structure with some snippets and allows you to check it out in the console.
     * @param args
     */
    public static void main(String[] args) {
        // Make an example folder structure within the dashboard.
        Dashboard my_dashboard = new Dashboard();

        Folder lsea = new Folder("LSEA");
        Folder mas = new Folder("MAS");

        Folder main_folder = my_dashboard.getMain();

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

        // Show the file structure.
        System.out.println("File structure:");
        System.out.println(main_folder.getStructure());

        // Example title search and results.
        System.out.println("\nTitle search for \"toString\":");
        System.out.println(main_folder.searchTitle("toString"));

        // Example content search and results.
        System.out.println("\nContent search for \"@Override\":");
        ArrayList<Node> results = main_folder.searchContent("@Override");
        System.out.println(results);

        System.out.println("\nResults:");
        for (Node node : results) {
            System.out.println(node.toString());
            if (node instanceof Snippet) {
                System.out.println(((Snippet) node).get());
            }
        }

        // Simple console app for traversing the folder structure and viewing snippets.
        String response = "";
        Node current = main_folder;
        Scanner in = new Scanner(System.in);
        
        // Main loop.
        while (!response.toLowerCase().contains("exit")) {

            // Perform selected action.
            String[] response_arr = response.split(" ");
            try {
                switch (response_arr[0]) {
                    case "move":
                        current = ((Folder) current).getChildren().get(Integer.parseInt(response_arr[1]) - 1);
                        break;
                    case "del":
                        ((Folder) current).removeChild(Integer.parseInt(response_arr[1]) - 1);
                        break;
                    case "up":
                        Node parent = current.getParent();
                        if (parent != null) {
                            current = current.getParent();
                        }
                        break;
                    case "copy":
                        Node selected = ((Folder)current).getChildren().get(Integer.parseInt(response_arr[1]) - 1);
                        Node selected_copy = (Node) selected.clone();
                        selected_copy.setTitle(selected.getTitle() + "_copy");
                        ((Folder) current).addChild(selected_copy);
                        break;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Second argument must be a valid integer!");
            } catch (IndexOutOfBoundsException ex) {
                System.out.println("Invalid node index!");
            } catch (ClassCastException ex) {
                System.out.println("Command invalid within Snippet!");
            } catch (CloneNotSupportedException ex) {
                System.out.println("Cloning not supported for selected Nodes!");
            }

            // Check whether Node is Folder or Snippet to display valid commands and file structure/snippet.
            if (current instanceof Folder) {
                displayFolder(current);

                // Show valid actions for folders.
                System.out.println("\nmove [id]: Move to the Folder, or view the Snippet.");
                System.out.println("del [id]: Delete an element in structure.");
                System.out.println("copy [id]: Copy an element in structure.");
                System.out.println("up: Move up in the structure.");
            } else if (current instanceof Snippet) {
                displaySnippet(current);

                // Show valid options for snippets.
                System.out.println("up: Exit snippet.");
            }

            // Show valid options for all nodes.
            System.out.println("exit: Quit the app.");

            // Wait for input.
            System.out.print("\nin> ");
            response = in.nextLine().toLowerCase();
        }
    }
}