package listeners;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import OS.OrtOS;
import Tasks.Task;
import Tasks.TaskState;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.InstanceInvocation;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.bytecode.InstructionInterface;

public class TaskChangeStateListener extends ListenerAdapter {

    public TaskChangeStateListener() {
    }

    private Map<Integer, TaskState> taskToState = new ConcurrentHashMap<>();

    @Override
    public void instructionExecuted(VM vm, ThreadInfo thread, Instruction nextInsn, Instruction executedInsn) {
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

//
//    public void searchStarted(Search search) {
//        String name = search.getVM().getSUTName() + ".tra"; //TODO revert this
//        try {
//            this.writer = new PrintWriter(name);
//        } catch (FileNotFoundException e) {
//            System.out.println("Listener could not write to file " + name);
//            search.terminate();
//        }
//    }
//
//    public abstract static class NullSource {
//        protected InstructionInterface insn;
//        protected ThreadInfo ti;
//        protected ElementInfo ei;
//        protected NullTracker.NullSource cause;
//
//        NullSource(ThreadInfo ti, InstructionInterface insn, ElementInfo ei) {
//            this.ti = ti;
//            this.insn = insn;
//            this.ei = ei;
//        }
//
//        public void setCause(NullTracker.NullSource cause) {
//            this.cause = cause;
//        }
//
//        public abstract void printOn(PrintWriter var1);
//
//        void printInsnOn(PrintWriter pw) {
//            pw.printf("    instruction: [%04x] %s\n", this.insn.getPosition(), this.insn.toString());
//        }
//
//        void printThreadInfoOn(PrintWriter pw) {
//            pw.println("    executed by: " + this.ti.getName() + " (id=" + this.ti.getId() + ")");
//        }
//
//        void printMethodInfoOn(PrintWriter pw, String msg, InstructionInterface instruction) {
//            MethodInfo mi = instruction.getMethodInfo();
//            ClassInfo ci = mi.getClassInfo();
//            pw.println(msg + ci.getName() + '.' + mi.getLongName() + " (" + instruction.getFilePos() + ')');
//        }
//
//        void printCauseOn(PrintWriter pw) {
//            if (this.cause != null) {
//                pw.println("set by: ");
//                this.cause.printOn(pw);
//            }
//
//        }
//    }
}
