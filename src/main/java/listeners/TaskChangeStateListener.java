package listeners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import OS.OrtOS;
import Tasks.Task;
import Tasks.TaskState;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.InstanceInvocation;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class TaskChangeStateListener extends ListenerAdapter {

    public TaskChangeStateListener() {
    }

    private Map<Integer, TaskState> taskToState = new ConcurrentHashMap<>();

    @Override
    public void executeInstruction(VM vm, ThreadInfo thread, Instruction executedInsn) {
        if (executedInsn instanceof JVMInvokeInstruction) {
            JVMInvokeInstruction call = (JVMInvokeInstruction) executedInsn;
            MethodInfo methodInfo = call.getInvokedMethod(thread);
            ClassInfo classInfo = methodInfo.getClassInfo();
            if (classInfo.isInstanceOf(Task.class.getName())) {
                if ("setState".equals(methodInfo.getName())) {
                    DynamicElementInfo elementInfo = (DynamicElementInfo) call.getArgumentValues(thread)[0];
                    InstanceInvocation instanceInvocation = (InstanceInvocation) call;
                    int calleeThis = instanceInvocation.getCalleeThis(thread);
                    ElementInfo currentTaskElementInfo = VM.getVM().getHeap().get(calleeThis);
                    int taskId = currentTaskElementInfo.getIntField("taskId");
                    TaskState currentState = TaskState.valueOf(String.valueOf(currentTaskElementInfo.getObjectField("state")
                            .getObjectField("name")
                            .getStringChars()));

                    TaskState newState = TaskState.valueOf(String.valueOf(elementInfo.getObjectField("name").getStringChars()));
                    taskToState.put(taskId, newState);
                    System.out.println(taskId + " : " + currentState + " -> " + newState);
                    switch (currentState) { // 9 - 11
                        case RUNNING: {
                                if (!newState.equals(TaskState.WAITING)
                                        && !newState.equals(TaskState.DONE)
                                        && !newState.equals(TaskState.RUNNING)) {
                                    throw new RuntimeException("Wrong state: " + newState);
                                }
                                break;
                            }
                        case WAITING: {
                                if (!newState.equals(TaskState.READY)
                                        && !newState.equals(TaskState.WAITING)) {
                                    throw new RuntimeException("Wrong state: " + newState);
                                }
                                break;
                            }
                        case READY: {
                                if (!newState.equals(TaskState.WAITING)
                                        && !newState.equals(TaskState.RUNNING)
                                        && !newState.equals(TaskState.READY)) {
                                    throw new RuntimeException("Wrong state: " + newState);
                                }
                                break;
                            }
                    }
                    long waitingCount = taskToState.values()
                            .stream()
                            .filter(TaskState.WAITING::equals)
                            .count();
                    if (waitingCount > OrtOS.MAX_TASK_COUNT) {
                        throw new RuntimeException("Tasks in WAITING state are more than MAX_TASK_COUNT"); // 3
                    }
                    long runningCount = taskToState.values()
                            .stream()
                            .filter(TaskState.RUNNING::equals)
                            .count();
                    if (runningCount > 1) {
                        throw new RuntimeException("RUNNING tasks more than 1"); // 1
                    }
                    if (newState == TaskState.DONE) {
                        int taskMineResourcesCount = currentTaskElementInfo.getObjectField("mineResources")
                            .getObjectField("list")
                            .getIntField("size");
                        System.out.println("Resources count on DONE: " + taskMineResourcesCount);
                        if (taskMineResourcesCount != 0) {
                            throw new RuntimeException("Task resources on DONE must be 0"); // 6
                        }
                    }
                }
            }
        }
    }
}
