package navigatorteam.cryptoproxy;

import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpResponse;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created on 2019-07-22.
 */
public class P21Node implements LogProducer {


    public static RawHttp rawHttp = new RawHttp();

    private final ServerSocket serverSocketWithP1;


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
        serverSocketWithP1 = new ServerSocket(port);


        //serverSocketWithP1.setSoTimeout(100000);	//if needed to add timeout
        Logger.getLogger(getLoggerName()).info("Port: " + serverSocketWithP1.getLocalPort());


    }


    private void startListening() throws IOException {
        Logger.getLogger(getLoggerName()).info("Started listening...");
        listen = true;

        while (listen) {
            Logger.getLogger(getLoggerName()).info("Waiting accept...");
            Socket socket = serverSocketWithP1.accept(); //waits for new connection/request

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
            activeThreads.clear();
        }


        serverSocketWithP1.close();


    }


    //multithreaded
//    public void handleRequest(Socket p1Socket, int internalReqID) {
//        try (Socket p22Socket = new Socket(ConstsAndUtils.P22Host, ConstsAndUtils.P22Port)){
//
//            BufferedReader p1In = new BufferedReader(new InputStreamReader(p1Socket.getInputStream()));
//            PrintWriter p22Out = new PrintWriter(p22Socket.getOutputStream());
//
//            Pipe p1ToP22Pipe = new Pipe(activeThreads, p1In, p22Out, s ->{
//                String decS = crypto.decrypt(s);
//                Logger.getLogger(getLoggerName()).info("REQ"+internalReqID+": ---> "+decS);
//                return decS;
//            });
//            activeThreads.add(p1ToP22Pipe);
//
//            BufferedReader p22In = new BufferedReader(new InputStreamReader(p22Socket.getInputStream()));
//            PrintWriter p1Out = new PrintWriter(p1Socket.getOutputStream());
//
//            Pipe p22ToP1Pipe = new Pipe(activeThreads, p22In, p1Out, s -> {
//                Logger.getLogger(getLoggerName()).info("RSP"+internalReqID+": <--- "+s);
//                return crypto.encrypt(s);
//            });
//
//            activeThreads.add(p22ToP1Pipe);
//
//            p1ToP22Pipe.start();
//            p22ToP1Pipe.start();
//
//            p1ToP22Pipe.join();
//            p22ToP1Pipe.join();
//
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                p1Socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            activeThreads.remove(Thread.currentThread());
//        }
//
//    }


    public void handleRequest(Socket p1Socket, int internalReqID) {
        HttpURLConnection proxyToServerCon = null;
        try {
            BufferedReader p21In = new BufferedReader(new InputStreamReader(p1Socket.getInputStream()));
            String firstLine = crypto.decrypt(p21In.readLine());
            System.out.println(firstLine);
            if (firstLine != null) {
                String url = firstLine.substring(firstLine.indexOf(' '));
                url = url.substring(1);
                url = url.substring(0, url.indexOf(' '));

                Logger.getLogger(getLoggerName()).info("REQ" + internalReqID + ": connecting to '" + url + "'");

                // Create the URL
                URL remoteURL = new URL(url);
                // Create a connection to remote server
                proxyToServerCon = (HttpURLConnection) remoteURL.openConnection();
                proxyToServerCon.setDoOutput(true);
                PrintWriter serverOut = new PrintWriter(proxyToServerCon.getOutputStream());

                Pipe p21ToServerPipe = new Pipe(activeThreads, p21In, serverOut, s -> {
                    String decS = crypto.decrypt(s);
                    Logger.getLogger(getLoggerName()).info("REQ" + internalReqID + ": ---> " + decS);
                    return decS;
                });
                activeThreads.add(p21ToServerPipe);

                BufferedReader serverIn = new BufferedReader(new InputStreamReader(proxyToServerCon.getInputStream()));
                PrintWriter p21Out;
                try {
                    p21Out = new PrintWriter(p1Socket.getOutputStream());

                    Pipe serverToP21Pipe = new Pipe(activeThreads, serverIn, p21Out, s -> {
                        Logger.getLogger(getLoggerName()).info("RSP" + internalReqID + ": <--- " + s);
                        return crypto.encrypt(s);
                    });
                    activeThreads.add(serverToP21Pipe);

                    serverOut.println(firstLine);
                    p21ToServerPipe.start();
                    serverToP21Pipe.start();

                    p21ToServerPipe.join();
                    serverToP21Pipe.join();
                } catch (FileNotFoundException fnfe) {
                    RawHttpResponse<Void> voidRawHttpResponse = rawHttp.parseResponse("HTTP/1.0 404 Not Found");
                    voidRawHttpResponse.writeTo(p1Socket.getOutputStream());

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                p1Socket.close();
                if (proxyToServerCon != null) {
                    proxyToServerCon.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            activeThreads.remove(Thread.currentThread());
        }
    }

}
