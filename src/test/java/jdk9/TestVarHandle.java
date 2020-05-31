package jdk9;

import org.junit.Assert;
import org.junit.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.locks.LockSupport;

/**
 * 参考jdk类库SubmissionPublisher里面的源码
 */
public class TestVarHandle {
    private static final VarHandle DEMAND;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            DEMAND = lookup.findVarHandle(ValueHolder.class, "demand", int.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }

        // Reduce the risk of rare disastrous classloading in first call to
        // LockSupport.park: https://bugs.openjdk.java.net/browse/JDK-8074773
        Class<?> ensureLoaded = LockSupport.class;
    }

    @Test
    public void t1() {
        ValueHolder valueHolder = new ValueHolder();
        DEMAND.setVolatile(valueHolder, 10);
        Assert.assertEquals(10, valueHolder.getValue());
    }

    private static class ValueHolder {
        /**
         * 默认情况下编译会报错:
         * Package 'jdk.internal.vm.annotation' is declared in module 'java.base', which does not export it to the unnamed module
         * <p>
         * 这个问题需要用Java9的javac编译指令加上选项--add-modules手动添加;在Maven里, 可以用maven-compiler-plugin设置compilerArgs
         * <p>
         * 使用IDEA的Alt+Enter即可修复此问题
         * 相当于在Setting - Build,Execution,Deployment - Compiler - Java Compiler - Override compiler parameters per-module中增加了
         */
        // JEP142: Reduce Cache Contention on Specified Fields
        // @jdk.internal.vm.annotation.Contended("c") // segregate避免伪共享
        private volatile int demand;

        public int getValue() {
            return demand;
        }
    }

}
