package navigatorteam.cryptoproxy;

import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created on 2019-07-22.
 */
public class P21Node implements LogProducer{



    public static RawHttp rawHttp = new RawHttp();

    private final ServerSocket socketWithP1;
    private final Socket socketWithP22;

    private CryptoServiceProvider crypto = null;

    private boolean listen = false;
    private final Set<Thread> activeThreads = Collections.synchronizedSet(new HashSet<>());

    @Override
    public String getLoggerName() {
        return "P21";
    }

    public static void main(String args[]) {
        try {
            P21Node p21Node = new P21Node(ConstsAndUtils.P21Port);

            p21Node.waitForAuth();

            p21Node.initCrypto();

            p21Node.startListening();
        } catch (SocketException se) {
            System.out.println("Socket Exception when connecting to client");
            se.printStackTrace();
        } catch (SocketTimeoutException ste) {
            System.out.println("Timeout occured while connecting to client");
        } catch (IOException io) {
            System.out.println("IO exception when connecting to client");
        }
    }

    private void initCrypto() {
        // TODO: implement
        crypto = new DummyCrypto();
    }

    private void waitForAuth() {
        // TODO: implement
    }


    public P21Node(int port) throws IOException {
        socketWithP1 = new ServerSocket(port);
        socketWithP22 = new Socket(ConstsAndUtils.P22Host, ConstsAndUtils.P22Port);

        //socketWithP1.setSoTimeout(100000);	//if needed to add timeout
        Logger.getLogger(getLoggerName()).info("Port: " + socketWithP1.getLocalPort());


    }


    private void startListening() throws IOException {
        Logger.getLogger(getLoggerName()).info("Started listening...");
        listen = true;

        while (listen) {
            Logger.getLogger(getLoggerName()).info("Waiting accept...");
            Socket socket = socketWithP1.accept(); //waits for new connection/request

            Logger.getLogger(getLoggerName()).info("new request from P1...");

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


        socketWithP1.close();
        socketWithP22.close();

    }


    //multithreaded
    public void handleRequest(Socket clientSocket, int internalReqID) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String s = bufferedReader.readLine();

            if(s != null) {
                Logger.getLogger(getLoggerName()).info("RQ"+ internalReqID + ": "+ "Decrypting...");
                String c = crypto.decrypt(s);

                Logger.getLogger(getLoggerName()).info("RQ"+ internalReqID + ": "+ c);
                Logger.getLogger(getLoggerName()).info("RQ"+ internalReqID + ": "+ "Sending to P22...");
                PrintWriter out = new PrintWriter(socketWithP22.getOutputStream(), true);
                out.println(c);
                Logger.getLogger(getLoggerName()).info("RQ"+ internalReqID + ": "+ "Sent.");

                Logger.getLogger(getLoggerName()).info("RQ"+ internalReqID + ": "+ "Waiting response from P22...");

                RawHttpResponse<Void> rawHttpResponse = rawHttp.parseResponse(socketWithP22.getInputStream());


                String resp = rawHttpResponse.eagerly().toString();
                if(resp != null) {
                    Logger.getLogger(getLoggerName()).info("RQ"+ internalReqID + ": "+ ("RESPONSE: " + resp));

                    Logger.getLogger(getLoggerName()).info("RQ"+ internalReqID + ": "+ "Encrypting response...");
                    String resp_crypt = crypto.encrypt(resp);
                    Logger.getLogger(getLoggerName()).info("RQ"+ internalReqID + ": "+ "Sending response to P1...");
                    PrintWriter outP1 = new PrintWriter(clientSocket.getOutputStream(), true);
                    outP1.println(resp_crypt);
                    Logger.getLogger(getLoggerName()).info("RQ"+ internalReqID + ": "+ "Response sent.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {

            activeThreads.remove(Thread.currentThread());
        }

    }
}
