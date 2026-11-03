/**
 * Logger - Member 1.
 * The ONLY place in the project that prints simulation events.
 * log() is synchronized, so two threads can never mix their text on one line.
 */
public class Logger {
 
    public Logger() {
    }
 
    public synchronized void log(String message) {
        System.out.println(message);
    }
}
 