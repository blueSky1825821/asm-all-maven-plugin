package com.beacon.asm.all;

import org.junit.Test;

import java.io.IOException;

/**
 * DESCRIPTION:
 * <P>
 * </p>
 *
 * @author WangMin
 * @since 2020/5/9 11:47 上午
 */
public class TimeMojoTest {
    @Test
    public void test() throws IOException {
        TimeMojo timeMojo = new TimeMojo();
        timeMojo.initASM();
    }

    /**
     * 测试生成后的代码是否有效
     */
    @Test
    public void testDemo() {
        Demo demo = new Demo();
        demo.test();
    }

    @Test
    public void testPlus() {
        plusi();
        iPlus();
    }

    @Test
    public void m() {
        CostTimeMethod costTimeMethod = new CostTimeMethod();
        costTimeMethod.test();
    }


    public void plusi() {
        int a = 0;
        int b = 0;
        for (int j = 0; j < 100; j++) {
            System.out.println(j);
        }
    }

    public void iPlus() {
        for (int i = 0; i < 100; ++i) {
            System.out.println(i);
        }
    }

    public static void main(String[] args){
        int i = 0;
        i = ++i;
        System.out.println(i);
    }
}
