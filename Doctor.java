import java.util.concurrent.ThreadLocalRandom;


public class Doctor extends Thread {

    public static final long ROOM_WAIT_MS = 3000;      // PDF: three seconds
    public static final int MIN_TREATMENT_MS = 2000;   // our choice (ambiguity A4)
    public static final int MAX_TREATMENT_MS = 6000;   // our choice

    private final int id;
    private final WaitingRoom waitingRoom;
    private final TreatmentRooms rooms;
    private final Logger logger;

    public Doctor(int id, WaitingRoom waitingRoom, TreatmentRooms rooms, Logger logger) {
        super("Doctor-" + id);
        this.id = id;
        this.waitingRoom = waitingRoom;
        this.rooms = rooms;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Idle: blocks (no busy waiting) until a patient is available.
                Patient patient = waitingRoom.takePatient();
                if (patient == null) {
                    return; // shutdown signal
                }

                logger.log("Doctor " + id + " is assigned to " + patient + ".");
                logger.log("Doctor " + id + " is trying to acquire a treatment room.");

                if (rooms.tryAcquireRoom(ROOM_WAIT_MS)) {
                    treat(patient);
                } else {
                    logger.log("Doctor " + id + " could not acquire a treatment room.");
                    logger.log("Doctor " + id + " is sending " + patient + " back to the waiting room.");
                    logger.log("Doctor " + id + " is now available.");
                    patient.sentBack(); // after the doctor's own last log line
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore the flag and leave the loop
        }
    }

    /** Called only after tryAcquireRoom returned true, so the room is ours. */
    private void treat(Patient patient) throws InterruptedException {
        try {
            logger.log("Doctor " + id + " acquired a treatment room.");
            logger.log("Doctor " + id + " begins treating " + patient + ".");
            Thread.sleep(ThreadLocalRandom.current()
                    .nextInt(MIN_TREATMENT_MS, MAX_TREATMENT_MS + 1));
            logger.log("Doctor " + id + " finished treating " + patient + ".");
        } finally {
            rooms.releaseRoom();
            logger.log("Doctor " + id + " released the treatment room.");
        }
        logger.log("Doctor " + id + " is now available.");
        patient.treatmentFinished(); // after the doctor's own last log line
    }
}
