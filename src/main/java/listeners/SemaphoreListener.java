package listeners;

import OS.exception.SemaphoreException;
import Resources.Semaphore;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.InstanceInvocation;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class SemaphoreListener extends ListenerAdapter {

    public SemaphoreListener() {

    }

    @Override
    public void instructionExecuted(VM vm, ThreadInfo thread, Instruction nextInstruction, Instruction executedInsn) {
        if (!(executedInsn instanceof JVMInvokeInstruction)) {
            return;
        }
        JVMInvokeInstruction call = (JVMInvokeInstruction) executedInsn;
        MethodInfo methodInfo = call.getInvokedMethod(thread);
        ClassInfo classInfo = methodInfo.getClassInfo();
        if (!classInfo.isInstanceOf(Semaphore.class.getName())) {
            return;
        }
        if ("deactivate".equals(methodInfo.getName())) { // 8
            int currentOwnerTaskId = getTaskOwnerId(call, thread);
            if (currentOwnerTaskId == Semaphore.RECOURSE_RELEASED) {
                throw new SemaphoreException("Пытаемся освободить ресурс, который уже был освобожден");
            }
        }
        if ("activate".equals(methodInfo.getName())) { // 7
            int currentOwnerTaskId = getTaskOwnerId(call, thread);
            if (currentOwnerTaskId != Semaphore.RECOURSE_RELEASED) {
                throw new SemaphoreException("Пытаемся захватить ресурс, который уже был захвачен");
            }
        }
    }

    private int getTaskOwnerId(JVMInvokeInstruction call, ThreadInfo thread) {
        InstanceInvocation instanceInvocation = (InstanceInvocation) call;
        int calleeThis = instanceInvocation.getCalleeThis(thread);
        ElementInfo currentTaskElementInfo = VM.getVM().getHeap().get(calleeThis);
        return currentTaskElementInfo.getObjectField("ownerTaskId")
                .getIntField("value");
    }

}
