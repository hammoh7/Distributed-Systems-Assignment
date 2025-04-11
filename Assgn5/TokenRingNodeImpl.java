import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.util.concurrent.atomic.AtomicBoolean;

public class TokenRingNodeImpl extends UnicastRemoteObject implements TokenRingNode {
    private final String nextNodeName;
    private final AtomicBoolean hasToken = new AtomicBoolean(false);
    private final AtomicBoolean wantsCriticalSection = new AtomicBoolean(false);
    private TokenRingNode cachedNextNode = null;

    public TokenRingNodeImpl(String nextNodeName) throws RemoteException {
        this.nextNodeName = nextNodeName;
    }

    @Override
    public void receiveToken() throws RemoteException {
        new Thread(() -> {
            hasToken.set(true);
            System.out.println(Thread.currentThread().getName() + " received token.");

            if (wantsCriticalSection.get()) {
                enterCriticalSection();
                wantsCriticalSection.set(false);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            passToken();
        }).start();
    }

    private void enterCriticalSection() {
        System.out.println(Thread.currentThread().getName() + " ENTERING CRITICAL SECTION.");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " EXITING CRITICAL SECTION.");
    }

    private void passToken() {
        try {
            if (cachedNextNode == null) {
                cachedNextNode = (TokenRingNode) Naming.lookup(nextNodeName);
            }
            System.out.println(Thread.currentThread().getName() + " passing token to " + nextNodeName);
            hasToken.set(false);
            cachedNextNode.receiveToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestCriticalSection() {
        wantsCriticalSection.set(true);
    }

    private static volatile boolean running = true;

    public static void stop() {
        running = false;
    }

    public static void main(String[] args) {
        try {
            String nodeName = args[0];
            String nextNodeName = args[1];
            TokenRingNodeImpl node = new TokenRingNodeImpl(nextNodeName);

            Naming.rebind(nodeName, node);
            System.out.println(nodeName + " registered. Next node: " + nextNodeName);

            if (args.length > 2 && args[2].equals("init")) {
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        node.receiveToken();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }

            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    node.requestCriticalSection();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            while (running) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
