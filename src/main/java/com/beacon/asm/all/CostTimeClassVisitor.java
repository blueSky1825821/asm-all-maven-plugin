package com.beacon.asm.all;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * DESCRIPTION:
 * <P>
 * </p>
 *
 * @author WangMin
 * @since 2020/4/20 9:52 上午
 */
public class CostTimeClassVisitor extends ClassVisitor {
//    private static final String ASM_COST_TIME_LOGGER = "ASM_COST_TIME_LOGGER";
//    private boolean isLoggerFieldPresent;

    private int api;

    public CostTimeClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
        this.api = api;
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
        //静态变量初始化方法:初始化LOGGER
//        if ("<clinit>".equals(name) && !isLoggerFieldPresent) {
//            mv.visitMethodInsn(org.objectweb.asm.Opcodes.INVOKESTATIC, "org/slf4j/LoggerFactory", "getLogger",
//                               "(Ljava/lang/Class;)Lorg/slf4j/Logger;", false);
//            mv.visitFieldInsn(org.objectweb.asm.Opcodes.PUTSTATIC, "com/beacon/asm/all/CostTimeMethod", "ASM_COST_TIME_LOGGER", "Lorg/slf4j/Logger;");
//        }

        mv = new AdviceAdapter(api, mv, access, name, descriptor) {
            private boolean inject = false;


            @Override
            protected void onMethodEnter() {
                if (inject) {
                    //坐等插代码
                    mv.visitFieldInsn(GETSTATIC, "com/beacon/asm/all/ASMConstants", "ASM_COST_TIME_LOGGER",
                                      "Lorg/slf4j/Logger;");
                    mv.visitLdcInsn("========start=========");
                    mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "info",
                                       "(Ljava/lang/String;)V", true);
                    mv.visitLdcInsn(name);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                    mv.visitMethodInsn(INVOKESTATIC, "com/beacon/asm/all/MethodTimeCache",
                                       "setStartTime", "(Ljava/lang/String;J)V", false);
                }
                super.onMethodEnter();
            }

            @Override
            protected void onMethodExit(int opcode) {
                if (inject) {
                    //坐等插代码
                    mv.visitLdcInsn(name);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                    mv.visitMethodInsn(INVOKESTATIC, "com/beacon/asm/all/MethodTimeCache",
                                       "setEndTime", "(Ljava/lang/String;J)V", false);
                    mv.visitFieldInsn(GETSTATIC, "com/beacon/asm/all/ASMConstants", "ASM_COST_TIME_LOGGER",
                                      "Lorg/slf4j/Logger;");
                    mv.visitLdcInsn(name);
                    mv.visitMethodInsn(INVOKESTATIC, "com/beacon/asm/all/MethodTimeCache",
                                       "getCostTime", "(Ljava/lang/String;)Ljava/lang/String;",
                                       false);
                    mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "info",
                                       "(Ljava/lang/String;)V", true);
                    mv.visitFieldInsn(GETSTATIC, "com/beacon/asm/all/ASMConstants", "ASM_COST_TIME_LOGGER",
                                      "Lorg/slf4j/Logger;");
                    mv.visitLdcInsn("========end=========");
                    //isInterface 注意不要弄错
                    mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "info",
                                       "(Ljava/lang/String;)V", true);
                }
            }

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if (Type.getDescriptor(CostTime.class)
                        .equals(descriptor)) {
                    inject = true;
                }
                return super.visitAnnotation(descriptor, visible);
            }

//            @Override
//            public void visitEnd() {
//                if (!isLoggerFieldPresent) {
//                    FieldVisitor fv = cv.visitField(Opcodes.ACC_PRIVATE, ASM_COST_TIME_LOGGER,
//                                                    Type.getDescriptor(Logger.class), null, null);
//                    if (fv != null) {
//                        fv.visitEnd();
//                    }
//                }
//                cv.visitEnd();
//            }

        };
        return mv;
    }



//    @Override
//    public FieldVisitor visitField(int access, String name, String descriptor, String signature,
//            Object value) {
//        if (ASM_COST_TIME_LOGGER.equals(name)) {
//            isLoggerFieldPresent = true;
//        }
//        return super.visitField(access, name, descriptor, signature, value);
//    }
}
