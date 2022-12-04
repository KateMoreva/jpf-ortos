package OS;

import Tasks.Task;
import Tasks.TaskPriorityQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Dispatcher extends Thread {

    public final TaskPriorityQueue taskQueue;
    public final Consumer<Task> currentTaskCallback;
    public final Consumer<Task> taskDoneCallback;

    private final AtomicBoolean isFree = new AtomicBoolean(true);

    public Dispatcher(final TaskPriorityQueue taskQueue, final Consumer<Task> currentTaskCallback, final Consumer<Task> taskDoneCallback) {
        this.setName("OS.Dispatcher");
        this.taskQueue = taskQueue;
        this.currentTaskCallback = currentTaskCallback;
        this.taskDoneCallback = taskDoneCallback;
        System.out.println("Диспетчер готов к работе!");
    }

    @Override
    public void run() {
        while (true) {
            Task task = null;
            try {
                final List<Task> waitingTasks = new ArrayList<>();
                while (true) {
                    task = taskQueue.take();
                    if (task.isReady()) {
                        break;
                    }
                    waitingTasks.add(task);
                }
                for (final Task waitingTask : waitingTasks) {
                    taskQueue.add(waitingTask);
                }
                isFree.set(false);
                if (task.payload == null) {
                    // ммм, сладкая пилюля с ядом...
                    if (taskQueue.size() > 0) {
                        System.out.println("В диспетчере остались заблокированные задачи " + taskQueue.size());
                    }
                    System.out.println("Диспетчер завершает свою работу.");
                    return;
                }
                System.out.println("Диспетчер взял задачу " + task);
                currentTaskCallback.accept(task);
                task.payload.run();
                isFree.set(true);
                // isFree = true => Диспетчер нельзя прервать, когда он свободен.
                if (!task.payload.done()) {
                    System.out.println("Диспетчер вернул задачу  " + task + " в очередь");
                    taskQueue.add(task);
                } else {
                    task.releaseAllResources();
                    taskDoneCallback.accept(task);
                    System.out.println("Диспетчер отпустил задачу " + task);
                }
                // закончили работу
                currentTaskCallback.accept(null);
            } catch (final InterruptedException e) {
                isFree.set(true);
                // нас прервали!
                if (task != null && !task.payload.done()) {
                    System.out.println("Диспетчер вернул задачу  " + task + " в очередь");
                    taskQueue.add(task);
                }
                System.out.println("Исполнение задачи " + task + " прервано. Диспетчер переходит к следующей.");
            }
        }
    }

    @Override
    public void interrupt() {
        if (isFree.get()) {
            return;
        }
        super.interrupt();
    }
}
