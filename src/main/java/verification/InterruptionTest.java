package verification;

import OS.OrtOS;
import Tasks.Task;

public class InterruptionTest {
    public static void main(String[] args) {
        final OrtOS os = new OrtOS();
        final Task taskToStart = new Task(0, Thread.MAX_PRIORITY, os);
        os.startOS(taskToStart);
        os.declareTask(2, 2);
        for (int i = 0; i < 3; i++) {
            os.declareResource(100 + i, true);
        }
        for (int i = 1; i < 11; i++) {
            os.declareTask(100 + i, i);
        }
        os.declareTask(3, Thread.MAX_PRIORITY);
//        os.printSystemInfo();
        os.shutdownOS();
        assert os.info.getTasksTookCount() == 3;
        assert os.info.getTasksDoneCount() == 3;
        assert os.info.getInterruptionsCount() == 1;
    }
}
