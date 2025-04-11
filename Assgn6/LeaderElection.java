import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LeaderElection extends Remote {
    void startElection(int nodeId) throws RemoteException;

    void announceLeader(int leaderId) throws RemoteException;

    boolean ping() throws RemoteException;
}