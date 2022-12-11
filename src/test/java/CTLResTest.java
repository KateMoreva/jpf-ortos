import org.junit.Assert;
import org.junit.Test;

import EventGenerators.EventGenerator;
import OS.OrtOS;

public class CTLResTest extends BaseTest {
   @Test
    public void test() {
       OrtOS os = new OrtOS();
       final TestEvent[] testEvents = new TestEvent[1];
       for (int i = 0; i < 1; i++) {
           testEvents[i] = new TestEvent(
               10L,
               EventGenerator.OsEvent.declareResourceEvent(200)
           );
       }
       final EventGenerator generator = createGenerator(os, testEvents);
       simulateOS(os, generator, 200);
       os.printSystemInfo();
       Assert.assertTrue(os.info.getLocalResourcesDeclared() <= 1);
   }
}
