package org.akaza.openclinica.job;

import org.quartz.simpl.ZeroSizeThreadPool;

/**
 *
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class EmptyThreadPool extends ZeroSizeThreadPool {

    private final Object lock = new Object();

    @Override
    public boolean runInThread(Runnable runnable) {
        sleep();
        return false;
    }

    @Override
    public int blockForAvailableThreads() {
        sleep();
        return 0;
    }

    private void sleep() {
        synchronized(lock) {
            try {
                lock.wait(10000);
            } catch (InterruptedException e) {
                // Do nothing
            }
        }
    }

}
