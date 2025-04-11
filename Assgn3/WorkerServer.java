import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class WorkerServer {
    public static void main(String[] args) {
        try {
            String name = args[0]; 
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            WorkerImpl worker = new WorkerImpl();
            registry.rebind(name, worker);
            System.out.println(name + " ready...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}