package Tasks;

public final class TaskPayload implements Runnable {
    
    private static final long EPSILON = 0L;
    
    private long workingTime;
    
    final Task task;

    public TaskPayload(final Task task) {
        this.workingTime = 100;
        System.out.println("Working time " + workingTime);
        this.task = task;
    }

    @Override
    public void run() {
        final long goToBedTime = System.currentTimeMillis();
        long lastWakeupTime = goToBedTime;
        try {
            System.out.println("Tasks.TaskPayload: Начало выполнения задачи" + task +  ". Требуемое время: " + workingTime);
            while (true) {
                Thread.sleep(workingTime);
                final long timeAtAlarm = System.currentTimeMillis();
                if (timeAtAlarm >= lastWakeupTime + workingTime) {
                    break;
                }
                workingTime = workingTime - (timeAtAlarm - lastWakeupTime);
                lastWakeupTime = timeAtAlarm;
            }
        } catch (final InterruptedException e) {
            System.out.println("Tasks.TaskPayload: Выполнение задачи" + task +  ".  прервано.");
        }
        final long awaitTime = System.currentTimeMillis();
        workingTime -= (awaitTime - lastWakeupTime);
        if (done()) {
            System.out.println("Tasks.TaskPayload: Задача" + task +  ".  выполнена успешно.");
        } else {
            System.out.println("Tasks.TaskPayload: Вернёмся к задаче" + task +  ".  позже. Осталось: " + workingTime);
        }
    }

    public boolean done() {
        return workingTime <= EPSILON;
    }


}
