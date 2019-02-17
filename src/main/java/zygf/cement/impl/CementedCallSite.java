package zygf.cement.impl;

import java.lang.invoke.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Taken from https://github.com/scala/scala/blob/2.13.x/test/files/run/indy-via-macro-with-dynamic-args/Bootstrap.java
// and expanded into a whole CallSite class

public final class CementedCallSite extends ConstantCallSite {
    volatile private Object value;
    volatile private ReentrantLock lock;

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
    
    // TODO: multithreaded tests?
    
    public final class Placeholder {
        @Override
        public String toString() {
            return "CementedCallSite.Placeholder";
        }
        
        /** Acquire update lock and return it, or else return null if the value was already set */
        public Lock startUpdate() {
            ReentrantLock lock = CementedCallSite.this.lock;
            
            if (lock == null) {
                return null;
            }
            
            lock.lock();
            if (lock.getHoldCount() > 1) {
                lock.unlock();
                throw new IllegalStateException("Circular initialization detected");
            }
            
            if (CementedCallSite.this.lock == null) /* recheck holding lock */ {
                lock.unlock();
                return null;
            }
            
            return lock;
        }
        
        /** Release update lock */
        public void finishUpdate(Lock lock) {
            lock.unlock();
        }
        
        /** Set value after acquiring the update lock, then forget the lock */
        public Object setValue(Object newValue) {
            value = newValue;
            // free the lock, it won't be needed anymore
            lock = null;
            return newValue;
        }
        
        /** Get the value that was set while we were trying to acquire the update lock */
        public Object getValue() {
            if (lock != null)
                throw new IllegalStateException();
            return value;
        }
    }

    private CementedCallSite() throws Throwable {
        super(getterType, initializer);
        value = new Placeholder();
        lock = new ReentrantLock(false);
    }

    private static MethodHandle init(CementedCallSite self) {
        return getter.bindTo(self);
    }

    Object get() {
        return value;
    }

    public static CallSite bootstrap(MethodHandles.Lookup lookup,
                                     String invokedName,
                                     MethodType invokedType) throws Throwable {
        return new CementedCallSite();
    }
}
