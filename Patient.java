// STUB by Member 1 - Member 5 replaces this file.
public class Patient extends Thread {
    private final int id;
    public Patient(int id, WaitingRoom waitingRoom, Logger logger) { this.id = id; }
    public int getPatientId() { return id; }
    @Override public String toString() { return "P" + id; }
    @Override public void run() { }
    public void treatmentFinished() { }
    public void sentBack() { }
}