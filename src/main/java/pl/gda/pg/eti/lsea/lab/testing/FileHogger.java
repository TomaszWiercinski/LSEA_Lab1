package pl.gda.pg.eti.lsea.lab.testing;

import pl.gda.pg.eti.lsea.lab.Node;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;

public class FileHogger implements Runnable {

    private File file;
    private int millis;

    public FileHogger(File file, int millis) {
        this.millis = millis;
        this.file = file;
    }
    public FileHogger(File file) {
        this(file, 10000);
    }

    @Override
    public synchronized void run() {
        try (FileOutputStream out = new FileOutputStream(this.file);
             FileChannel channel = out.getChannel();
             FileLock lock = channel.tryLock()) {

            System.out.println("INFO: Hogging file \"" + file.getAbsolutePath() + "\".");

            try {
                this.wait(millis);
            } catch (InterruptedException e) {
                System.out.println("INFO: Finishing early!");
            }

            System.out.println("INFO: Finished hogging file \"" + file.getAbsolutePath() + "\".");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
