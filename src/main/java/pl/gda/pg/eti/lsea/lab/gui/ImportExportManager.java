package pl.gda.pg.eti.lsea.lab.gui;

import pl.gda.pg.eti.lsea.lab.Folder;
import pl.gda.pg.eti.lsea.lab.Node;
import pl.gda.pg.eti.lsea.lab.Snippet;
import pl.gda.pg.eti.lsea.lab.testing.FileHogger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;

/**
 *
 */
public class ImportExportManager {

    // Snippets can be saved as plain text files or as .snip files (binary file)
    private static final FileNameExtensionFilter filter_snippet =
            new FileNameExtensionFilter("Snippet files", "txt", "snip");
    // Folders can only be saved as .snip files (binary file)
    private static final FileNameExtensionFilter filter_folder =
            new FileNameExtensionFilter("Dashboard files", "snip");

    private static final int text_file_version = 1;
    private static final String snippet_code = "SNIP";
    private static final int snip_file_version = 1;
    private static final String folder_code = "FOLD";
    private JFileChooser file_chooser;

    public ImportExportManager() {
        this.file_chooser = new JFileChooser();
        this.file_chooser.setAcceptAllFileFilterUsed(false);
        this.file_chooser.setDialogTitle("Specify a file to export");
        this.file_chooser.setApproveButtonToolTipText("Export");
    }

    // region METHODS - EXPORT
    public void export_node(Node node, File file) {
        try {
            if (node instanceof Folder) {
                this.export_folder((Folder) node, file);
            } else if (node instanceof Snippet) {
                this.export_snippet((Snippet) node, file);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog (null,
                    "An IOException occurred during export!\n" + e.getMessage(),"IOException",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void export_node(Node node) {
        try {
            if (node instanceof Folder) {
                File file = this.display_export_dialog(ImportExportManager.filter_folder);
                this.export_folder((Folder) node, file);
            } else if (node instanceof Snippet) {
                File file = this.display_export_dialog(ImportExportManager.filter_snippet);
                this.export_snippet((Snippet) node, file);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog (null,
                    "An IOException occurred!\n" + e.getMessage(),"IOException",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void export_folder(Folder folder, File file) throws IOException{
        try (FileOutputStream out = new FileOutputStream(file);
             FileChannel channel = out.getChannel();
             FileLock lock = channel.tryLock()) {

            System.out.print("INFO: Exporting to " + file.getAbsolutePath() + "... ");

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {

                oos.writeObject(folder);
                oos.flush();
                byte[] postingBytes = baos.toByteArray();

                ByteBuffer buffer = ByteBuffer.allocate(postingBytes.length);
                buffer.clear();
                buffer.put(postingBytes);
                buffer.flip();
                channel.write(buffer);
            }

            System.out.println("done!");
        } catch (OverlappingFileLockException e) {
            JOptionPane.showMessageDialog (null,
                    "File " + file.getName() + " is already in use!","Export warning",
                    JOptionPane.ERROR_MESSAGE);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog (null,
                    "Cannot find file " + file.getName() + "!","Export warning",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void export_snippet(Snippet snippet, File file) throws IOException {

        try (FileOutputStream out = new FileOutputStream(file);
             FileChannel channel = out.getChannel();
             FileLock lock = channel.tryLock()) {

            System.out.print("INFO: Exporting to " + file.getAbsolutePath() + "... ");

            if (this.file_chooser.getTypeDescription(file).contains("SNIP")) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     ObjectOutputStream oos = new ObjectOutputStream(baos)) {

                    oos.writeObject(snippet);
                    oos.flush();
                    byte[] postingBytes = baos.toByteArray();

                    ByteBuffer buffer = ByteBuffer.allocate(postingBytes.length);
                    buffer.clear();
                    buffer.put(postingBytes);
                    buffer.flip();
                    channel.write(buffer);
                }
            } else {
                String contents = ImportExportManager.snippet_code + ";" + snippet.getTitle() + ";" + snippet.getLang()
                        + ";\n" + snippet.get();
                ByteBuffer byteBuffer = ByteBuffer.wrap(contents.getBytes(StandardCharsets.ISO_8859_1));
                channel.write(byteBuffer);
            }

            System.out.println("done!");

        } catch (OverlappingFileLockException e) {
            // File is already in use by someone else.
            JOptionPane.showMessageDialog (null,
                    "File " + file.getName() + " is already in use!","Export warning",
                    JOptionPane.ERROR_MESSAGE);
        } catch (FileNotFoundException e) {
            // File wasn't created properly or deleted.
            JOptionPane.showMessageDialog (null,
                    "Cannot find file " + file.getName() + "!","Export warning",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private File display_export_dialog(FileNameExtensionFilter filter) throws IOException {
        // TODO: Actually force a specific file extension?
        this.file_chooser.setFileFilter(filter);

        // Ask for file
        int userSelection = this.file_chooser.showSaveDialog(null);
        File file = null;

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            file = this.file_chooser.getSelectedFile();

            // If no extension entered, select default.
            if (!file.getName().contains(".")) {
                file = new File(file.getAbsolutePath() + "." + filter.getExtensions()[0]);
            }

            // If file already exists ask about overwriting
            if (!file.createNewFile()) {
                // Display YES/NO dialog
                int dialogResult = JOptionPane.showConfirmDialog(null, "File " +
                                file.getName() + " already exists!\nDo you want to overwrite it?", "Warning",
                        JOptionPane.YES_NO_OPTION);

                // User picked "YES"
                if (dialogResult == JOptionPane.YES_OPTION) {
                    System.out.println("INFO: Overwriting file: " + file.getAbsolutePath());
                    // User picked "NO" or closed the prompt
                } else {
                    System.out.println("INFO: File selection cancelled.");
                    file = null;
                }
            }
        }

        return file;
    }
    // endregion

    // region METHODS - IMPORT
    public Node import_node() {
        Node imported_node = null;
        File file = this.display_import_dialog();
        if (file != null)
            imported_node =  this.import_node(file);
        return imported_node;
    }

    public Node import_node(File file) {

        Node imported_node = null;
        String[] extension_split = file.getName().split("[.]");
        String extension = "NO EXTENSION";
        if (extension_split.length > 1)
            extension = extension_split[extension_split.length-1];


        // try-with-resources should automatically close channel via AutoCloseable interface
        try (FileInputStream in = new FileInputStream(file);
             FileChannel channel = in.getChannel()) {

            System.out.print("INFO: Importing node " + file.getAbsolutePath() + "... ");

            if (extension.equalsIgnoreCase("snip")) {
                // Binary file import - all Nodes
                try (InputStream is = Channels.newInputStream(channel) ;
                     ObjectInputStream ois = new ObjectInputStream(is)) {

                    imported_node = (Node) ois.readObject();
                }
            } else {
                // Plain text import - Snippets
                int buffer_size = (int)channel.size();
                ByteBuffer buffer = ByteBuffer.allocate(buffer_size);
                channel.read(buffer);

                String file_content = new String(buffer.array(), StandardCharsets.UTF_8);
                String[] file_content_split = file_content.split(";");

                imported_node = new Snippet(file_content_split[1], file_content_split[2],
                        file_content_split[3].substring(1));
            }
        } catch (OverlappingFileLockException e) {
            // File is already in use by someone else.
            JOptionPane.showMessageDialog (null,
                    "File " + file.getName() + " is already in use!","Import warning",
                    JOptionPane.ERROR_MESSAGE);
        } catch (FileNotFoundException e) {
            // File wasn't created properly or deleted.
            JOptionPane.showMessageDialog (null,
                    "Cannot find file " + file.getName() + "!","Import warning",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog (null,
                    "An IOException occurred during import!\n" + e.getMessage(),"IOException",
                    JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException e) {
            System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        }

        System.out.println("done!");

        return imported_node;
    }

    private File display_import_dialog() {
        // Ask for file
        int userSelection = this.file_chooser.showSaveDialog(null);
        File file = null;

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            file = this.file_chooser.getSelectedFile();

            if (file.exists()) {
                this.import_node(file);
            } else {
                JOptionPane.showMessageDialog (null,
                        "Congratulations! You've selected a file that doesn't exist!","Import warning",
                        JOptionPane.ERROR_MESSAGE);
                file = null;
            }
        }

        return file;
    }
    // endregion

    public static void main(String[] args) {
        ImportExportManager manager = new ImportExportManager();

        // Export snippet as plain text
        Snippet snippet = new Snippet("Test", "java", "System.out.println(\"Hello world!\");");
        System.out.println("Snippet before export:");
        System.out.println("Title:\t\t\t" + snippet.getTitle());
        System.out.println("Lang:\t\t\t" + snippet.getLang());
        System.out.println("Last edited:\t" + snippet.getDateEdited());
        System.out.println("Created:\t\t" + snippet.getDateCreated());
        System.out.println("Contents:\n" + snippet.get() + "\n");
        File file = new File("snippet_export_test.txt");
        manager.export_node(snippet, file);

        // Import snippet as plain text
        Snippet imported_snippet = (Snippet) manager.import_node(file);
        if (imported_snippet == null) {
            System.out.println("\nWARNING: Something went wrong during importing!\n");
        } else {
            System.out.println("\nSnippet after import from text file:");
            System.out.println("Title:\t\t\t" + imported_snippet.getTitle());
            System.out.println("Lang:\t\t\t" + imported_snippet.getLang());
            System.out.println("Last edited:\t" + imported_snippet.getDateEdited());
            System.out.println("Created:\t\t" + imported_snippet.getDateCreated());
            System.out.println("Contents:\n" + imported_snippet.get() + "\n");
        }

        // Export snippet as binary
        snippet = new Snippet("Test", "java", "System.out.println(\"Hello world!\");");
        file = new File("snippet_export_test.snip");
        manager.export_node(snippet, file);

        // Import snippet from binary file
        imported_snippet = (Snippet) manager.import_node(file);
        if (imported_snippet == null) {
            System.out.println("\nWARNING: Something went wrong during importing!\n");
        } else {
            System.out.println("\nSnippet after import from binary file:");
            System.out.println("Title:\t\t\t" + imported_snippet.getTitle());
            System.out.println("Lang:\t\t\t" + imported_snippet.getLang());
            System.out.println("Last edited:\t" + imported_snippet.getDateEdited());
            System.out.println("Created:\t\t" + imported_snippet.getDateCreated());
            System.out.println("Contents:\n" + imported_snippet.get() + "\n");
        }

        // Export folder as binary
        Folder folder = new Folder("Main Folder");
        Folder folder_sub = new Folder("Sub folder");
        folder.addChild(folder_sub);
        folder.addChild((Snippet) snippet.clone());
        folder_sub.addChild((Snippet) snippet.clone());
        file = new File("folder_export_test.snip");
        System.out.println("Folder before export:");
        System.out.println(folder.getStructure() + "\n");
        manager.export_node(folder, file);


        // Import snippet from binary file
        Folder imported_folder = (Folder) manager.import_node(file);
        if (imported_folder == null) {
            System.out.println("\nWARNING: Something went wrong during importing!\n");
        } else {
            System.out.println("\nFolder after import:");
            System.out.println(imported_folder.getStructure());
        }

        System.out.println("\nLaunching file hogger!\n");
        Thread thread = new Thread(new FileHogger(file));
        thread.start();

        // Short sleep to make sure file hogger starts hogging.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\nAttempting to export to hogged files!\n");
        manager.export_node(folder, file); // This should result in a pop-up.

        System.out.println("Waiting for file hogger to stop hogging!\n");
        try {
            thread.join();

            System.out.println("\nAttempting to export again!\n");
            manager.export_node(folder, file);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
