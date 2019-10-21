package pl.edu.agh;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Buffer {
    private final int m;
    private int size;

    public Buffer(int m) {
        this.m = m;
        this.size = 0;
    }

    private int capacity () { return 2*m; }

    public int getM() {
        return m;
    }

    public boolean put(int n) {
        if (n > m) {
            throw new IllegalArgumentException("n must be less or equal to m");
        }
        if(fits(n)) {
            this.size += n;
            return true;
        }
        return false;
    }

    public boolean fits(int n) {
        return this.size + n <= capacity();
    }

    public int size() {
        return size;
    }

    public boolean take(int n) {
        if (n > m) {
            throw new IllegalArgumentException("n must be less or equal to m");
        }
        if(this.size > n) {
            this.size -= n;
            return true;
        }
        return false;
    }
}

class Producer extends Thread {
    final Buffer buff;

    public Producer(Buffer buff) {
        this.buff = buff;
    }

    public void run() {
        Random r = new Random();

        while(true) {
            synchronized (buff) {
                // Lower values more often
                int n;
                if(r.nextInt(10) > 6) {
                    n = r.nextInt(buff.getM()) + 1;
                } else {
                    n = r.nextInt(buff.getM()/1000)+1;
                }

                long waitStart = System.nanoTime();
                while (!buff.fits(n)) {
                    try {
                        buff.wait();
                    } catch (InterruptedException ignored) {
                    }
                }

                long timeWaited = System.nanoTime() - waitStart;
                System.out.println("P," + n + "," + timeWaited);
                buff.put(n);
                buff.notifyAll();
            }
        }
    }
}

class Consumer extends Thread {
    final Buffer buff;

    public Consumer(Buffer buff) {
        this.buff = buff;
    }

    public void run() {
        Random r = new Random();

        while(true) {
            synchronized (buff) {
                // Lower values more often
                int n;
                if(r.nextInt(10) > 6) {
                    n = r.nextInt(buff.getM()) + 1;
                } else {
                    n = r.nextInt(buff.getM()/1000)+1;
                }

                long waitStart = System.nanoTime();
                while (buff.size() < n) {
                    try {
                        buff.wait();
                    } catch (InterruptedException ignored) {
                    }
                }

                long timeWaited = System.nanoTime() - waitStart;
                System.out.println("C," + n + "," + timeWaited);
                buff.take(n);
                buff.notifyAll();
            }
        }
    }
}

public class NaiveProducerConsumer {

    private static final int M = 100000;
    private static final int P = 1000;
    private static final int C = 1000;

    public static void main(String[] args) {
        // System.out.println("M:" + M + ", P:" + P + ", C:" + C);
        System.out.println("role,n,time");

        Buffer buff = new Buffer(M);

        List<Thread> threadList = new ArrayList<>();
        for(int i=0; i<P; i++) { threadList.add(new Producer(buff));  }
        for(int i=0; i<C; i++) { threadList.add(new Consumer(buff));  }
        for(Thread t: threadList) { t.start(); }

        // Collect output for 10 seconds
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ignored) { }
    }
}
