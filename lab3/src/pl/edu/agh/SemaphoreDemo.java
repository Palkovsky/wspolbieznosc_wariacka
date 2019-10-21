package pl.edu.agh;

import java.util.ArrayList;
import java.util.List;

class Sem {
    private int val;

    public Sem(int val) {
        this.val = val;
    }

    public synchronized void P(){
        while(this.val == 0) {
            try {
                this.wait();
            } catch (InterruptedException ignored) {}
        }
        this.val -= 1;
    }

    public synchronized void V() {
        this.val += 1;
        this.notify();
    }
}

class Counter {
    private int val;

    public Counter(int val) {
        this.val = val;
    }

    public void inc() {
        this.val += 1;
    }

    public void dec() {
        this.val -= 1;
    }

    public int get() {
        return this.val;
    }
}

class CounterThread extends Thread {

    private boolean up;
    private Sem s;
    private Counter cnt;

    public CounterThread(boolean up, Sem s, Counter cnt) {
        this.up = up;
        this.s = s;
        this.cnt = cnt;
    }

    @Override
    public void run() {
        for (int i1 = 0; i1 < 10000000; ++i1) {
            s.P();
            if(up) {
                cnt.inc();
            } else {
                cnt.dec();
            }
            s.V();
        }
    }
}

public class SemaphoreDemo {

    public static void main(String[] args) {
        Counter cnt = new Counter(0);
        Sem s = new Sem(1);
        List<Thread> threadList = new ArrayList<>();

        threadList.add(new CounterThread(true, s, cnt));
        threadList.add(new CounterThread(true, s, cnt));
        threadList.add(new CounterThread(true, s, cnt));
        threadList.add(new CounterThread(true, s, cnt));
        threadList.add(new CounterThread(false, s, cnt));
        threadList.add(new CounterThread(false, s, cnt));
        threadList.add(new CounterThread(false, s, cnt));
        threadList.add(new CounterThread(false, s, cnt));

        for(Thread t: threadList) {
            t.start();
        }
        for(Thread t: threadList) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(cnt.get());
    }
}
