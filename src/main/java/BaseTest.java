import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import EventGenerators.EventGenerator;
import OS.OrtOS;
import OS.OsAPI;
import Tasks.Task;
import gov.nasa.jpf.vm.Verify;

public class BaseTest extends Thread {

    public static void simulateOS(final OsAPI os, final EventGenerator eventsGenerator, final long timeout) {
        final Task taskToStart = new Task(0, MAX_PRIORITY, os);
        System.out.println("STAAAAART" + System.currentTimeMillis());
        os.startOS(taskToStart);
        eventsGenerator.start();
//        System.out.println("Время ! " + System.currentTimeMillis());
//        try {
//            Verify.ignoreIf(data > someValue);
//            Thread.sleep(Verify.getLongFromList(700L, 1000L));
//        } catch (final InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            os.shutdownOS();
//        }
//        eventsGenerator.interrupt();
//        try {
//            eventsGenerator.join();
//        } catch (final InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    public static void turnOff(final OsAPI os, final EventGenerator eventsGenerator) {
        System.out.println("Время ! ИСТЕКЛО" + System.currentTimeMillis());
        os.shutdownOS();
        eventsGenerator.interrupt();
        try {
            System.out.println("SHUT");
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
