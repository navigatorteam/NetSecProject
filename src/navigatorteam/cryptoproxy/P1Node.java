package navigatorteam.cryptoproxy;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class P1Node implements LogProducer {

    //public static RawHttp rawHttp = new RawHttp();

    private final ServerSocket serverSocketWithClient;

    private CryptoServiceProvider crypto = null;


    private boolean listen = false;
    private final Set<Thread> activeThreads = Collections.synchronizedSet(new HashSet<>());

    @Override
    public String getLoggerName() {
        return "P1";
    }

    public static void main(String args[]) {
        try {
            P1Node p1Node = new P1Node(ConstsAndUtils.P1Port);

            p1Node.auth();

            p1Node.initCrypto(/*args*/);

            p1Node.startListening();

        } catch (SocketException se) {
            System.out.println("Socket Exception when connecting to client");
            se.printStackTrace();
        } catch (SocketTimeoutException ste) {
            System.out.println("Timeout occurred while connecting to client");
        } catch (UnknownHostException uhe) {
            System.out.println("Unknown P2 host");
        } catch (IOException io) {
            System.out.println("IO exception when connecting to client");
        }
    }

    private void auth() {
        //TODO implement
    }

    private void initCrypto() {
        //TODO implement with correct crypto
        crypto = new DummyCrypto();
    }


    public P1Node(int port) throws IOException {
        serverSocketWithClient = new ServerSocket(port);


        //serverSocketWithClient.setSoTimeout(100000);	//if needed to add timeout
        Logger.getLogger(getLoggerName()).info("Port: " + serverSocketWithClient.getLocalPort());


    }


    private void startListening() throws IOException {
        Logger.getLogger(getLoggerName()).info("Started listening...");
        listen = true;

        while (listen) {
            Logger.getLogger(getLoggerName()).info("Waiting accept...");
            Socket socket = serverSocketWithClient.accept(); //waits for new connection/request

            Logger.getLogger(getLoggerName()).info("New request from client...");

            Thread thread = new Thread(() -> handleRequest(socket, ConstsAndUtils.nextID()));

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
            activeThreads.clear();
        }


        serverSocketWithClient.close();


    }


    //multithreaded
    public void handleRequest(Socket clientSocket, int internalReqID) {
        try (Socket p21Socket = new Socket(ConstsAndUtils.P21Host, ConstsAndUtils.P21Port)){

            BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter p21out = new PrintWriter(p21Socket.getOutputStream(), true);
            Pipe pipeClientToP21 = new Pipe(activeThreads, clientIn, p21out, s -> {
                Logger.getLogger(getLoggerName()).info("REQ"+internalReqID+": ---> "+ s);
                return crypto.encrypt(s);
            });
            activeThreads.add(pipeClientToP21);

            BufferedReader p21In = new BufferedReader(new InputStreamReader(p21Socket.getInputStream()));
            PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream());
            Pipe pipeP21toClient = new Pipe(activeThreads, p21In, clientOut, s -> {
                String decS = crypto.decrypt(s);
                Logger.getLogger(getLoggerName()).info("RSP"+internalReqID+": <--- "+decS);
                return decS;
            });
            activeThreads.add(pipeP21toClient);

            pipeClientToP21.start();
            pipeP21toClient.start();

            pipeClientToP21.join();
            pipeP21toClient.join();


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            activeThreads.remove(Thread.currentThread());
        }
    }


}