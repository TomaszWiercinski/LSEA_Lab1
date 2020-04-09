package pl.gda.pg.eti.lsea.lab.testing;

import pl.gda.pg.eti.lsea.lab.Folder;
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
    ExecutorService dynamic_executor; // pool of threads

    //region Constructors
    public MultithreadedSearch(String term, ArrayList<Node> list) {
        this(10, term, list);
    }
    public MultithreadedSearch(int max_threads, String term, ArrayList<Node> list) {
        this.max_threads = max_threads;
        this.term = term;
        this.list = list;
        results = new ArrayList<>();
        dynamic_executor = new ThreadPoolExecutor(1, max_threads, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
    }
    //endregion

    //region Mutators
    synchronized private void addNode(Node node) {
        results.add(node);
    }
    //endregion

    public void search() {

        List<Future> futures = new ArrayList<>();

        int size; // size of sublist
        ArrayList<List<Node>> sublists = new ArrayList<>(); // list of sublists

        for (int i = 1; i <= max_threads; i++) {
            // clear all lists
            results.clear();
            futures.clear();
            sublists.clear();

            // prepare sublists
            size = (int) Math.ceil(list.size() / (float)i);
            for (int start = 0; start < list.size(); start += size) {
                int end = Math.min(start + size, list.size());
                sublists.add(list.subList(start, end));
            }

            System.out.println("INFO: Created " + sublists.size() + " sublists.");

            // add all threads to pool
            for (int j = 0; j < i; j++) {
                futures.add(dynamic_executor.submit(new Thread(new SearchRunnable(sublists.get(j)))));
            }

            // wait for all threads
            long start = System.currentTimeMillis();
            try {
                for (Future f : futures) {
                    f.get();
                }
            } catch (InterruptedException ex) {
                System.out.println("EXCEPTION: InterruptedException!");
            } catch (ExecutionException ex) {
                System.out.println("EXCEPTION: ExecutionException!");
            }
            long end = System.currentTimeMillis();
            long duration = end-start;

            System.out.println("INFO: Found: " + results);
            System.out.println("INFO: Time taken: " + duration);
        }
    }


    /**
     * Runnable that searches its own segment for term matches.
     */
    private class SearchRunnable implements Runnable {

        private List<Node> list;

        SearchRunnable(List<Node> list) {
            this.list = list;
        }

        @Override
        public void run() {
            for (Node node : list) {
                if (node.getTitle().toLowerCase().contains(term.toLowerCase()))
                    addNode(node);
            }
        }
    }
}