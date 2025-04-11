import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Worker extends Remote {
    int calculateSum(int[] subArray) throws RemoteException;
}