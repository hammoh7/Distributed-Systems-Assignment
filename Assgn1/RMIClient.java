import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            RemoteInterface remote = (RemoteInterface) registry.lookup("RemoteService");

            for (int i = 0; i < 5; i++) {
                final int threadId = i;
                new Thread(() -> {
                    try {
                        System.out.println("Thread " + threadId + " sending request...");
                        String response = remote.processRequest("Request-" + threadId);
                        System.out.println("Thread " + threadId + " received: " + response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}