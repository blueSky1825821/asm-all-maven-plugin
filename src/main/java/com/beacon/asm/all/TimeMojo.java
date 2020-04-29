package com.beacon.asm.all;

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
