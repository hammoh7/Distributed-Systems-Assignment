import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteImpl extends UnicastRemoteObject implements RemoteInterface {

    public RemoteImpl() throws RemoteException {
        super();
    }

    @Override
    public String processRequest(String input) throws RemoteException {
        System.out.println("Server processing: " + input + " | Thread: " + Thread.currentThread().threadId());
        try {
            Thread.sleep(2000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Processed: " + input;
    }
}