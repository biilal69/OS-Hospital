public class Patient extends Thread {

    private enum Result { NONE, TREATED, SENT_BACK }

    private int id;
    private WaitingRoom waitingRoom;
    private Logger logger;
    private Semaphore resultSignal = new Semaphore(0);
    private volatile Result result = Result.NONE;

    public Patient(int id, WaitingRoom waitingRoom, Logger logger) {
        super("Patient-" + id);
        this.id = id;
        this.waitingRoom = waitingRoom;
        this.logger = logger;
    }

    public int getPatientId() {
        return id;
    }
    @Override
    public String toString() {
        return "P" + id;
    }
    @Override
    public void run() {
    
        logger.log(this + " arrived.");

        try {
          
            while (true) {
                enterWaitingRoom();
                resultSignal.acquire();

                if (result == Result.TREATED) {
                   
                    logger.log(this + " left.");
                    return;
                }
                
            }
        } catch (InterruptedException e) {.
            Thread.currentThread().interrupt();
        }
    }
    private void enterWaitingRoom() throws InterruptedException {
        if (!waitingRoom.tryEnter(this)) {
            logger.log("The waiting room is full.");
            logger.log(this + " is waiting outside for an available seat.");
            waitingRoom.enter(this); // blocks until a seat is available
        }
    }

    
    public void treatmentFinished() {
        result = Result.TREATED;
        resultSignal.release();
    }

    
    public void sentBack() {
        result = Result.SENT_BACK;
        resultSignal.release();
    }
}
