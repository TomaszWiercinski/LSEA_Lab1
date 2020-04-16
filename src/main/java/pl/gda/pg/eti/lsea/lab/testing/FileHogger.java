package pl.gda.pg.eti.lsea.lab.testing;

import pl.gda.pg.eti.lsea.lab.Node;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;

public class FileHogger implements Runnable {

    private File file;

    public FileHogger(File file) {
        this.file = file;
    }

    @Override
    public void run() {
        try (FileOutputStream out = new FileOutputStream(this.file);
             FileChannel channel = out.getChannel();
             FileLock lock = channel.tryLock()) {

            System.out.println("INFO: Hogging file \"" + file.getAbsolutePath() + "\".");

            Thread.sleep(10000);

            System.out.println("INFO: Finished hogging file \"" + file.getAbsolutePath() + "\".");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
