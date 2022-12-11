import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import OS.OrtOS;
import Tasks.Task;
import gov.nasa.jpf.annotation.JPFConfig;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

@JPFConfig({"listener=+gov.nasa.jpf.listener.PreciseRaceDetector"})
public class ExJPFTest extends TestJPF {

    @Test
    public void simple() throws RuntimeException {
        if (verifyNoPropertyViolation("listener=gov.nasa.jpf.listener.PreciseRaceDetector")) {
            final OrtOS os = new OrtOS();
            final Task taskToStart = new Task(0, Thread.MAX_PRIORITY, os);
            os.startOS(taskToStart);
            for (int i = 0; i < 17; i++) {
                os.declareTask(100 + i, Verify.getInt(0, 10));
            }
            os.shutdownOS();
        }
    }
    @Test
    public void testNoRaceCondition() throws InterruptedException {
        if (verifyNoPropertyViolation("listener=gov.nasa.jpf.listener.PreciseRaceDetector")) {
            Service srv = new AtomicService();
            Thread[] threads = new Thread[3];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(() -> {
                    srv.resetValue();
                    srv.incValue();
                    System.out.println(String.format("THREAD: %d CURRENT VALUE: %d",Thread.currentThread().getId(),srv.getValue()));
                });
            }
            for (Thread t : threads) {
                t.start();
            }
            for (Thread t : threads) {
                t.join();
            }
        }
    }

    private static class AtomicService implements Service {

        private AtomicInteger x = new AtomicInteger(0);

        @Override
        public void resetValue() {
            x.set(0);
        }

        @Override
        public void incValue() {
            x.incrementAndGet();
        }

        @Override
        public int getValue() {
            return x.intValue();
        }
    }
    public interface Service {
        int getValue();
        void incValue();
        void resetValue();
    }
}
