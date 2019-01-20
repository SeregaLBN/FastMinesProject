package fmg.common.notyfier;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Signal {

    private final CountDownLatch signal = new CountDownLatch(1);

    /** set signal */
    public void set() {
        signal.countDown();
    }

    /** wait for signal */
    public boolean await(long timeoutMs) {
        try {
            return signal.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
