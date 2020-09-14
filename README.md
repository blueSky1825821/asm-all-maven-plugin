# asm-all-maven-plugin
## asm代码生成插件
- 目前只生成了方法耗时的代码
- 实际已经使用，完全可以放心使用

## 1 优点
- 可以对任意方法进行方法耗时统计,不只是spring管理的对象的方法
- 配置简单

## 2 接入指南

### 2.1 引入pom
每个需要统计耗时的module都需要在pom.xml引入该配置
```
<build>
  <plugins>
    <plugin>
        <groupId>com.beacon</groupId>
        <artifactId>asm-all-maven-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
        <configuration>
            <!--         代码编译输出目录           -->
            <outputDirectory>${basedir}/target/classes</outputDirectory>
            <!--        需要统计耗时的类，类中统计耗时的方法上需要加上com.beacon.asm.all.CostTime注解            -->
            <asmPackages>
                <asmPackage>com.beacon.asm.all</asmPackage>
            </asmPackages>
            <asmClasses>
                <asmClass>com.beacon.asm.all.Demo</asmClass>
            </asmClasses>
        </configuration>
        <executions>
            <execution>
                <id>package</id>
                <phase>prepare-package</phase>
                <goals>
                    <goal>time</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
  </plugins>
</build>
```
### 2.2 生成代码
```
mvn clean install -Dmaven.test.skip=true -U
mvn com.beacon:asm-all-maven-plugin:1.0-SNAPSHOT:time
查看生成的class文件
```
### 2.3 示例代码
```
package com.beacon.asm.all;

/**
 * DESCRIPTION:
 * <P>
 * </p>
 *
 * @author WangMin
 * @since 2020/4/22 9:17 下午
 */
public class Demo {
    @CostTime
    public void test() {
        System.out.println("test");
    }
}

代码生成后的class文件变为
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.beacon.asm.all;

public class Demo {
    public Demo() {
    }

    @CostTime
    public void test() {
        ASMConstants.ASM_COST_TIME_LOGGER.info("========start=========");
        MethodTimeCache.setStartTime("test", System.currentTimeMillis());
        System.out.println("test");
        MethodTimeCache.setEndTime("test", System.currentTimeMillis());
        ASMConstants.ASM_COST_TIME_LOGGER.info(MethodTimeCache.getCostTime("test"));
        ASMConstants.ASM_COST_TIME_LOGGER.info("========end=========");
    }
}
```
## 3 ASM代码生成步骤及介绍
### 3.1 安装idea插件：[ASM Bytecode outline](https://www.cnblogs.com/davenkin/p/advanced-maven-write-your-own-plugin.html)
### 3.2 代码生成：![代码生成](https://cdn.nlark.com/yuque/0/2020/png/318396/1587720156408-b34495be-e782-43aa-9631-497ea362797f.png)
### 3.3 插入代码
```
\\将生成的字节码插入代码中
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
```
至此ASM可以生成代码，并且执行mvn package时可以在对应位置增加方法耗时统计。
参考资料：http://www.wangyuwei.me/2017/01/22/手摸手增加字节码往方法体内插代码/

## 4 集成到maven插件中
- 由于这样生成实在不利于实际使用，于是想使用maven插件集成，第一次写maven插件，坑还是踩了一些，踩过就舒畅了，哈哈哈。
```
//此处附上MoJo代码
package com.beacon.asm.all;

import com.alibaba.fastjson.JSON;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;

/**
 * DESCRIPTION:
 * <P>
 * </p>
 *
 * @author WangMin
 * @since 2020/4/24 10:50 上午
 */
@Mojo(name = "time")
public class TimeMojo extends AbstractMojo {

    /**
     * Maven内部提供的参数,通过${}来进行获取
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @Parameter(property = "basedir", defaultValue = "${project.build.directory}")
    private File outputDirectory;

    /**
     * @parameter expression = "${project.basedir}"
     * @readonly
     * @required
     */
    @Parameter(property = "baseDir", defaultValue = "${project.basedir}")
    private File baseDir;

    /**
     * @parameter expression = "${project.build.sourceDirectory}"
     * @readonly
     * @required
     */
    @Parameter(property = "sourceDirectory", defaultValue = "${project.build.sourceDirectory}")
    private File sourceDirectory;

    /**
     * @parameter expression = "${project.build.testSourceDirectory}"
     * @readonly
     * @required
     */
    @Parameter(property = "testSourceDirectory",
               defaultValue = "${project.build.testSourceDirectory}")
    private File testSourceDirectory;

    /**
     * @parameter expression = "${project.build.resources}"
     * @readonly
     * @required maven所标识的资源，也就是我们工程中对应maven目录规划下的文件
     */
    @Parameter(defaultValue = "${project.build.resources}", property = "resources")
    private List<Resource> resources;

    /**
     * @parameter expression = "${project.build.testResources}"
     * @readonly
     * @required
     */
    @Parameter(property = "testResources", defaultValue = "${project.build.testResources}")
    private List<Resource> testResources;

    /**
     * 也可以获取自定义的属性值，在插件使用者的配置文件中进行传入
     * 我们这里定义的这个属性值的含义是包含的文件后缀
     *
     * @parameter
     */
    @Parameter(property = "includes")
    private Class[] includes;

    /**
     * needasms
     *
     * @parameter
     */
    @Parameter(property = "needasms")
    private String[] needasms;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("#######out" + outputDirectory.getPath());
        getLog().info("#######include" + JSON.toJSONString(includes));
        getLog().info("#######" + new Class[] { Demo.class }[0].getName());
        try {
            initASM();
        } catch (IOException e) {
            getLog().error(e);
        }
    }

    private void initASM() throws IOException {
        //做单元测试使用
//        needasms = new String[] { "com.beacon.asm.all.Demo" };
//        outputDirectory = new File(
//                "/Users/wangmin/Study/code/asm-all-maven-plugin/target/classes");
        if (needasms == null || needasms.length == 0) {
            return;
        }

        for (String needasm : needasms) {
            getLog().info("###asm needasm:" + needasm);
            //校验参数
            String outPath = outputDirectory.getPath() + "/" + needasm.replace(".", "/") + ".class";
            //将二进制流写到out/下 注意一定要是out
            getLog().info("###asm outPath: " + outPath);
            try (InputStream inputStream = new FileInputStream(new File(outPath))) {
                ClassReader cr = new ClassReader(inputStream);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
                ClassVisitor cv = new CostTimeClassVisitor(Opcodes.ASM8, cw);
                cr.accept(cv, EXPAND_FRAMES);

                // 获取生成的class文件对应的二进制流
                byte[] code = cw.toByteArray();
                try (FileOutputStream fos = new FileOutputStream(outPath)) {
                    fos.write(code);
                }
            }
        }
    }
}
```
- 有疑问 原来needasms 定义为 Class[] needasms，由于下文中
```
//needasm 声明为class
for (Class<?> needasm : needasms) {
    getLog().info("#######include:" + JSON.toJSONString(needasm));
   
    //校验参数
    String outPath = outputDirectory.getPath() + "/" + needasm.replace(".", "/") + ".class";
    getLog().info("#######outPath: " + outPath);
    try (InputStream inputStream = new FileInputStream(new File(outPath))) {
        //此处not found class？？why
        ClassReader cr = new ClassReader(needasm.getName());
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new CostTimeClassVisitor(Opcodes.ASM8, cw);
        cr.accept(cv, EXPAND_FRAMES);

        // 获取生成的class文件对应的二进制流
        byte[] code = cw.toByteArray();

        //将二进制流写到out/下
        //注意一定要是out
        getLog().info("#######outPath: " + outPath);
        try (FileOutputStream fos = new FileOutputStream(outPath)) {
            fos.write(code);
        }
    }
}
```
- 一直处理不了，只能采取其他措施，就是使用该方法:
```
  /**
   * Constructs a new {@link ClassReader} object.
   *
   * @param inputStream an input stream of the JVMS ClassFile structure to be read. This input
   *     stream must contain nothing more than the ClassFile structure itself. It is read from its
   *     current position to its end.
   * @throws IOException if a problem occurs during reading.
   */
  public ClassReader(final InputStream inputStream) throws IOException {
    this(readStream(inputStream, false));
  }
```
参考资料：https://www.cnblogs.com/davenkin/p/advanced-maven-write-your-own-plugin.html
