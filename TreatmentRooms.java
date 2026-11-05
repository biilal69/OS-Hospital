public class TreatmentRooms {

    private final Semaphore rooms;


    public TreatmentRooms(int numberOfRooms) {
        if (numberOfRooms < 1) {
            throw new IllegalArgumentException("numberOfRooms must be >= 1");
        }
        this.rooms = new Semaphore(numberOfRooms);
    }

    public boolean tryAcquireRoom(long timeoutMillis) throws InterruptedException {
        return rooms.tryAcquire(timeoutMillis);
    }

    public void releaseRoom() {
        rooms.release();
    }
}

