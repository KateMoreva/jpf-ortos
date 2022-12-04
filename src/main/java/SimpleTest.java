import EventGenerators.EventGenerator;
import OS.OrtOS;
import OS.OsAPI;
import Tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static OS.OrtOS.MAX_PRIORITY;

public class SimpleTest {


    private final String noExceptionString = "Поток операционной системы не должен выбрасывать исключения во время работы";
    protected static final Random RANDOM = new Random(2L);

    public static void simulateOS(final OsAPI os, final EventGenerator eventsGenerator, final long timeout) {
        final Task taskToStart = new Task(0, MAX_PRIORITY, os);
        try {
            os.startOS(taskToStart);
            eventsGenerator.start();
            Thread.sleep(timeout);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } finally {
            os.shutdownOS();
        }
        eventsGenerator.interrupt();
        try {
            eventsGenerator.join();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static OrtOS createOS() {
        return new OrtOS();
    }

    public static EventGenerator createGenerator(
            final OrtOS os,
            final BaseTest.TestEvent... testEvents
    ) {
        final List<Long> timeouts = new ArrayList<>();
        final List<EventGenerator.OsEvent> events = new ArrayList<>();
        for (final BaseTest.TestEvent testEvent : testEvents) {
            timeouts.add(testEvent.timeout);
            events.add(testEvent.event);
        }
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

    public static void main(String[] args) {

//
////        if (ver()) {
//        StringBuffer buf = new StringBuffer();
//        HelloWorld hw1 = new HelloWorld(buf, "Hello, ");
//        HelloWorld hw2 = new HelloWorld(buf, "World!");
//        hw1.start(); // spin off first worker thread
//        hw2.start(); // spin off second worker thread
//        try {
//            hw1.join(); // wait for first worker to finish
//            hw2.join(); // wait for second worker
//        } catch (InterruptedException e) {
//        }
//
//        assert (buf.toString().equals("Hello, World!"));
//    }
        final OrtOS os = createOS();
        final EventGenerator generator = createGenerator(
                os,
                // Ждём 2 секунды, пока выполнится начальная задача в ОС. Затем объявляем задачу с приоритетом 5.
                new BaseTest.TestEvent(2000L, EventGenerator.OsEvent.declareTaskEvent(1, 5)),
                // Ждём 20 мс. Захватываем глобальный ресурс 0.
                new BaseTest.TestEvent(20L, EventGenerator.OsEvent.getGlobalResource(0)),
                // Ждём 100 мс. Объявляем задачу с высшим приоритетом и ожидаем, что она вытеснит предыдущую.
                new BaseTest.TestEvent(100L, EventGenerator.OsEvent.declareTaskEvent(2, 10)),
                // Ждём 20 мс. Захватываем ресурс 1.
                new BaseTest.TestEvent(20L, EventGenerator.OsEvent.getGlobalResource(1)),
                // Ждём 20 мс. Пытаемся захватить ресурс 0 - и засыпаем.
                new BaseTest.TestEvent(20L, EventGenerator.OsEvent.getGlobalResource(0)),
                // Ждём 100 мс. Проснувшаяся задача с приоритетом 5 пытается захватить ресурс 1 и тоже засыпает.
                new BaseTest.TestEvent(100L, EventGenerator.OsEvent.getGlobalResource(1))
        );
        simulateOS(os, generator, 7000L);
        os.printSystemInfo();
        assert os.info.getInterruptionsCount() == 1; // "Количество прерываний должно быть равно 1"
//            Assertions.assertEquals(os.info.getTasksDoneCount(), 1, "Только задача старта системы должна быть выполнена.");
//            Assertions.assertTrue(os.info.hasDeadlocks(), "Должно быть состояние дедлока");
//            Assertions.assertTrue(os.info.getDispatcherFinishedCorrectly(), "Диспетчер должен завершиться без ошибок");
//            Assertions.assertTrue(os.info.getOsFinishedCorrectly(), "ОС должна завершиться без ошибок");
//        }
    }

}
