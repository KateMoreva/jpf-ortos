import EventGenerators.EventGenerator;
import OS.OrtOS;

import static OS.OrtOS.MAX_RECOURSE_COUNT;
import static OS.OrtOS.MAX_TASK_COUNT;

public class GlobalResourceTest extends BaseTest {
    public static void main(String[] args) {
        final OrtOS os = createOS();
        final TestEvent[] testEvents = new TestEvent[MAX_TASK_COUNT * 2];
        for (int i = 0; i < MAX_TASK_COUNT * 2; i += 2) {
            testEvents[i] = new TestEvent(
                100L,
                EventGenerator.OsEvent.declareTaskEvent((int) (Math.random() * 2589) + 9675, 0)
            );
            testEvents[i + 1] = new TestEvent(
                1L,
                EventGenerator.OsEvent.declareResourceEvent((int) (Math.random() * 235) + 73)
            );
        }
        final EventGenerator generator = createGenerator(os, testEvents);
        simulateOS(os, generator, MAX_TASK_COUNT * 1100);
        turnOff(os, generator);
        os.printSystemInfo();
        assert os.info.getLocalResourcesDeclared() <= MAX_RECOURSE_COUNT;
    }
}
