package coflow.agent;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class Transformer implements ClassFileTransformer {

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
                    CtField flow = CtField.make("CoflowChannel flow;", clazz);
                    clazz.addField(flow);

                    // Instrument write method
                    CtMethod methods[] = clazz.getDeclaredMethods("write");
                    for (CtMethod method : methods) {
                        method.insertAfter("if (flow != null) flow.write($_);");
                        method.insertBefore("{" +
                            "if (flow == null) {" +
                            "flow = new CoflowChannel(this);" +
                            "}" +
                            "if (!flow.canProceed()) {" +
                            "return 0;" +
                            "}" +
                            "}");
                    }

                    // Instrument close method
                    CtMethod method = clazz.getDeclaredMethod("kill");
                    method.insertAfter("if (flow != null) flow.close();");

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
