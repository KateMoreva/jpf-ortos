package verification;

import OS.OrtOS;
import Tasks.Task;

public class InterruptionTest {
    public static void main(String[] args) {
        final OrtOS os = new OrtOS();
        final Task taskToStart = new Task(0, Thread.MAX_PRIORITY, os);
        os.startOS(taskToStart);
        os.declareTask(2, 2);
        os.declareTask(3, Thread.MAX_PRIORITY);
//        os.printSystemInfo();
        os.shutdownOS();
        assert os.info.getTasksTookCount() == 3;
        assert os.info.getTasksDoneCount() == 3;
        assert os.info.getInterruptionsCount() == 1;
    }
}
