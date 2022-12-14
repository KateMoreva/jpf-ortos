package verification;

import OS.OrtOS;
import Tasks.Task;

import static OS.OrtOS.GLOBAL_RESOURCES_COUNT;
import static OS.OrtOS.MAX_RECOURSE_COUNT;

public class LocalResourceTest {

    public static void main(String[] args) {
        final OrtOS os = new OrtOS();
        final Task taskToStart = new Task(0, Thread.MAX_PRIORITY, os);
        os.startOS(taskToStart);
        for (int i = 0; i < 17; i++) {
            os.declareResource(100 + i, true);
        }
        os.printSystemInfo();
        os.shutdownOS();
        assert os.info.getLocalResourcesDeclared() <= MAX_RECOURSE_COUNT - GLOBAL_RESOURCES_COUNT;
        assert os.info.getLocalResourcesRequested() >= MAX_RECOURSE_COUNT;
    }
}
