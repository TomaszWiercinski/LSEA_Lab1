package pl.gda.pg.eti.lsea.lab.testing;

import java.util.Random;
import pl.gda.pg.eti.lsea.lab.Folder;
import pl.gda.pg.eti.lsea.lab.Snippet;

/**
 *
 * @author Tomasz Wierciński
 */
public class RandomStructure {
    Random rand = new Random();
    private int max_depth;
    private int max_width;
    private String[] thing = {"Box", "Square", "Cube", "Thing", "Coffee"};
    private String[] doer = {"Boxer", "Doer", "Cuber", "Brewer", "Producer", 
        "Twister", "Wrangler", "Strangler"};
    
    public RandomStructure() {
        
    }
    public RandomStructure(int max_depth, int max_width) {
        this.max_depth = max_depth;
        this.max_width = max_width;
    }
    
    private Folder generate_folder() {
        String title = thing[rand.nextInt(thing.length)] +
                doer[rand.nextInt(doer.length)] + "s";
        return new Folder(title);
    }
    
    private Snippet generate_snippet() {
        String title = thing[rand.nextInt(thing.length)] +
                doer[rand.nextInt(doer.length)] + "s";
        return new Snippet(title, "Java");
    }
    
    public Folder generate() {
        return generate(this.max_depth);
    }
    
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
}
