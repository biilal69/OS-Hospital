/**
 * Our own counting semaphore (built with synchronized / wait / notifyAll).
 * Starting value 1 = mutex. Starting value N = N identical resources.
 * Does NOT use java.util.concurrent.Semaphore.
 */
public class Semaphore {

    private int permits; // always accessed inside synchronized methods

    public Semaphore(int initialPermits) {
        if (initialPermits < 0) {
            throw new IllegalArgumentException("initialPermits must be >= 0");
        }
        this.permits = initialPermits;
    }

    /** Blocks until a permit exists, then takes it. */
    public synchronized void acquire() throws InterruptedException {
        // while (not if): a woken thread must re-check the condition
        while (permits == 0) {
            wait();
        }
        permits--;
    }

    /** Adds a permit and wakes all waiters (notifyAll avoids lost wake-ups). */
    public synchronized void release() {
        permits++;
        notifyAll();
    }

    /**
     * Tries to take a permit within timeoutMillis.
     * true  = got a permit.
     * false = time ran out. timeoutMillis <= 0 means "do not wait at all".
     * The deadline is computed ONCE; remaining time is recomputed after
     * every wake-up, so the total wait never exceeds the timeout.
     */
    public synchronized boolean tryAcquire(long timeoutMillis) throws InterruptedException {
        if (permits > 0) {
            permits--;
            return true;
        }
        if (timeoutMillis <= 0) {
            return false;
        }
        long deadline = System.nanoTime() + timeoutMillis * 1_000_000L;
        while (permits == 0) {
            long remainingNanos = deadline - System.nanoTime();
            if (remainingNanos <= 0) {
                return false; // timeout
            }
            // remainingNanos > 0, so this is never wait(0, 0) (which would mean forever)
            wait(remainingNanos / 1_000_000L, (int) (remainingNanos % 1_000_000L));
        }
        permits--;
        return true;
    }
}

