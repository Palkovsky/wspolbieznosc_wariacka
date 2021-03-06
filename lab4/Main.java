import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import java.util.concurrent.*;
import java.util.stream.*;
import java.util.*;

/*
 * ===== WNIOSKI
 * Testowane na dwu-rdzeniowym CPU z HT.
 * ===== Metoda ze zwracaniem buforu
 * Dla dwóch wątków czas wykonania ulega skróceniu o około połowę.
 * Dla większych ilości wątków czasy wyknania są podobne co dla dwóch wątków.
 * Dla jeszcze większych ilości można zauważyć delikatne wydłużenie czasu związane z zarządzaniem wieloma wątkami.
 * ===== Metoda z edycją BufferedImage w wątku
 * Podobnie jak do metody z buforem, z tym że czasy są średnio(ale nieznacznie) większe.
 * Nie synchronizowanie na obiekcie BufferedImage nie wydaje się psuć działania programu.
 * Jeżeli BufferedImage używa zwykłych Javowych tablic(nie Collections), to wywoływanie tylko .setRGB() dla różnych koordynatów powinno być bezpieczne.
 * Jeżeli zaczeniemy synchronizować wewnątrz podwójnie zagnieżdżonej pętli for to czasy względem metody z buforem czasy wzrosną znacznie.
*/

class MandelbrotWindow extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static int THREAD_COUNT;
    private static int TASK_COUNT;
    private static int ROWS_PER_THREAD;

    private static final int MAX_ITER = 50000;
    private static final double ZOOM = 150;

    private final BufferedImage img;

    public MandelbrotWindow(int threads) throws InterruptedException, ExecutionException {
        super("Mandelbrot Set");
        setBounds(100, 100, WIDTH, HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        THREAD_COUNT = threads;
        TASK_COUNT = THREAD_COUNT;
        ROWS_PER_THREAD = (int) Math.ceil((HEIGHT*1.0)/TASK_COUNT);

        img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);


        long timeStart = System.nanoTime();

        // Initialize thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        // Submit all thread into thread pool
        List<Future<int[][]>> futures = new ArrayList<>();
        for(int chunk = 0; chunk < TASK_COUNT; chunk++) {
            futures.add(executorService.submit(rows(chunk)));
        }

        // Acquire final results and put onto canvas.
        for(int chunk = 0; chunk < TASK_COUNT; chunk++) {
            Future<int[][]> future = futures.get(chunk);
            int[][] buff = future.get();
            /*
            for(int y=0; y<ROWS_PER_THREAD; y++){
               for(int x=0; x<WIDTH; x++){
                   if(x >= WIDTH || chunk*ROWS_PER_THREAD+y >= HEIGHT) { break; }
                    img.setRGB(x, chunk*ROWS_PER_THREAD+y, buff[y][x]);
                }
            }
            */
        }

        long timeElapsed = System.nanoTime() - timeStart;
        executorService.shutdown();
        /*
        System.out.println("==== SUMMARY ====");
        System.out.println("Threads no.: " + THREAD_COUNT);
        System.out.println("Task no.: " + TASK_COUNT);
        System.out.println("Dimensions: " + WIDTH + "x" + HEIGHT);
        System.out.println("Rows per thread: " + ROWS_PER_THREAD);
        System.out.println("Max iter: " + MAX_ITER);
        System.out.println("Time elapsed: " + timeElapsed + "ns" + "/" + timeElapsed/1000 + "us" + "/" + timeElapsed/1000000 + "ms");
        System.out.println("================");
        */
        System.out.println(THREAD_COUNT+","+timeElapsed);
    }

    private Callable<int[][]> rows(final int chunk) {
        return () -> {
            int yStart = chunk*ROWS_PER_THREAD;
            int yEnd = (chunk+1)*ROWS_PER_THREAD;
            int[][] buff = new int[ROWS_PER_THREAD][WIDTH];

            double zx, zy, cX, cY, tmp;
            for (int y = yStart; y < yEnd; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    zx = zy = 0;
                    cX = (x - 400) / ZOOM;
                    cY = (y - 300) / ZOOM;
                    int iter = MAX_ITER;
                    while (zx * zx + zy * zy < 4 && iter > 0) {
                        tmp = zx * zx - zy * zy + cX;
                        zy = 2.0 * zx * zy + cY;
                        zx = tmp;
                        iter--;
                    }
                    if(x >= WIDTH ||
                       chunk*ROWS_PER_THREAD+y >= HEIGHT ||
                       y >= ROWS_PER_THREAD) { break; }
                    img.setRGB(x, chunk*ROWS_PER_THREAD+y, buff[y][x]);
                    // buff[y-yStart][x] = iter | (iter << 8);
                }
            }

            return buff;
        };
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(img, 0, 0, this);
    }
}

public class Main {
    public static void main(String[] args) {
        /*
        try {
            new MandelbrotWindow(1).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        for(int j=0; j<4; j++){
            for(int i=1; i<=10; i++){
                try {
                    new MandelbrotWindow(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
