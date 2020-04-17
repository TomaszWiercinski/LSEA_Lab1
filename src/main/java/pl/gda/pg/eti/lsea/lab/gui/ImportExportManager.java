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
import java.util.Arrays;

/**
 * Used to import and export individual Snippets and entire Folders. Allows export of Snippets to text and binary files
 * and export of Folders to binary files. Binary files use "snip" extension.
 *
 * @author Tomasz Wierciński
 */
public class ImportExportManager {

    //region FIELDS
    /**
     * Filter for allowed Snippet export/import file extensions. Snippets can be saved as plain text files or as
     * .snip files (binary files).
     */
    private static final FileNameExtensionFilter filter_snippet =
            new FileNameExtensionFilter("Snippet files", "txt", "snip");

    /**
     * Filter for allowed Folder export file extensions. Folders can only be saved as .snip files (binary files).
     */
    private static final FileNameExtensionFilter filter_folder =
            new FileNameExtensionFilter("Dashboard files", "snip");

    /**
     * Used to prompt the user to select a file when one isn't passed as argument.
     */
    private JFileChooser file_chooser;
    // endregion

    public ImportExportManager() {
        this.file_chooser = new JFileChooser();
        this.file_chooser.setAcceptAllFileFilterUsed(false);
        this.file_chooser.setDialogTitle("Specify a file to export");
        this.file_chooser.setApproveButtonToolTipText("Export");
    }

    // region METHODS - EXPORT

    /**
     * Export Node passed as argument to a selected File.
     *
     * @param node
     * Node to export.
     * @param file
     * File to export to.
     */
    public void export_node(Node node, File file) {
        try {
            if (node instanceof Snippet) {
                // Snippets can be exported as binary (if picked file extension is .snip) or as plain text
                if (this.file_chooser.getTypeDescription(file).contains("SNIP"))
                    this.export_to_snip(node, file);
                else
                    this.export_to_text((Snippet) node, file);
            } else {
                // Folders must be exported as binary files.
                this.export_to_snip(node, file);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog (null,
                    "An IOException occurred during export!\n" + e.getMessage(),"IOException",
                    JOptionPane.ERROR_MESSAGE);
        } catch (OverlappingFileLockException e) {
            JOptionPane.showMessageDialog (null,
                    "File " + file.getName() + " is already in use!","Export warning",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Ask the user for a file and export to it a Node passed as an argument.
     *
     * @param node
     * Node to export.
     */
    public void export_node(Node node) {
        FileNameExtensionFilter used_filter;

        // Select extension filter based on Node type
        if (node instanceof Snippet)
            used_filter = ImportExportManager.filter_snippet;
        else
            used_filter = ImportExportManager.filter_folder;

        // Ask user to select a file.
        File file = this.display_export_dialog(used_filter);

        // Export
        if (file != null)
            this.export_node(node, file);
    }

    /**
     * Exports the passed Node as binary. Exports to a file passed as argument.
     *
     * @param node
     * Node to export.
     * @param file
     * File to export to.
     * @throws IOException
     * @throws OverlappingFileLockException
     */
    private void export_to_snip(Node node, File file) throws IOException, OverlappingFileLockException {
        // try-with-resources used to automatically close everything via AutoCloseable interface
        try (FileOutputStream out = new FileOutputStream(file);
             FileChannel channel = out.getChannel();
             FileLock lock = channel.tryLock()) {

            System.out.print("INFO: Exporting as binary to " + file.getAbsolutePath() + "... ");

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {

                // Serialize the node as a byte array
                oos.writeObject(node);
                oos.flush();
                byte[] object_bytes = baos.toByteArray();

                // Write to file with buffer
                ByteBuffer buffer = ByteBuffer.allocate(object_bytes.length);
                buffer.clear();
                buffer.put(object_bytes);
                buffer.flip();
                channel.write(buffer);
            }

            System.out.println("done!");
        }
    }

    /**
     * Exports the passed Snippet as plain text. Exports to a file passed as argument.
     *
     * @param snippet
     * Snippet to export.
     * @param file
     * File to export to.
     * @throws IOException
     * @throws OverlappingFileLockException
     */
    private void export_to_text(Snippet snippet, File file) throws IOException {

        try (FileOutputStream out = new FileOutputStream(file);
             FileChannel channel = out.getChannel();
             FileLock lock = channel.tryLock()) {

            System.out.print("INFO: Exporting as text to " + file.getAbsolutePath() + "... ");

            String contents = snippet.getTitle() + "\n" + snippet.getLang() + "\n" + snippet.get();
            ByteBuffer byteBuffer = ByteBuffer.wrap(contents.getBytes(StandardCharsets.ISO_8859_1));
            channel.write(byteBuffer);

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

    /**
     * Asks the user to select a file to export to. If the selected file doesn't exist, a new one will be created in its place.
     *
     * @param filter
     * A file extension filter to be applied when prompting the user to select a file.
     * @return
     * Returns a file to export to.
     */
    private File display_export_dialog(FileNameExtensionFilter filter) {
        this.file_chooser.setFileFilter(filter);

        // Ask for file
        int userSelection = this.file_chooser.showSaveDialog(null);
        File file = null;

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            file = this.file_chooser.getSelectedFile();

            // If no extension entered, select default (first from filter list).
            String file_name = file.getName();
            if (!file_name.contains(".")) {
                file = new File(file.getAbsolutePath() + "." + filter.getExtensions()[0]);
            } else {  // Otherwise, check if the extension matches those on filter list.
                // Extract file extension.
                String[] file_name_split = file_name.split("[.]");
                String file_extension = file_name_split[file_name_split.length-1];

                // If it's not on the filter list, display error and return null.
                if (!Arrays.stream(filter.getExtensions()).anyMatch(file_extension::equals)) {
                    String message = "Exporting error: " + file_extension + " is not a valid file extension!\n" +
                            "List of valid extensions for the selected node:\n ";
                    for (String ext : filter.getExtensions())
                        message += ext + " ";
                    JOptionPane.showMessageDialog (null,
                            message,
                            "File creation error",
                            JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            }

            try {
                // Attempt to create the selected file.
                if (!file.createNewFile()) {
                    // If file already exists ask about overwriting via YES/NO dialog
                    int dialogResult = JOptionPane.showConfirmDialog(null, "File " +
                                    file.getName() + " already exists!\nDo you want to overwrite it?", "Overwrite warning",
                            JOptionPane.YES_NO_OPTION);

                    // User picked "YES"
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        System.out.println("INFO: Overwriting file: " + file.getAbsolutePath());
                    } else {  // User picked "NO" or closed the prompt
                        System.out.println("INFO: File selection cancelled.");
                        file = null;
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog (null,
                        "An IOException occurred during file creation!\n" + e.getMessage(),"File creation error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        return file;
    }
    // endregion

    // region METHODS - IMPORT
    /**
     * Prompts the user to select a file and imports its contents.
     *
     * @return
     * Imported Node or null in case of failure.
     */
    public Node import_node() {
        Node imported_node = null;

        // Ask for a file.
        File file = this.display_import_dialog();

        // Attempt import.
        if (file != null)
            imported_node =  this.import_node(file);

        return imported_node;
    }

    /**
     * Imports a Node from a selected File.
     *
     * @param file
     * File to import from.
     * @return
     * Imported Node or null in case of failure.
     */
    public Node import_node(File file) {

        Node imported_node = null;

        // Extract file extension.
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
                // Plain text import - Snippets only
                int buffer_size = (int)channel.size();
                ByteBuffer buffer = ByteBuffer.allocate(buffer_size);
                channel.read(buffer);

                String file_content = new String(buffer.array(), StandardCharsets.UTF_8);
                String[] file_content_split = file_content.split("\n");

                imported_node = new Snippet(file_content_split[0], file_content_split[1],
                        String.join("\n",
                                Arrays.copyOfRange(file_content_split, 2, file_content_split.length)));
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
            System.out.println(e.getMessage());
        }

        System.out.println("done!");

        return imported_node;
    }

    /**
     * Prompts the user to select a file to import from.
     *
     * @return
     * Selected file or null if the picked file is invalid.
     */
    private File display_import_dialog() {
        // Set file filter (filter for snippets includes both txt and snip - binary)
        this.file_chooser.setFileFilter(ImportExportManager.filter_snippet);

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

    /**
     * Tests exporting and importing both as binary and plain text. Check is FileLock works properly using
     * multithreading and FileHogger class.
     * @param args
     */
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
        FileHogger hogger = new FileHogger(file, 0);
        Thread thread = new Thread(hogger);
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
            thread.interrupt();
            thread.join();

            System.out.println("\nAttempting to export again!\n");
            manager.export_node(folder, file);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
