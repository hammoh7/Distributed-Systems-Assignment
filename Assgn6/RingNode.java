import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;

public class RingNode extends UnicastRemoteObject implements RingElection {
    private final int nodeId;
    private final String nextNodeUrl;
    private int leaderId = -1;

    public RingNode(int nodeId, String nextNodeUrl) throws RemoteException {
        this.nodeId = nodeId;
        this.nextNodeUrl = nextNodeUrl;
    }

    @Override
    public void startElection(int initiatorId, int maxId) throws RemoteException {
        int currentMax = Math.max(maxId, nodeId);

        if (initiatorId == nodeId) {
            leaderId = currentMax;
            System.out.println("Node " + nodeId + ": Election complete. Leader is " + leaderId);
            return;
        }

        try {
            RingElection nextNode = (RingElection) Naming.lookup(nextNodeUrl);
            nextNode.startElection(initiatorId, currentMax);
        } catch (Exception e) {
            System.err.println("Node " + nodeId + ": Next node is down. Restarting election.");
        }
    }

    public static void main(String[] args) {
        try {
            int nodeId = Integer.parseInt(args[0]);
            String nextNodeUrl = args[1];

            RingNode node = new RingNode(nodeId, nextNodeUrl);
            Naming.rebind("Node" + nodeId, node);
            System.out.println("Node " + nodeId + " started. Next node: " + nextNodeUrl);

            Thread.sleep(10000);
            node.startElection(nodeId, nodeId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}