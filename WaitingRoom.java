
public class WaitingRoom {

    private int size;            
    private Patient[] seats;
    private int inptr = 0;
    private int outptr = 0; 

    private Semaphore emptySeats;
    private Semaphore waitingPatients;
    private Semaphore mutex;

    private Logger logger;


    private volatile boolean closed = false;

    public WaitingRoom(int capacity, Logger logger) {
        this.size = capacity;
        this.seats = new Patient[capacity];
        this.logger = logger;
        this.emptySeats = new Semaphore(capacity);
        this.waitingPatients = new Semaphore(0);
        this.mutex = new Semaphore(1);
    }

  
    public boolean tryEnter(Patient p) throws InterruptedException {
        if (emptySeats.tryAcquire(0) == false) {
            return false;        
        }
        insert(p);
        return true;
    }

    public void enter(Patient p) throws InterruptedException {
        emptySeats.acquire();      
        insert(p);
    }


    private void insert(Patient p) throws InterruptedException {
        mutex.acquire();
        seats[inptr] = p;
        inptr = (inptr + 1) % size;
        logger.log(p + " entered the waiting room.");
        mutex.release();

        waitingPatients.release(); 
    }


    public Patient takePatient() throws InterruptedException {
        waitingPatients.acquire();  

        if (closed) {
            return null;         
        }

        mutex.acquire();
        Patient p = seats[outptr];
        outptr = (outptr + 1) % size;
        mutex.release();

        emptySeats.release();      
        return p;
    }


    public void shutdown(int numberOfDoctors) {
        closed = true;
        for (int i = 0; i < numberOfDoctors; i++) {
            waitingPatients.release();
        }
    }
}