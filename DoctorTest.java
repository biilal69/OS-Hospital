
public class DoctorTest {

    static class TestPatient extends Patient {
        volatile String result = null;
        volatile long resultAt = 0;
        TestPatient(int id, WaitingRoom w, Logger l) { super(id, w, l); }
        @Override public void treatmentFinished() { result = "TREATED"; resultAt = System.currentTimeMillis(); }
        @Override public void sentBack() { result = "SENT_BACK"; resultAt = System.currentTimeMillis(); }
    }

    static int failures = 0;
    static void check(String name, boolean ok) {
        System.out.println((ok ? "  [PASS] " : "  [FAIL] ") + name);
        if (!ok) failures++;
    }

    static void waitFor(TestPatient p, long maxMs) throws InterruptedException {
        long end = System.currentTimeMillis() + maxMs;
        while (p.result == null && System.currentTimeMillis() < end) Thread.sleep(20);
    }

    public static void main(String[] args) throws Exception {
        Logger logger = new Logger();

        System.out.println("Test 1: 1 doctor, 1 patient, free room -> full message sequence");
        {
            WaitingRoom wr = new WaitingRoom(2, logger);
            TreatmentRooms rooms = new TreatmentRooms(2);
            TestPatient p = new TestPatient(1, wr, logger);
            wr.enter(p);
            Doctor d = new Doctor(1, wr, rooms, logger);
            d.start();
            waitFor(p, 10000);
            check("patient got TREATED", "TREATED".equals(p.result));
            wr.shutdown(1);
            d.join(2000);
            check("doctor thread ended after shutdown", !d.isAlive());
        }

        System.out.println("Test 2: both rooms held 4 s -> doctor sends patient back after ~3 s");
        {
            WaitingRoom wr = new WaitingRoom(2, logger);
            TreatmentRooms rooms = new TreatmentRooms(2);
            rooms.tryAcquireRoom(0); rooms.tryAcquireRoom(0); // dummy holder takes both
            TestPatient p = new TestPatient(2, wr, logger);
            wr.enter(p);
            long t0 = System.currentTimeMillis();
            Doctor d = new Doctor(2, wr, rooms, logger);
            d.start();
            waitFor(p, 6000);
            long took = p.resultAt - t0;
            check("patient got SENT_BACK", "SENT_BACK".equals(p.result));
            check("send-back after ~3000 ms (was " + took + ")", took >= 2900 && took <= 3500);
            rooms.releaseRoom(); rooms.releaseRoom();
            wr.shutdown(1);
            d.join(2000);
            check("doctor thread ended after shutdown", !d.isAlive());
        }

        System.out.println("Test 3: one room freed at 1 s -> doctor treats normally");
        {
            WaitingRoom wr = new WaitingRoom(2, logger);
            TreatmentRooms rooms = new TreatmentRooms(2);
            rooms.tryAcquireRoom(0); rooms.tryAcquireRoom(0);
            TestPatient p = new TestPatient(3, wr, logger);
            wr.enter(p);
            Doctor d = new Doctor(3, wr, rooms, logger);
            d.start();
            Thread.sleep(1000);
            rooms.releaseRoom(); // free one room
            waitFor(p, 10000);
            check("patient got TREATED", "TREATED".equals(p.result));
            rooms.releaseRoom();
            wr.shutdown(1);
            d.join(2000);
            check("doctor thread ended after shutdown", !d.isAlive());
        }

        System.out.println(failures == 0 ? "ALL DOCTOR TESTS PASSED" : failures + " CHECK(S) FAILED");
    }
}
