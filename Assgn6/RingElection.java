import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RingElection extends Remote {
    void startElection(int initiatorId, int maxId) throws RemoteException;
}