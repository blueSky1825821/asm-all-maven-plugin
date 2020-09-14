package com.beacon.asm.all;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * DESCRIPTION:
 * <P>
 * </p>
 *
 * @author WangMin
 * @since 2020/4/24 1:02 下午
 */
public class STest {

//    public static void main(String[] args) throws ClassNotFoundException {
//        String name = STest.class.getName();
//        Class.forName(name);
//        System.out.println();
//    }

    @Test
    public void testInitAsm() throws IOException {
        TimeMojo timeMojo = new TimeMojo();
        timeMojo.setAsmPackages(new String[]{"com.souche.danube.beacon"});
        timeMojo.setAsmClasses(new String[]{"com.souche.danube.beacon.asm.Demo", "com.souche.danube.beacon.asm.Demo.java"});
        timeMojo.setOutputDirectory(new File("/Users/wangmin/Work/code/beacon-asm-maven-plugin/target/classes"));
        timeMojo.initASM();
    }
}
