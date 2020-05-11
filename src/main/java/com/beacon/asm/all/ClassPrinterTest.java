package com.beacon.asm.all;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.IOException;

/**
 * DESCRIPTION:
 * <P>
 * </p>
 *
 * @author WangMin
 * @since 2020/4/29 10:41 上午
 */
public class ClassPrinterTest {

    public static void main(String[] args) throws IOException {
        ClassPrinter cp = new ClassPrinter(Opcodes.ASM8);
        ClassReader cr = new ClassReader("com.beacon.asm.all.Student");

        cr.accept(cp, 0);
    }
}
