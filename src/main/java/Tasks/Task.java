package Tasks;

import OS.UserOsAPI;
import Resources.Resource;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Task implements Comparable<Task> {
    
    public static final Task POISSON_PILL = new Task(-1, Integer.MIN_VALUE, null, null);
    
    public final int taskId;
    
    public final int priority;
    
    public final TaskPayload payload;
    
    public final List<Resource> mineResources;
    private TaskState state;
    public Resource waitingFor;
    private final UserOsAPI os;

    public Task(final int taskId, final int priority, final TaskPayload entry, final UserOsAPI os) {
        this.waitingFor = null;
        this.taskId = taskId;
        this.priority = priority;
        this.payload = entry;
        this.mineResources = Collections.synchronizedList(new LinkedList<>());
        this.os = os;
        this.state = TaskState.READY;
    }

    public Task(final int taskId, final int priority, final UserOsAPI os) {
        this.waitingFor = null;
        this.taskId = taskId;
        this.priority = priority;
        this.payload = new TaskPayload(this);
        this.mineResources = Collections.synchronizedList(new LinkedList<>());
        this.os = os;
        this.state = TaskState.READY;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public boolean isReady() {
        if (waitingFor == null) {
            return true;
        }
        return waitingFor.isFree();
    }

    @Override
    public int compareTo(final Task task) {
        return -Integer.compare(this.priority, task.priority);
    }

    @Override
    public String toString() {
        return String.format("[TASK<ID задачи: %d, Приоритет: %d>]", taskId, priority);
    }

    public void releaseAllResources() {
        for (final Resource mineResource: mineResources) {
            os.releaseResource(mineResource);
        }
    }
}
