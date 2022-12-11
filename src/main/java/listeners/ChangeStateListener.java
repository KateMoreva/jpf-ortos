package listeners;

import Tasks.Task;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.vm.*;

public class ChangeStateListener extends ListenerAdapter {

    public ChangeStateListener() {
    }

    @Override
    public void instructionExecuted (VM vm, ThreadInfo thread, Instruction nextInsn, Instruction executedInsn) {
        if (executedInsn instanceof JVMInvokeInstruction) {
            JVMInvokeInstruction call = (JVMInvokeInstruction)executedInsn;
            MethodInfo methodInfo = call.getInvokedMethod(thread);
            ClassInfo classInfo = methodInfo.getClassInfo();
            if (classInfo.isInstanceOf(Task.class.getName())) {
                System.out.println("IN TASK METHODS");
//                FieldInfo state = classInfo.getDeclaredInstanceField("state");
            }
        }
    }

}
