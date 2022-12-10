package OS;

import EventGenerators.EventGenerator;
import Resources.Resource;
import Resources.Semaphore;
import Tasks.Task;
import Tasks.TaskPriorityQueue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class OrtOS implements OsAPI {

    public final static int MAX_TASK_COUNT = 32;
    public final static int MAX_RECOURSE_COUNT = 16;
    public final static int MAX_PRIORITY = 10;
    public static final int GLOBAL_RESOURCES_COUNT = 4;

    private final TaskPriorityQueue taskQueue;
    private final List<Resource> resourceList;
    private final Dispatcher dispatcher;
    private Task currentTask;

    private final Lock currentTaskLock = new ReentrantLock(false);
    final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    public final OsInfo info;

    public void printSystemInfo() {
        System.out.printf("Количество полученных задач: %d\n" +
                        "Количество выполненных задач: %d\n" +
                        "Количество задач, отложенных из-за ожидания ресурса: %d\n" +
                        "Количество задач, которые дождались ожидаемых ресурсов: %d\n" +
                        "Максимальное количество задач в очереди: %d\n" +
                        "Максимальное количество ресурсов: %d\n" +
                        "Количество прерываний: %d\n" +
                        "Количество объявленных локальных ресурсов: %d\n" +
                        "Диспетчер корректно завершил работу: %b\n" +
                        "ОС корректно завершила работу: %b\n" +
                        "Наличие дедлоков: %b%n",
                info.getTasksTookCount(),
                info.getTasksDoneCount(),
                info.getWaitingForResourceTasksCount(),
                info.getGotWaitingForResourceTasksCount(),
                info.getMaxTaskPull(),
                info.getMaxRecoursesPull(),
                info.getInterruptionsCount(),
                info.getLocalResourcesDeclared(),
                info.getDispatcherFinishedCorrectly(),
                info.getOsFinishedCorrectly(),
                info.hasDeadlocks()
        );
    }

    public OrtOS() {
        this.info = new OsInfo();
        this.taskQueue = new TaskPriorityQueue(MAX_TASK_COUNT);
        this.resourceList = new CopyOnWriteArrayList<>();
        this.dispatcher = new Dispatcher(this.taskQueue, takenTask -> {
            currentTaskLock.lock();
            try {
                currentTask = takenTask;
            } finally {
                currentTaskLock.unlock();
            }
            if (currentTask != null && currentTask.waitingFor != null) {
                final Resource res = currentTask.waitingFor;
                getResource(res);
                currentTask.waitingFor = null;
                info.incrementGotWaitingForResourceTasksCount();
                System.out.println("Задача " + takenTask + " получила необходимый ресурс " + res);
            }
        }, doneTask -> info.incrementTasksDoneCount());
        this.currentTask = null;
    }

    private Task getActiveTask() {
        final Task snapshotTask;
        currentTaskLock.lock();
        try {
            snapshotTask = currentTask;
        } finally {
            currentTaskLock.unlock();
        }
        return snapshotTask;
    }

    @Override
    public void activateTask(Task task) {
        final Task currentTask = getActiveTask();
        if (currentTask == null) {
            System.out.println("Диспетчер простаивает! Ставим на выполнение задачу " + task);
            terminateTask();
        } else if (currentTask.priority < task.priority) {
            System.out.println("Произошло прерывание! Активируем задачу " + task);
            info.incrementInterruptionsCount();
            terminateTask();
        }
        info.updateMaxTaskPull(taskQueue.size() + 1);
        taskQueue.add(task);
    }

    public void terminateTask() {
        dispatcher.interrupt();
    }

    public void startOS(final Task firstTask) {
        // Объявляем глобальные ресурсы. Задачи могут запрашивать к ним доступ или создавать свои ЛОКАЛЬНЫЕ переменные.
        for (int i = 1; i <= GLOBAL_RESOURCES_COUNT; ++i) {
            declareResource(i, false);
        }
        this.dispatcher.start();
        activateTask(firstTask);
    }

    @Override
    public void shutdownOS() {
        shuttingDown.set(true);
        dispatcher.taskQueue.add(Task.POISSON_PILL);
        dispatcher.interrupt();
        try {
            dispatcher.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("This is the end...");
        info.setDispatcherFinishedCorrectly();
        info.setOsFinishedCorrectly();
    }

    @Override
    public void getResource(Resource resource) {
        final Task activeTask = getActiveTask();
        if (activeTask == null) {
            throw new IllegalStateException("Кто здесь???");
        }
        final Semaphore semaphore = new Semaphore(resource, activeTask.taskId);
        P(semaphore);
    }

    @Override
    public void releaseResource(Resource resource) {
        V(resource.semaphore);
    }

    @Override
    public void P(final Semaphore newSemaphore) {
        // TODO: можно ли хватать один и тот же ресурс?
        if (newSemaphore.getResource().ownerTaskId()== newSemaphore.getOwnerTaskId()) {
            System.out.println("Одна и та же задача хватает один и тот же ресурс.");
            return;
        }
        if (!newSemaphore.getResource().isFree()) {
            /*мы пришли, а семафор перекрыт*/
            // Отдаём управление другой задаче.
            withCurrentTask(task -> {
                task.waitingFor = newSemaphore.getResource();
                info.incrementWaitingForResourceTasksCount();
                System.out.printf("Задача %s ожидает освобождение ресурса %s \n", task.toString(), newSemaphore.getResource().toString());
                terminateTask();
            });
            return;
        }
        newSemaphore.activate();
        final Task activeTask = getActiveTask();
        if (activeTask.taskId != newSemaphore.getOwnerTaskId()) {
            throw new IllegalStateException("МыЯ (не) захватили ресурс");
        }
        activeTask.mineResources.add(newSemaphore.getResource());
        System.out.printf("Ресурс %s захвачен задачей %d %n", newSemaphore.getResource().toString(), activeTask.taskId);
    }

    @Override
    public void V(final Semaphore s) {
        final Task activeTask = getActiveTask();
        if (activeTask != null && s.getOwnerTaskId() != activeTask.taskId) {
            throw new IllegalStateException("Задача пытается освободить ресурс, который ей не принадлежит.");
        }
        s.deactivate();
        // Локальные переменные удаляются после вызова releaseResource().
        if (s.getResource().isLocal) {
            resourceList.remove(s.getResource());
        }
        System.out.printf("Задача %s отпустила ресурс %s\n", activeTask.toString(), s.getResource().toString());
    }

    /**
     * Осуществляет регистрацию ресурса в системе (назначение подобно объявлению глобальных переменных в
     * языке С). Должен вызываться ДО использования ресурса в коде пользовательского приложения.
     */
    public Resource declareResource(final int resourceId, final boolean isLocal) {
        final boolean present = resourceList
                .stream()
                .anyMatch(resource -> resource.id == resourceId);
        if (present) {
            System.out.println("Попытка Ресурс с таким ID уже существует!");
            return null;
        }
        if (resourceList.size() >= MAX_RECOURSE_COUNT) {
            System.out.println("Попытка переполнения списка ресурсов!");
            return null;
        }
        final Resource resource = new Resource(resourceId, isLocal);
        System.out.println("Создан новый ресурс: " + resource);

        if (isLocal) {
            info.incrementLocalResourcesDeclared();
        }
        info.updateMaxRecoursesPull(resourceList.size() + 1);

        resourceList.add(resource);
        return resource;
    }

    /**
     * Осуществляет регистрацию задачи в системе (назначение подобно объявлению глобальных функций в языке
     * С). Должен вызываться ДО использования задачи в
     * коде пользовательского приложения.
     */
    public Task declareTask(int taskId, int priority) {
//        System.out.println("IS SHUTDOWN"+shuttingDown);
//        if (shuttingDown.get()) {
//            return null;
//        }
        final Task task = new Task(taskId, priority, this);
        System.out.println("Объявлена новая задача: " + task);
        activateTask(task);
        info.incrementTasksTookCount();
        return task;
    }

    public void withCurrentTask(final Consumer<Task> runnable) {
        currentTaskLock.lock();
        try {
            if (currentTask == null) {
                return;
            }
            runnable.accept(currentTask);
        } finally {
            currentTaskLock.unlock();
        }
    }

    public void interpretEvent(final EventGenerator.OsEvent osEvent) {
        EventGenerator.EventType eventType = osEvent.eventType;
        System.out.println("Пришло событие "+ osEvent);
        switch (eventType) {
            case declareTask:
                System.out.println("Событие: создание новой задачи.");
                declareTask(osEvent.taskId, osEvent.taskPriority);
                break;
            case declareResource:
                withCurrentTask((task) -> {
                    System.out.println("Событие: создание локального ресурса.");
                    final Resource resource = declareResource(osEvent.resourceId, true);
                    if (resource != null) {
                        System.out.println("Res npe check");
                        getResource(resource);
                    }
                });
                break;
            case getRecourse:
                withCurrentTask((task) -> {
                    System.out.println("Событие: попытка захвата глобального ресурса.");
                    final Resource resource = resourceList.get(osEvent.globalResourceIndex);
                    if (resource.isLocal) {
                        throw new IllegalStateException("Ожидался глобальный ресурс, а получен локальный.");
                    }
                    getResource(resource);
                });
                break;
            default:
                throw new IllegalStateException("Событие: генератор вышел из чата.");
        }
    }


}
