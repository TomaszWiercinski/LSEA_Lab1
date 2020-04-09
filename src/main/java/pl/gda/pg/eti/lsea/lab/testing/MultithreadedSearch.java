package pl.gda.pg.eti.lsea.lab.testing;

import pl.gda.pg.eti.lsea.lab.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Class used to test the speed of multithreaded search on a list of Nodes. Divides list based on number of threads and
 * assigns each thread to a segment. Repeats the operation with the number of threads ranging from 1 to max_threads.
 * 
 * @author Tomasz Wierciński
 */
public class MultithreadedSearch {

    private int max_threads; // Maximum number of used threads.
    private ArrayList<Node> results; // results of search - list of found Nodes
    private ArrayList<Node> list; // list of Nodes to search in
    private String term; // search term

    //region Constructors
    public MultithreadedSearch(String term, ArrayList<Node> list) {
        this(10, term, list);
    }
    public MultithreadedSearch(int max_threads, String term, ArrayList<Node> list) {
        this.max_threads = max_threads;
        this.term = term;
        this.list = list;
        results = new ArrayList<>();
    }
    //endregion

    //region Mutators
    synchronized private void addNode(Node node) {
        results.add(node);
    }
    //endregion

    /**
     * Divides a list based on number of threads and assigns each thread to a segment of the list. Repeats the
     * operation with the number of threads ranging from 1 to max_threads. Prints out a list of times for each number
     * of threads.
     */
    public void search() {

        List<Future> futures = new ArrayList<>(); // list of futures for each thread in pool
        ArrayList<Thread> thread_list = new ArrayList<>(); // list of threads
        ArrayList<Long> times = new ArrayList<Long>(); // list of times for each thread number

        int size; // size of sublist
        ArrayList<List<Node>> sublists = new ArrayList<>(); // list of sublists

        for (int i = 1; i <= max_threads; i++) {
            // clear all lists
            results.clear();
            futures.clear();
            sublists.clear();
            thread_list.clear();

            // prepare sublists
            size = (int) Math.ceil(list.size() / (float)i);
            for (int start = 0; start < list.size(); start += size) {
                int end = Math.min(start + size, list.size());
                sublists.add(list.subList(start, end));
            }

            // print messages
            System.out.println("INFO: Created " + sublists.size() + " sublists for " + i + " threads.");
            System.out.print("INFO: Elements in sublists: [");
            for (List<Node> l : sublists) System.out.print(" " + l.size());
            System.out.println(" ]");

            // add all threads to list and run them on prepared sublists
            long start = System.currentTimeMillis();
            for (int j = 0; j < i; j++) {
                Thread new_thread = new Thread(new SearchRunnable(sublists.get(j)));
                thread_list.add(new_thread);
                new_thread.run();
            }

            // wait for all threads
            try {
                for (Thread t : thread_list) {
                    t.join();
                }
            } catch (InterruptedException ex) {
                System.out.println("EXCEPTION: InterruptedException!");
            }

            long end = System.currentTimeMillis();
            long duration = end-start;

            System.out.println("INFO: Found " + results.size() + " elements.");
            System.out.println("INFO: Time taken: " + duration + " milliseconds");
            times.add(duration);
        }
        System.out.println("INFO: List of times taken: " + times);
    }


    /**
     * Runnable that searches its own segment for term matches.
     */
    private class SearchRunnable implements Runnable {

        private List<Node> list; // assigned list of nodes to search through

        SearchRunnable(List<Node> list) {
            this.list = list;
        }

        /**
         * Searches the assigned list for nodes with selected term in title, performs and action and adds it to a shared
         * ArrayList list via synchronized {@link #addNode(Node)} method.
         */
        @Override
        public void run() {
            for (Node node : list) {
                if (node.getTitle().toLowerCase().contains(term.toLowerCase())) {
                    // perform some operations on found nodes
                    node.getTitle().replaceAll(term, "FOUND");
                    // add them to the shared list
                    addNode(node);
                }
            }
        }
    }

    /**
     * Tests multithreaded search on a large randomly generated file structure. Example multithreading usage.
     * @param args
     */
    static public void main(String[] args) {
        int list_length = 1000000; // elements on list
        ArrayList<Node> node_list = new ArrayList<>(list_length);

        System.out.println("INFO: Generating random Nodes");
        for (int i = 0; i < list_length / 2; i++) {
            // generate random folders
            node_list.add(RandomStructure.generate_folder());
        }
        for (int i = 0; i < list_length / 2; i++) {
            // generate random snippets
            node_list.add(RandomStructure.generate_snippet());
        }

        MultithreadedSearch ms = new MultithreadedSearch(10, "Cube", node_list);
        System.out.println("INFO: Running MultithreadedSearch...\n");
        ms.search();
    }
}