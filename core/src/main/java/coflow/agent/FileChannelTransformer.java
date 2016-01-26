package coflow.agent;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class FileChannelTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        byte[] byteCode = classfileBuffer;

        switch (className) {
            case "sun/nio/ch/FileChannelImpl":
                try {
                    ClassPool classPool = ClassPool.getDefault();
                    CtClass clazz = classPool.get("sun.nio.ch.FileChannelImpl");

                    System.out.println("Instrumenting " + clazz.getName());

                    classPool.importPackage("coflow");

                    String methodName = "transferToDirectly";
                    CtMethod methods[] = clazz.getDeclaredMethods(methodName);
                    for (CtMethod method : methods) {

                        String type = method.getReturnType().getName();
                        String oldMethodName = methodName + "$impl";
                        method.setName(oldMethodName);

                        CtMethod newMethod = CtNewMethod.copy(method, methodName, clazz, null);
                        String body = "{" +
                            "CoflowChannel coflowChannel = CoflowChannel.getChannel($3);" +
                            type + " result = " + oldMethodName + "($$);" +
                            "if (coflowChannel != null) { coflowChannel.write(result); }" +
                            "return result;" +
                        "}";

                        newMethod.setBody(body);
                        clazz.addMethod(newMethod);
                    }

                    byteCode = clazz.toBytecode();
                    clazz.detach();
                } catch (IOException | NotFoundException | CannotCompileException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

        return byteCode;
    }
}
