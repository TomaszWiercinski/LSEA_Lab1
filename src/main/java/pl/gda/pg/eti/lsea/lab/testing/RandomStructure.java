package pl.gda.pg.eti.lsea.lab.testing;

import java.util.Random;
import pl.gda.pg.eti.lsea.lab.Folder;
import pl.gda.pg.eti.lsea.lab.Snippet;

/**
 * Tool for generating random Snippets and Folder structures.
 * 
 * @author Tomasz Wierciński
 */
public class RandomStructure {
    
    //region Fields
    private static Random rand = new Random(); // random number generator used to pick nouns
    private int max_depth; // maximum depth of the file tree, default=3
    private int max_width; // maximum number of nodes within a folder, default=3
    //endregion
    
    //region Static Fields
    // List of nouns used for generating titles.
    private static final String[] thing = {"Box", "Square", "Cube", "Thing", 
        "Coffee", "Implementation", "Book"};
    // List of -er nouns used for generating titles.
    private static final String[] doer = {"Snipper", "Doer", "Cuber", "Brewer", 
        "Producer", "Twister", "Wrangler", "Strangler", "Cooker", "Singer", 
        "Reader"};
    //endregion
    
    //region Constructors
    public RandomStructure() {
        this.max_depth = 3;
        this.max_width = 3;
    }
    public RandomStructure(int max_depth, int max_width) {
        this.max_depth = max_depth;
        this.max_width = max_width;
    }
    //endregion
    
    /**
     * Generate a single Folder with a random title. Title in the form of 
     * "Thing" + "Doer" + "s".
     * @return random Folder
     */
    public static Folder generate_folder() {
        String title = thing[rand.nextInt(thing.length)] +
                doer[rand.nextInt(doer.length)] + "s";
        return new Folder(title);
    }
    
    /**
     * Generate a single empty Snippet with a random title. Title in the form of
     * "Thing" + "Doer".
     * @return random Snippet
     */
    public static Snippet generate_snippet() {
        String title = thing[rand.nextInt(thing.length)] +
                doer[rand.nextInt(doer.length)];
        return new Snippet(title, "Java");
    }
    
    /**
     * Generates a random file structure with a set maximum depth and width.
     * @return root Folder of the structure
     */
    public Folder generate() {
        return generate(this.max_depth);
    }
    
    /**
     * Generates a random file structure with a set maximum depth and width.
     * @param max_depth maximum depth of structure
     * @return root Folder
     */
    private Folder generate(int max_depth) {
        Folder folder = generate_folder();
        
        if (max_depth > 1) {
            for (int i = 0; i < max_width; i++) {
                if (rand.nextDouble() < 0.5) {
                    folder.addChild(generate_snippet());
                } else {
                    folder.addChild(generate(max_depth-1));
                }
            }
        }
        
        return folder;
    }
    
    /**
     * Generates and displays the structure of a randomly generated file 
     * structure. Sets max depth and width of the structure to 5. Sorts nodes
     * lexicographically based on titles before displaying.
     * @param args 
     */
    public static void main(String[] args) {
        // generate random structure
        RandomStructure rs = new RandomStructure(5, 5);
        Folder folder_random = rs.generate();
        
        // sort lexicographically based on titles
        folder_random.sort();
        
        // display structure
        System.out.println(folder_random.getStructure());
    }
}
