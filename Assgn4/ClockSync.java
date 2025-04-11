import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClockSync extends Remote {
    long getTime() throws RemoteException;

    void adjustTime(long offset) throws RemoteException;
}