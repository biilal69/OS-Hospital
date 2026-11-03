import java.util.Random;
import java.util.Scanner;
 
/**
 * Hospital - Member 1.
 * Reads and validates input, creates the shared objects, starts all threads,
 * waits for the patients, shuts the doctors down and prints the final lines.
 */
public class Hospital {
 
    public static final int NUM_TREATMENT_ROOMS = 2;   // PDF: only two rooms
    private static final int MAX_ARRIVAL_GAP_MS = 500; // our choice: pause between arrivals
 
    private final int waitingRoomCapacity;
    private final int numDoctors;
    private final int numPatients;
    private final Logger logger = new Logger();
    private final Random random = new Random();
 
    public Hospital(int waitingRoomCapacity, int numDoctors, int numPatients) {
        if (waitingRoomCapacity < 1) {
            throw new IllegalArgumentException("Waiting room capacity must be at least 1.");
        }
        if (numDoctors < 1) {
            throw new IllegalArgumentException("Number of doctors must be at least 1.");
        }
        if (numPatients < 0) {
            throw new IllegalArgumentException("Number of patients cannot be negative.");
        }
        this.waitingRoomCapacity = waitingRoomCapacity;
        this.numDoctors = numDoctors;
        this.numPatients = numPatients;
    }
 
    public void runSimulation() throws InterruptedException {
        // 1. Shared resources: created once, the same instances go to every thread.
        WaitingRoom waitingRoom = new WaitingRoom(waitingRoomCapacity, logger);
        TreatmentRooms treatmentRooms = new TreatmentRooms(NUM_TREATMENT_ROOMS);
 
        // 2. Create doctors and patients (ids start at 1).
        Doctor[] doctors = new Doctor[numDoctors];
        for (int i = 0; i < numDoctors; i++) {
            doctors[i] = new Doctor(i + 1, waitingRoom, treatmentRooms, logger);
        }
        Patient[] patients = new Patient[numPatients];
        for (int i = 0; i < numPatients; i++) {
            patients[i] = new Patient(i + 1, waitingRoom, logger);
        }
 
        // 3. Header line from the PDF sample.
        logger.log("Patients arriving:");
        if (numPatients > 0) {
            StringBuilder names = new StringBuilder();
            for (int i = 0; i < numPatients; i++) {
                if (i > 0) {
                    names.append(' ');
                }
                names.append(patients[i]);   // Patient.toString() gives P1, P2, ...
            }
            logger.log(names.toString());
        }
 
        // 4. Doctors first (they go idle), then patients with a short random gap.
        for (Doctor doctor : doctors) {
            doctor.start();
        }
        for (int i = 0; i < numPatients; i++) {
            patients[i].start();
            if (i < numPatients - 1) {
                Thread.sleep(random.nextInt(MAX_ARRIVAL_GAP_MS + 1));
            }
        }
 
        // 5. A patient thread only ends after being treated and leaving,
        //    so when every join returns, everyone has been treated.
        for (Patient patient : patients) {
            patient.join();
        }
        logger.log("All patients have been treated.");
 
        // 6. Wake the idle doctors so they can stop, then wait for them.
        waitingRoom.shutdown(numDoctors);
        for (Doctor doctor : doctors) {
            doctor.join();
        }
        logger.log("Simulation completed.");
    }
 
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            int capacity = readInt(scanner, "Waiting room capacity: ", 1);
            int doctors = readInt(scanner, "Number of doctors: ", 1);
            int patients = readInt(scanner, "Number of patients: ", 0);
            new Hospital(capacity, doctors, patients).runSimulation();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Simulation interrupted.");
        } catch (IllegalStateException e) {
            System.out.println("Input ended before all values were given.");
        }
    }
 
    /** Keeps asking until the user types an integer >= min. */
    private static int readInt(Scanner scanner, String prompt, int min) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNext()) {
                throw new IllegalStateException("no more input");
            }
            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                if (value >= min) {
                    return value;
                }
                System.out.println("Please enter a number that is at least " + min + ".");
            } else {
                scanner.next();   // discard the bad token
                System.out.println("Please enter a whole number.");
            }
        }
    }
}
 