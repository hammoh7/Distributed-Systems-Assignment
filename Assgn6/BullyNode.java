import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BullyNode extends UnicastRemoteObject implements LeaderElection {
    private final int nodeId;
    private final List<String> nodeUrls;
    private volatile int leaderId = -1;

    public BullyNode(int nodeId, List<String> nodeUrls) throws RemoteException {
        this.nodeId = nodeId;
        this.nodeUrls = new CopyOnWriteArrayList<>(nodeUrls);
    }

    @Override
    public synchronized void startElection(int initiatorId) throws RemoteException {
        System.out.println("Node " + nodeId + ": Election started by Node " + initiatorId);

        boolean higherNodeExists = false;

        for (String url : nodeUrls) {
            int targetId = Integer.parseInt(url.split("Node")[1]);
            if (targetId > nodeId) {
                try {
                    LeaderElection node = (LeaderElection) Naming.lookup(url);
                    System.out.println("Node " + nodeId + ": Contacting Node " + targetId);
                    if (node.ping()) {
                        higherNodeExists = true;
                        node.startElection(nodeId);
                    }
                } catch (Exception e) {
                    System.err.println("Node " + nodeId + ": Node " + targetId + " is unreachable.");
                }
            }
        }

        if (!higherNodeExists) {
            leaderId = nodeId;
            System.out.println("Node " + nodeId + ": No higher nodes responded. Declaring myself leader.");
            announceLeaderToAll();
        }
    }

    private void announceLeaderToAll() {
        for (String url : nodeUrls) {
            try {
                LeaderElection node = (LeaderElection) Naming.lookup(url);
                node.announceLeader(leaderId);
                System.out.println("Node " + nodeId + ": Notified " + url + " about leader.");
            } catch (Exception e) {
                System.err.println("Node " + nodeId + ": Failed to notify " + url);
            }
        }
    }

    @Override
    public synchronized void announceLeader(int leaderId) {
        this.leaderId = leaderId;
        System.out.println("Node " + nodeId + ": Updated leader to Node " + leaderId);
    }

    @Override
    public boolean ping() throws RemoteException {
        return true;
    }

    public static void main(String[] args) {
        try {
            int nodeId = Integer.parseInt(args[0]);
            List<String> nodeUrls = List.of(
                    "rmi://localhost/Node1",
                    "rmi://localhost/Node2",
                    "rmi://localhost/Node3");

            BullyNode node = new BullyNode(nodeId, nodeUrls);
            Naming.rebind("Node" + nodeId, node);
            System.out.println("Node " + nodeId + " started.");

            Thread.sleep((4 - nodeId) * 5000);
            
            new Thread(() -> {
                try {
                    node.startElection(nodeId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}