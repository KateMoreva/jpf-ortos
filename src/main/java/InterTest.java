import EventGenerators.EventGenerator;
import OS.OrtOS;

public class InterTest extends BaseTest {
    public static void main(String[] args) {
        final OrtOS os = createOS();
        final int tasksCount = 2;
        final EventGenerator generator = createGenerator(
                os,
                new TestEvent(2000L, EventGenerator.OsEvent.declareTaskEvent(123, 5)),
                new TestEvent(200L, EventGenerator.OsEvent.declareTaskEvent(1234, 10))
        );
        simulateOS(os, generator, 5000L);
//        Assertions.assertDoesNotThrow(() -> simulateOS(os, generator, 5000L), noExceptionString);
        os.printSystemInfo();
//        Assertions.assertEquals(os.info.getInterruptionsCount(), 1, "Количество прерываний должно быть равно 1");
//        Assertions.assertEquals(os.info.getTasksDoneCount(), tasksCount + 1, "Выполненных задач должно быть на одну больше, чем было задано (так как старт системы - тоже задача)");
//        Assertions.assertTrue(os.info.getDispatcherFinishedCorrectly(), "Диспетчер должен завершиться без ошибок");
//        Assertions.assertTrue(os.info.getOsFinishedCorrectly(), "ОС должна завершиться без ошибок");
    }
}
