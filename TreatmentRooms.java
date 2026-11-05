/**
 * The treatment rooms: a thin wrapper around ONE counting semaphore.
 * Independent from the waiting room (no shared lock with it).
 */
public class TreatmentRooms {

    private final Semaphore rooms;

    /** Hospital passes 2. */
    public TreatmentRooms(int numberOfRooms) {
        if (numberOfRooms < 1) {
            throw new IllegalArgumentException("numberOfRooms must be >= 1");
        }
        this.rooms = new Semaphore(numberOfRooms);
    }

    /** true = a room is now ours; false = timed out (no room taken). */
    public boolean tryAcquireRoom(long timeoutMillis) throws InterruptedException {
        return rooms.tryAcquire(timeoutMillis);
    }

    /** Call ONLY after a successful tryAcquireRoom (use try/finally). */
    public void releaseRoom() {
        rooms.release();
    }
}

