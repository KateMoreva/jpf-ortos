package listeners;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import Tasks.Task;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.bytecode.InstructionInterface;

public class TaskChangeStateListener extends ListenerAdapter {

    public TaskChangeStateListener() {
    }

    //    private Map<Integer, String> taskStatuses = new ConcurrentHashMap();
    private List<String> taskStatuses = new CopyOnWriteArrayList<>();

    @Override
    public void instructionExecuted(VM vm, ThreadInfo thread, Instruction nextInsn, Instruction executedInsn) {

        if (executedInsn instanceof JVMInvokeInstruction) {
            JVMInvokeInstruction call = (JVMInvokeInstruction) executedInsn;
            MethodInfo methodInfo = call.getInvokedMethod();
            ClassInfo classInfo = methodInfo.getClassInfo();
            if (classInfo.isInstanceOf(Task.class.getName())) {
                if ("setState".equals(methodInfo.getName())) {
                    DynamicElementInfo elementInfo = (DynamicElementInfo) call.getArgumentValues(thread)[0];
//                    System.out.println(elementInfo.isObject());
//                    System.out.println(elementInfo.getNumberOfFields());
//                    FieldInfo fieldInfo = elementInfo.getFieldInfo("name");
//                    System.out.println(elementInfo.getObjectField("name"));
//                    System.out.println(classInfo.getClassObject().getIntField("taskId"));

                    char[] state = elementInfo.getObjectField("name").getStringChars();
                    int last = taskStatuses.size() - 1;
                    taskStatuses.add(String.valueOf(state));
                    System.out.println("Task change state: " + String.valueOf(state));
                    StackFrame frame = thread.getTopFrame();

                    if (last > 0 && taskStatuses.get(last).equals("RUNNING")) {
                        switch (taskStatuses.get(last)) {
                            case "RUNNING": {
                                if (!taskStatuses.get(last + 1).equals("WAITING")) {
                                    throw new RuntimeException("Wrong state");
                                }
                                break;
                            }
                            case "WAITING": {
                                if (!taskStatuses.get(last + 1).equals("READY") || !taskStatuses.get(last + 1).equals("WAITING")) {
                                    throw new RuntimeException("Wrong state");
                                }
                                break;
                            }
                            case "READY": {
                                if (!(taskStatuses.get(last + 1).equals("READY") || !taskStatuses.get(last + 1).equals("WAITING"))) {
                                    throw new RuntimeException("Wrong state");
                                }
                                break;
                            }
                        }
                    }
//                    System.out.println(classInfo.getInstanceField("taskId"));
//                    FieldInfo fieldInfo = classInfo.getInstanceField("taskId");
//                    System.out.println(fieldInfo.r);
//                    System.out.println(fieldInfo.toString());
//                    fieldInfo.get
//                    System.out.println(elementInfo.);
//                    try {
//                        Method method = argumentValue.getClass().getMethod("getName");
//                        System.out.println(method.invoke(argumentValue));
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
////                    System.out.println(call.getArgumentValues(thread)[0]);
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
