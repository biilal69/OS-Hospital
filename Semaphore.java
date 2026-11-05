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

    public synchronized void release() {
        permits++;
        notifyAll();
    }


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

