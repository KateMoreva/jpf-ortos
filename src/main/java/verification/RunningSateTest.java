package verification;

import OS.OrtOS;
import Tasks.Task;
import gov.nasa.jpf.vm.Verify;

public class RunningSateTest {
    public static void main(String[] args) {
        final OrtOS os = new OrtOS();
        final Task taskToStart = new Task(0, Thread.MAX_PRIORITY, os);
        os.startOS(taskToStart);
        for (int i = 0; i < 17; i++) {
            os.declareTask(100 + i, Verify.getInt(0, 10));

        }
        os.shutdownOS();
        assert os.info.getDispatcherFinishedCorrectly();
        assert os.info.getOsFinishedCorrectly();
    }
}
