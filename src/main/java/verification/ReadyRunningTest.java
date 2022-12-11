package verification;

import OS.OrtOS;
import Tasks.Task;

public class ReadyRunningTest {
    public static void main(String[] args) {
        final OrtOS os = new OrtOS();
        final Task taskToStart = new Task(0, Thread.MAX_PRIORITY, os);
        os.startOS(taskToStart);
        os.shutdownOS();
        assert os.info.getDispatcherFinishedCorrectly();
        assert os.info.getOsFinishedCorrectly();
    }
}
