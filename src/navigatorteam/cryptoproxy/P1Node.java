package navigatorteam.cryptoproxy;


import rawhttp.core.RawHttp;

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

    public static RawHttp rawHttp = new RawHttp();

    private final ServerSocket socketWithClient;
    private final Socket socketWithP21;
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
        socketWithClient = new ServerSocket(port);
        socketWithP21 = new Socket(ConstsAndUtils.P21Host, ConstsAndUtils.P21Port);

        //socketWithClient.setSoTimeout(100000);	//if needed to add timeout
        Logger.getLogger(getLoggerName()).info("Port: " + socketWithClient.getLocalPort());


    }


    private void startListening() throws IOException {
        Logger.getLogger(getLoggerName()).info("Started listening...");
        listen = true;

        while (listen) {
            Socket socket = socketWithClient.accept(); //waits for new connection/request

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
        }


        socketWithClient.close();
        socketWithP21.close();

    }


    //multithreaded
    public void handleRequest(Socket clientSocket, int internalReqID) {
        try {
            String clientReq = rawHttp.parseRequest(clientSocket.getInputStream()).eagerly().toString();
            PrintWriter client_out = new PrintWriter(clientSocket.getOutputStream(), true);
            Logger.getLogger(getLoggerName()).info(clientReq);
            if (clientReq != null) {
                Logger.getLogger(getLoggerName()).info("RQ"+ internalReqID + ": "+ "encrypting...");
                String c = crypto.encrypt(clientReq);

                Logger.getLogger(getLoggerName()).info("RQ"+ internalReqID + ": "+ "sending to P21...");
                PrintWriter out = new PrintWriter(socketWithP21.getOutputStream(), true);
                out.println(c);

                Logger.getLogger(getLoggerName()).info("RQ"+ internalReqID + ": "+ "Waiting for response...");
                BufferedReader p21In = new BufferedReader(new InputStreamReader(socketWithP21.getInputStream()));
                String resp_crypt = p21In.readLine();
                if(resp_crypt != null) {

                    Logger.getLogger(getLoggerName()).info("RQ"+ internalReqID + ": "+ "decrypting response...");
                    String resp = crypto.decrypt(resp_crypt);

                    Logger.getLogger(getLoggerName()).info("RQ"+ internalReqID + ": "+ ("RESPONSE: " + resp));

                    Logger.getLogger(getLoggerName()).info("RQ"+ internalReqID + ": "+ "Sending response to client...");


                    client_out.println(resp);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            activeThreads.remove(Thread.currentThread());
        }
    }


}