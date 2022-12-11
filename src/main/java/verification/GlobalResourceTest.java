package verification;

import OS.OrtOS;
import Tasks.Task;

import static OS.OrtOS.GLOBAL_RESOURCES_COUNT;

public class GlobalResourceTest {

    public static void main(String[] args) {
        final OrtOS os = new OrtOS();
        final Task taskToStart = new Task(0, Thread.MAX_PRIORITY, os);
        os.startOS(taskToStart);
        os.printSystemInfo();
        os.shutdownOS();
        assert os.info.getGlobalResourcesDeclared() <= GLOBAL_RESOURCES_COUNT;
    }
}
