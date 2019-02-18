package zygf.cement.impl;

import java.lang.invoke.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Taken from https://github.com/scala/scala/blob/2.13.x/test/files/run/indy-via-macro-with-dynamic-args/Bootstrap.java
// and expanded into a whole CallSite class

public final class CementedCallSite extends ConstantCallSite {
    volatile private Object value;

    private static final MethodType getterType = MethodType.methodType(Object.class);

    private static final MethodHandle getter;
    private static final MethodHandle initializer;

    static {
        try {
            getter = MethodHandles.lookup().findVirtual(CementedCallSite.class, "get", getterType);
            initializer = MethodHandles.lookup().findStatic(CementedCallSite.class, "init", MethodType.methodType(MethodHandle.class, CementedCallSite.class));
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    public final class Placeholder extends zygf.cement.impl.Placeholder {
        /** Set value after acquiring the update lock, then forget the lock */
        public Object setValue(Object newValue) {
            value = newValue;
            return newValue;
        }
        
        /** Get the value that was set while we were trying to acquire the update lock */
        public Object getValue() {
            return value;
        }
    }

    public CementedCallSite() throws Throwable {
        super(getterType, initializer);
        value = new Placeholder();
    }

    private static MethodHandle init(CementedCallSite self) {
        return getter.bindTo(self);
    }

    public Object get() {
        return value;
    }

    public static CallSite bootstrap(MethodHandles.Lookup lookup,
                                     String invokedName,
                                     MethodType invokedType) throws Throwable {
        return new CementedCallSite();
    }
}
