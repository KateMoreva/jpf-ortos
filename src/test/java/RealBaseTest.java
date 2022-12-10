import EventGenerators.EventGenerator;
import OS.OrtOS;
import OS.OsAPI;
import Tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static OS.OrtOS.MAX_PRIORITY;

public class RealBaseTest {

    protected static final Random RANDOM = new Random(2L);

    public static void simulateOS(final OsAPI os, final EventGenerator eventsGenerator, final long timeout) {
        final Task taskToStart = new Task(0, MAX_PRIORITY, os);
        try {
            System.out.println("STAAAAART");
            os.startOS(taskToStart);
            eventsGenerator.start();
            Thread.sleep(timeout);
            System.out.println("Время timeout истекло!");
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("SHUT");
            os.shutdownOS();
        }
        System.out.println("INTERRRUPT");
        eventsGenerator.interrupt();
        try {
            eventsGenerator.join();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static OrtOS createOS() {
        System.out.println("CREATE");
        return new OrtOS();
    }

    public static EventGenerator createGenerator(
            final OrtOS os,
            final TestEvent... testEvents
    ) {
        final List<Long> timeouts = new ArrayList<>();
        final List<EventGenerator.OsEvent> events = new ArrayList<>();
        System.out.println(" !!!!!!!!!!!!!!!!!! ");
        for (final TestEvent testEvent : testEvents) {
            timeouts.add(testEvent.timeout);
            events.add(testEvent.event);
        }
        System.out.println(" !!!!!!!!!!!!!!!!!! " + timeouts.size() + "   " + events.size());
        return new EventGenerator(
                timeouts.iterator(),
                events.iterator(),
                os::interpretEvent
        );
    }

    public static class TestEvent {
        public final long timeout;
        public final EventGenerator.OsEvent event;

        public TestEvent(final long timeout, final EventGenerator.OsEvent event) {
            this.timeout = timeout;
            this.event = event;
        }
    }

}
