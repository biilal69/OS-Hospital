public class Logger {
 
    public Logger() {
    }
 
    public synchronized void log(String message) {
        System.out.println(message);
    }
}
 