package listeners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import OS.OrtOS;
import Tasks.TaskState;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.InstanceInvocation;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class OSListener extends ListenerAdapter {

    public OSListener() {
    }

    private Map<Integer, TaskState> taskToState = new ConcurrentHashMap<>();

    @Override
    public void executeInstruction(VM vm, ThreadInfo thread, Instruction executedInsn) {
        if (executedInsn instanceof JVMInvokeInstruction) {
            JVMInvokeInstruction call = (JVMInvokeInstruction) executedInsn;
            MethodInfo methodInfo = call.getInvokedMethod(thread);
            ClassInfo classInfo = methodInfo.getClassInfo();
            if (classInfo.isInstanceOf(OrtOS.class.getName())) {
                if ("declareResource".equals(methodInfo.getName())) {
                    InstanceInvocation instanceInvocation = (InstanceInvocation) call;
                    int calleeThis = instanceInvocation.getCalleeThis(thread);
                    ElementInfo currentTaskElementInfo = VM.getVM().getHeap().get(calleeThis);
                    int localResourcesDeclared = currentTaskElementInfo.getObjectField("info").getObjectField("localResourcesDeclared").getIntField("value");
                    int globalResourcesDeclared = currentTaskElementInfo.getObjectField("info").getObjectField("globalResourcesDeclared").getIntField("value");
                    System.out.println("declareResource: Local res declared: " + localResourcesDeclared);
                    System.out.println("declareResource: Global res declared: " + globalResourcesDeclared);
                    if (globalResourcesDeclared > OrtOS.GLOBAL_RESOURCES_COUNT) {
                        throw new RuntimeException("Нельзя объявить больше глобальных ресурсов чем GLOBAL_RESOURCES_COUNT");
                    }
                    if (localResourcesDeclared > (OrtOS.MAX_RECOURSE_COUNT - OrtOS.GLOBAL_RESOURCES_COUNT)) {
                        throw new RuntimeException("Нельзя объявить больше локальных ресурсов чем MAX_RECOURSE_COUNT");
                    }
                }
            }
        }
    }
}
