package listeners;

import Tasks.Task;
import Tasks.TaskState;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.vm.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ChangeStateListener extends ListenerAdapter {

    public ChangeStateListener() {
    }

    @Override
    public void instructionExecuted (VM vm, ThreadInfo thread, Instruction nextInsn, Instruction executedInsn) {
        if (executedInsn instanceof JVMInvokeInstruction) {
            JVMInvokeInstruction call = (JVMInvokeInstruction)executedInsn;
            MethodInfo methodInfo = call.getInvokedMethod();
            ClassInfo classInfo = methodInfo.getClassInfo();
            if (classInfo.isInstanceOf(Task.class.getName())) {
                if ("setState".equals(methodInfo.getName())) {
                    DynamicElementInfo elementInfo = (DynamicElementInfo) call.getArgumentValues(thread)[0];
                    System.out.println(elementInfo.isObject());
                    System.out.println(elementInfo.getNumberOfFields());
                    FieldInfo fieldInfo = elementInfo.getFieldInfo("name");
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

}
