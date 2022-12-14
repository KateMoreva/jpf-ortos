package listeners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import OS.OrtOS;
import Tasks.TaskPriorityQueue;
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

public class OSListener extends ListenerAdapter {

    public OSListener() {
    }

    private Map<Integer, TaskInnerInfo> taskInnerInfoMap = new ConcurrentHashMap<>();

    @Override
    public void executeInstruction(VM vm, ThreadInfo thread, Instruction executedInsn) {
        if (executedInsn instanceof JVMInvokeInstruction) {
            JVMInvokeInstruction call = (JVMInvokeInstruction) executedInsn;
            MethodInfo methodInfo = call.getInvokedMethod(thread);
            ClassInfo classInfo = methodInfo.getClassInfo();
            if (classInfo.isInstanceOf(OrtOS.class.getName())) {
                if ("activateTask".equals(methodInfo.getName())) {
                    InstanceInvocation instanceInvocation = (InstanceInvocation) call;
                    int calleeThis = instanceInvocation.getCalleeThis(thread);
                    ElementInfo currentTaskElementInfo = VM.getVM().getHeap().get(calleeThis);
                    DynamicElementInfo elementInfo = (DynamicElementInfo) call.getArgumentValues(thread)[0];
                    ElementInfo currentTask = currentTaskElementInfo.getObjectField("currentTask");
                    if (currentTask == null) {
                        return;
                    }
                    int currentTaskId = currentTask.getIntField("taskId");
                    int currentTaskPriority = currentTaskElementInfo.getObjectField("currentTask").getIntField("priority");
                    int newTaskPriority = elementInfo.getIntField("priority");
                    if (newTaskPriority > currentTaskPriority) {
                        taskInnerInfoMap.put(currentTaskId, new TaskInnerInfo(true, false));
                    }

                }
                if ("terminateTask".equals(methodInfo.getName())) {
                    InstanceInvocation instanceInvocation = (InstanceInvocation) call;
                    int calleeThis = instanceInvocation.getCalleeThis(thread);
                    ElementInfo currentTaskElementInfo = VM.getVM().getHeap().get(calleeThis);
                    ElementInfo currentTask = currentTaskElementInfo.getObjectField("currentTask");
                    if (currentTask == null) {
                        return;
                    }
                    int currentTaskId = currentTask.getIntField("taskId");
                    TaskInnerInfo taskInnerInfo = taskInnerInfoMap.get(currentTaskId);
                    if (taskInnerInfo != null) {
                        taskInnerInfo.previousTerminated = true;
                        taskInnerInfoMap.put(currentTaskId, taskInnerInfo);
                    }
                }

                if ("declareResource".equals(methodInfo.getName())) {
                    InstanceInvocation instanceInvocation = (InstanceInvocation) call;
                    int calleeThis = instanceInvocation.getCalleeThis(thread);
                    ElementInfo currentTaskElementInfo = VM.getVM().getHeap().get(calleeThis);
                    int localResourcesDeclared = currentTaskElementInfo.getObjectField("info").getObjectField("localResourcesDeclared").getIntField("value");
                    int globalResourcesDeclared = currentTaskElementInfo.getObjectField("info").getObjectField("globalResourcesDeclared").getIntField("value");
                    System.out.println("declareResource: Local res declared: " + localResourcesDeclared);
                    System.out.println("declareResource: Global res declared: " + globalResourcesDeclared);
                    if (globalResourcesDeclared > OrtOS.GLOBAL_RESOURCES_COUNT) { //5
                        throw new RuntimeException("Нельзя объявить больше глобальных ресурсов чем GLOBAL_RESOURCES_COUNT");
                    }
                    if (localResourcesDeclared > (OrtOS.MAX_RECOURSE_COUNT - OrtOS.GLOBAL_RESOURCES_COUNT)) { //6
                        throw new RuntimeException("Нельзя объявить больше локальных ресурсов чем MAX_RECOURSE_COUNT");
                    }
                }
            }
            if (classInfo.isInstanceOf(TaskPriorityQueue.class.getName())) {
                if ("add".equals(methodInfo.getName())) {
                    DynamicElementInfo elementInfo = (DynamicElementInfo) call.getArgumentValues(thread)[0];
                    int taskId = elementInfo.getIntField("taskId");
                    TaskInnerInfo taskInnerInfo = taskInnerInfoMap.get(taskId);
                    if (taskInnerInfo != null && taskInnerInfo.higherPriority && !taskInnerInfo.previousTerminated) { //2
                        throw new RuntimeException("Высокоприоритетная задача не может ожидать завершения работы низкоприоритетной");
                    }
                }
            }
        }
    }

    private static class TaskInnerInfo {
        public final boolean higherPriority;
        public boolean previousTerminated;

        public TaskInnerInfo(boolean higherPriority, boolean previousTerminated) {
            this.higherPriority = higherPriority;
            this.previousTerminated = previousTerminated;
        }
    }
}
