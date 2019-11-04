import java.util.ArrayList;
import java.util.List;

/*
 * ModuloCounter is a shared object used for keeping track of current position in sequence.
 */
class ModuloCounter {
    private final int modulo;
    private int current;

    ModuloCounter(int modulo) {
        this.modulo = modulo;
        this.current = 0;
    }

    // Current position.
    int current() { return this.current; }
    // Module value.
    int total() { return this.modulo; }
    // Move current pointer forward wrapping at the end of sequence.
    void next() { this.current = (this.current+1)%this.modulo; }
}

/*
 * NumSeqWriter creates n-thread pool for ModuloCounter, where n is equal to ModuloCounter total.
 */
class NumSeqWriter {
    private final ModuloCounter counter;
    private int sequences_count;
    private List<Thread> threads;

    NumSeqWriter(int sequence_size, int sequences_count) {
        this.counter = new ModuloCounter(sequence_size);
        this.sequences_count = sequences_count;

        this.threads = new ArrayList<>();
        for(int i = 0; i<this.counter.total(); i++) {
            threads.add(new Thread(this.getThreadLogic(i)));
        }
    }

    void start() {
        for(Thread t: threads) {
            t.start();
        }
    }

    void join() {
        for(Thread t: threads) {
            try { t.join(); }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Runnable getThreadLogic(int i) {
        return () -> {
            for(int j = 0; j < this.sequences_count; j++) {
                synchronized (this.counter) {
                    // Wait if on wrong position.
                    while (this.counter.current() != i) {
                        try { this.counter.wait(); }
                        catch (InterruptedException ignored) {}
                    }
                    // Print (i+1), which is constant.
                    System.out.print(i+1);
                    // Move pointer forward.
                    this.counter.next();
                    // Notify other threads.
                    this.counter.notifyAll();
                }
            }
        };
    }
}

public class Main {

    private static final int SEQUENCE_SIZE = 4;
    private static final int SEQUENCES_COUNT = 25;

    public static void main(String[] args) {
        NumSeqWriter writer = new NumSeqWriter(SEQUENCE_SIZE, SEQUENCES_COUNT);
        writer.start();
        writer.join();
    }
}
