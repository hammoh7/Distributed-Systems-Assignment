import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class SlaveNode extends UnicastRemoteObject implements ClockSync {
    private long localTime;
    private String name;

    public SlaveNode(String name, long initialTime) throws RemoteException {
        this.name = name;
        this.localTime = initialTime;
    }

    @Override
    public long getTime() throws RemoteException {
        return localTime;
    }

    @Override
    public void adjustTime(long offset) throws RemoteException {
        localTime += offset;
        System.out.println(name + " adjusted by " + offset + ". New time: " + localTime);
    }

    public static void main(String[] args) {
        try {
            String name = args[0];
            long initialTime = Long.parseLong(args[1]);
            Registry registry = LocateRegistry.getRegistry();
            SlaveNode slave = new SlaveNode(name, initialTime);
            registry.rebind(name, slave);
            System.out.println(name + " is ready with initial time " + initialTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}