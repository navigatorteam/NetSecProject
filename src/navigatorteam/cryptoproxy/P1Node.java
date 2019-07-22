package navigatorteam.cryptoproxy;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class P1Node implements LogProducer {
    static final int port = 6789;
    private final ServerSocket serverSocket;
    private boolean listen = false;
    private final Set<Thread> activeThreads = Collections.synchronizedSet(new HashSet<>());

    @Override
    public String getLoggerName() {
        return this.getClass().getName();
    }

    public static void main(String args[]) {
        try {
            P1Node p1Node = new P1Node(port);

            p1Node.startListening();
        } catch (SocketException se) {
            System.out.println("Socket Exception when connecting to client");
            se.printStackTrace();
        } catch (SocketTimeoutException ste) {
            System.out.println("Timeout occured while connecting to client");
        } catch (IOException io) {
            System.out.println("IO exception when connecting to client");
        }
    }


    public P1Node(int port) throws IOException {
        serverSocket = new ServerSocket(port);


        //serverSocket.setSoTimeout(100000);	//if needed to add timeout
        print("Port: " + serverSocket.getLocalPort());


    }


    private void startListening() throws IOException {
        print("Started listening...");
        listen = true;

        while (listen) {
            Socket socket = serverSocket.accept(); //waits for new connection/request

            // Create new Thread and pass it Runnable RequestHandler
            Thread thread = new Thread(() -> handleRequest(socket));

            // Key a reference to each thread so they can be joined later if necessary
            activeThreads.add(thread);

            thread.start();
        }
    }


    private void stopListeningAndClose() throws IOException {

        listen = false;
        synchronized (activeThreads) {
            for (Thread t : activeThreads) {
                if (t.isAlive()) {
                    t.interrupt();
                }
            }
        }


        serverSocket.close();

    }


    //multithreaded
    public void handleRequest(Socket clientSocket) {


        activeThreads.remove(Thread.currentThread());
    }


}