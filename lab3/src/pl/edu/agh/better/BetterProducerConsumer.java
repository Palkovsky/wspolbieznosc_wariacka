package pl.edu.agh.better;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Buffer {
    private final int m;
    private int size;
    private final Lock lock;
    private final Condition enoughFree;
    private final Condition enoughElements;

    public Buffer(int m) {
        this.m = m;
        this.size = 0;
        lock = new ReentrantLock(true);
        enoughFree = lock.newCondition();
        enoughElements = lock.newCondition();
    }

    private int capacity () { return 2*m; }

    public int getM() {
        return m;
    }

    public void put(int n) throws InterruptedException {
        lock.lock();

        if (n > m) { throw new IllegalArgumentException("n must be less or equal to m");  }

        try {
            while(!fits(n)) { enoughFree.await(); }
            this.size += n;
            enoughElements.signal();
        } finally {
            lock.unlock();
        }
    }

    public void take(int n) throws InterruptedException {
        lock.lock();

        if (n > m) { throw new IllegalArgumentException("n must be less or equal to m"); }

        try {
            while (this.size < n) { enoughElements.await(); }
            this.size -= n;
            enoughFree.signal();
        } finally {
            lock.unlock();
        }
    }

    public boolean fits(int n) {
        return this.size + n <= capacity();
    }
}

class Producer extends Thread {
    private final Buffer buff;

    public Producer(Buffer buff) {
        this.buff = buff;
    }

    public void run() {
        Random r = new Random();

        while (true) {
            int n;
            // Lower values more often
            if(r.nextInt(10) > 6) {
                n = r.nextInt(buff.getM()) + 1;
            } else {
                n = r.nextInt(buff.getM()/1000)+1;
            }
            // Equal
            /*
            n = r.nextInt(buff.getM()) + 1;
            */
            long waitStart = System.nanoTime();
            try {
                buff.put(n);
            } catch (InterruptedException ignored) {
            }

            long timeWaited = System.nanoTime() - waitStart;
            System.out.println("P," + n + "," + timeWaited);
        }
    }
}

class Consumer extends Thread {
    private final Buffer buff;

    public Consumer(Buffer buff) {
        this.buff = buff;
    }

    public void run() {
        Random r = new Random();

        while (true) {
            int n;
            // Lower values more often
            if(r.nextInt(10) > 6) {
                n = r.nextInt(buff.getM()) + 1;
            } else {
                n = r.nextInt(buff.getM()/1000)+1;
            }
            // Equal
            /*
            n = r.nextInt(buff.getM()) + 1;
*/
            long waitStart = System.nanoTime();
            try {
                buff.take(n);
            } catch (InterruptedException ignored) {
            }

            long timeWaited = System.nanoTime() - waitStart;
            System.out.println("C," + n + "," + timeWaited);
        }
    }
}

public class BetterProducerConsumer {

    private static final int M = 100000;
    private static final int P = 1000;
    private static final int C = P;

    public static void main(String[] args) throws InterruptedException {
        // System.out.println("M:" + M + ", P:" + P + ", C:" + C);
        System.out.println("role,n,time");

        Buffer buff = new Buffer(M);

        List<Thread> threadList = new ArrayList<>();
        for(int i=0; i<P; i++) { threadList.add(new Producer(buff));  }
        for(int i=0; i<C; i++) { threadList.add(new Consumer(buff));  }
        threadList.forEach(t -> t.setDaemon(true));
        for(Thread t: threadList) { t.start(); }

        /*
        for(Thread t: threadList) {
            t.join();
        }
        */
        Thread.sleep(5000);
    }
}
