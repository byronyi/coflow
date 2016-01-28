package coflow.agent;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class SocketChannelTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        byte[] byteCode = classfileBuffer;

        switch (className) {
            case "sun/nio/ch/SocketChannelImpl":
                try {
                    ClassPool classPool = ClassPool.getDefault();
                    CtClass clazz = classPool.get("sun.nio.ch.SocketChannelImpl");

                    System.out.println("Instrumenting " + clazz.getName());

                    // Add flow as a field in SocketChannelImpl
                    classPool.importPackage("coflow");
                    CtField flow = CtField.make("volatile CoflowChannel coflowChannel;", clazz);
                    clazz.addField(flow);

                    String methodName = "write";
                    CtMethod methods[] = clazz.getDeclaredMethods(methodName);
                    for (CtMethod method : methods) {

                        String type = method.getReturnType().getName();
                        String oldMethodName = methodName + "$impl";
                        method.setName(oldMethodName);

                        CtMethod newMethod = CtNewMethod.copy(method, methodName, clazz, null);
                        String body = "{" +
                            "if (coflowChannel == null) { coflowChannel = new CoflowChannel(this); }" +
                            type + " result = " + oldMethodName + "($$);" +
                            "if (coflowChannel != null) { coflowChannel.write(result); }" +
                            "return result;" +
                            "}";

                        newMethod.setBody(body);
                        clazz.addMethod(newMethod);
                    }

                    // Instrument close method
                    CtMethod method = clazz.getDeclaredMethod("kill");
                    method.insertAfter("if (coflowChannel != null) coflowChannel.close();");

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
