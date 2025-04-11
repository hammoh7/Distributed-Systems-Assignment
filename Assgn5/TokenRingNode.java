import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TokenRingNode extends Remote {
    void receiveToken() throws RemoteException;
}