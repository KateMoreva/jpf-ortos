import EventGenerators.EventGenerator;
import OS.OrtOS;
import Tasks.Task;
import gov.nasa.jpf.vm.Verify;

public class SimpleTest extends BaseTest {
    public static void main3(String[] args) {
        final OrtOS ortOs = new OrtOS();

        final Thread eventsGenerator = new InterruptionTest(ortOs::interpretEvent);
//        final Thread eventsGenerator = new EventGenerators.SimpleTests.LocalResourceTest(ortOs::interpretEvent);
//        final Thread eventsGenerator = new GlobalResourceTest(ortOs::interpretEvent);
        final Task taskToStart = new Task(0, MAX_PRIORITY, ortOs);
        try {
            ortOs.startOS(taskToStart);
            eventsGenerator.start();
            //test 1 and 2
            Thread.sleep(5000);
            //test3
//            Thread.sleep(10000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } finally {
            ortOs.shutdownOS();
        }
        eventsGenerator.interrupt();
        try {
            eventsGenerator.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
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
        simulateOS(os, generator, 5000L);
        try {
            Thread.sleep(Verify.getLongFromList(5000L));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        turnOff(os, generator);
        os.printSystemInfo();
        assert os.info.getInterruptionsCount() == 1;
        // "Количество прерываний должно быть равно 1"
//            Assertions.assertEquals(os.info.getTasksDoneCount(), 1, "Только задача старта системы должна быть выполнена.");
//            Assertions.assertTrue(os.info.hasDeadlocks(), "Должно быть состояние дедлока");
//            Assertions.assertTrue(os.info.getDispatcherFinishedCorrectly(), "Диспетчер должен завершиться без ошибок");
//            Assertions.assertTrue(os.info.getOsFinishedCorrectly(), "ОС должна завершиться без ошибок");
//        }
    }

}
