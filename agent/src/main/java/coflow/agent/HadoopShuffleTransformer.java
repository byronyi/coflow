package coflow.agent;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class HadoopShuffleTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        byte[] byteCode = classfileBuffer;

        switch (className) {
            case "org/apache/hadoop/mapred/ShuffleHandler$Shuffle":
                try {
                    ClassPool classPool = ClassPool.getDefault();
                    CtClass clazz = classPool.get("org.apache.hadoop.mapred.ShuffleHandler$Shuffle");

                    System.out.println("Instrumenting " + clazz.getName());

                    classPool.importPackage("coflow");

                    // Instrument write method
                    CtMethod methods[] = clazz.getDeclaredMethods("verifyRequest");
                    for (CtMethod method : methods) {
                        method.insertAfter("CoflowChannel.register(" +
                            "ctx.getChannel().getLocalAddress()," +
                            "ctx.getChannel().getRemoteAddress()," +
                            "$1" +
                            ");");
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
