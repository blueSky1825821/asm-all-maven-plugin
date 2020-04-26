package com.beacon.asm.all;

/**
 * DESCRIPTION:
 * <P>
 *     ASM统计耗时java方法,使用该类生成java字节码
 * </p>
 *
 * @author WangMin
 * @since 2020/4/25 10:44 上午
 */
public class CostTimeMethod {


    /**
     * 耗时生成的代码
     */
    public void test() {
        ASMConstants.ASM_COST_TIME_LOGGER.info("========start=========");
        MethodTimeCache.setStartTime("test", System.currentTimeMillis());

        MethodTimeCache.setEndTime("test", System.currentTimeMillis());
        ASMConstants.ASM_COST_TIME_LOGGER.info(MethodTimeCache.getCostTime("test"));
        ASMConstants.ASM_COST_TIME_LOGGER.info("========end=========");
    }
}
