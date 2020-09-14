package com.beacon.asm.all;

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    private static final String[] INCLUDES_DEFAULT = {};
    private static final String DOT = ".";
    private static final String BLANK = "";
    private static final String JAVA_SUFFIX = ".java";
    private static final String CLASS_SUFFIX = ".class";
    private static final String DIAGONAL = "/";

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Maven内部提供的参数,通过${}来进行获取
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @Parameter(property = "basedir", defaultValue = "${project.build.directory}")
    private File outputDirectory;

    /**
     * 也可以获取自定义的属性值，在插件使用者的配置文件中进行传入
     * 我们这里定义的这个属性值的含义是包含的文件后缀
     *
     * @parameter
     */
    @Parameter(property = "asmClasses")
    private String[] asmClasses;

    @Parameter(property = "asmPackages")
    private String[] asmPackages;

    public String[] getAsmClasses() {
        return asmClasses;
    }

    public void setAsmClasses(String[] asmClasses) {
        this.asmClasses = asmClasses;
    }

    public String[] getAsmPackages() {
        return asmPackages;
    }

    public void setAsmPackages(String[] asmPackages) {
        this.asmPackages = asmPackages;
    }

    @Override
    public void execute() {
        getLog().info("#######out" + outputDirectory.getPath());
        getLog().info("#######class includes:" + Arrays.toString(getAsmClasses()));
        getLog().info("#######package includes:" + Arrays.toString(getAsmPackages()));
        try {
            initASM();
        } catch (Exception e) {
            getLog().error(e);
        }
    }

    protected void initASM() throws IOException {
        Set<String> asmClasses = new HashSet<>();
        if (asmPackages != null && asmPackages.length > 0) {
            for (String asmPackage : asmPackages) {
                String asmPackagePath = outputDirectory.getPath() + DIAGONAL + asmPackage.replace(
                        DOT, DIAGONAL);
                File file = new File(asmPackagePath);
                getFilesPath(file, asmClasses);
            }
        }
        getLog().debug("####asmClasses:" + asmClasses);
        if (this.asmClasses != null && this.asmClasses.length > 0) {
            Set<String> replaces = asmClasses.stream()
                                             .filter(Objects::nonNull)
                                             .map(asmClass -> {
                                                 if (asmClass.endsWith(CLASS_SUFFIX)) {
                                                     return asmClass.replace(CLASS_SUFFIX, BLANK);
                                                 } else if (asmClass.endsWith(JAVA_SUFFIX)) {
                                                     return asmClass.replace(JAVA_SUFFIX, BLANK);
                                                 } else {
                                                     return asmClass;
                                                 }
                                             })
                                             .map(asmClass -> asmClass.replace(DOT, DIAGONAL) +
                                                              CLASS_SUFFIX)
                                             .collect(Collectors.toSet());
            asmClasses.addAll(replaces);
        }
        if (asmClasses.size() == 0) {
            getLog().info("have not any class to asm");
            return;
        }
        for (String asmClass : asmClasses) {
            getLog().debug("#######asmClass:" + asmClass);
            try (InputStream inputStream = new FileInputStream(new File(asmClass))) {
                ClassReader cr = new ClassReader(inputStream);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
                ClassVisitor cv = new CostTimeClassVisitor(Opcodes.ASM8, cw);
                cr.accept(cv, EXPAND_FRAMES);

                // 获取生成的class文件对应的二进制流
                byte[] code = cw.toByteArray();

                //将二进制流写到out/下
                //注意一定要是out
                getLog().debug("#######outPath: " + asmClass);
                try (FileOutputStream fos = new FileOutputStream(asmClass)) {
                    fos.write(code);
                }
            }
        }
    }

    private void getFilesPath(File file, Set<String> asmClasses) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File innerFile : files) {
                    getFilesPath(innerFile, asmClasses);
                }
            }
        } else {
            if (file.getName()
                    .endsWith(CLASS_SUFFIX)) {
                asmClasses.add(file.getAbsolutePath());
            }
        }
    }
}
