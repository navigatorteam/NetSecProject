package navigatorteam.cryptoproxy;

import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created on 2019-07-22.
 */
public class P22Node implements LogProducer {

    public static RawHttp rawHttp = new RawHttp();

    private final ServerSocket serverSocketWithP21;

    private boolean listen = false;
    private final Set<Thread> activeThreads = Collections.synchronizedSet(new HashSet<>());

    @Override
    public String getLoggerName() {
        return "P22";
    }

    public static void main(String args[]) {
        try {
            P22Node p22Node = new P22Node(ConstsAndUtils.P22Port);

            p22Node.startListening();
        } catch (SocketException se) {
            System.out.println("Socket Exception when connecting to client");
            se.printStackTrace();
        } catch (SocketTimeoutException ste) {
            System.out.println("Timeout occured while connecting to client");
        } catch (IOException io) {
            System.out.println("IO exception when connecting to client");
        }
    }


    public P22Node(int port) throws IOException {
        serverSocketWithP21 = new ServerSocket(port);


        //socketWithP1.setSoTimeout(100000);	//if needed to add timeout
        Logger.getLogger(getLoggerName()).info("Port: " + serverSocketWithP21.getLocalPort());


    }


    private void startListening() throws IOException {
        Logger.getLogger(getLoggerName()).info("Started listening...");
        listen = true;

        while (listen) {
            Logger.getLogger(getLoggerName()).info("Waiting accept...");
            Socket socket = serverSocketWithP21.accept(); //waits for new connection/request

            Logger.getLogger(getLoggerName()).info("new request from P21...");

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


        serverSocketWithP21.close();

    }


    public void handleRequest(Socket p21Socket, int internalReqID) {
        HttpURLConnection proxyToServerCon = null;
        try {
            BufferedReader p21In = new BufferedReader(new InputStreamReader(p21Socket.getInputStream()));
            Pipe p = new Pipe(activeThreads, p21In, new PrintWriter(System.out), s -> s);
            activeThreads.add(p);
            p.start();
            p.join();
//            String firstLine = p21In.readLine();
//
//            if(firstLine != null) {
//                String url = firstLine.substring(firstLine.indexOf(' '));
//                url = url.substring(1, url.indexOf(' '));
//
//                Logger.getLogger(getLoggerName()).info("REQ"+internalReqID+": connecting to '"+url+"'");
//
//                // Create the URL
//                URL remoteURL = new URL(url);
//                // Create a connection to remote server
//                proxyToServerCon = (HttpURLConnection) remoteURL.openConnection();
//                PrintWriter serverOut = new PrintWriter(proxyToServerCon.getOutputStream());
//
//                Pipe p21ToServerPipe = new Pipe(activeThreads, p21In, serverOut, s -> {
//                    Logger.getLogger(getLoggerName()).info("REQ" + internalReqID + ": ---> " + s);
//                    return s;
//                });
//                activeThreads.add(p21ToServerPipe);
//
//                BufferedReader serverIn = new BufferedReader(new InputStreamReader(proxyToServerCon.getInputStream()));
//                PrintWriter p21Out = new PrintWriter(p21Socket.getOutputStream());
//                Pipe serverToP21Pipe = new Pipe(activeThreads, serverIn, p21Out, s -> {
//                    Logger.getLogger(getLoggerName()).info("RSP" + internalReqID + ": <--- " + s);
//                    return s;
//                });
//                activeThreads.add(serverToP21Pipe);
//
//                serverOut.println(firstLine);
//                p21ToServerPipe.start();
//                serverToP21Pipe.start();
//
//                p21ToServerPipe.join();
//                serverToP21Pipe.join();
//            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                p21Socket.close();
                if(proxyToServerCon != null){
                    proxyToServerCon.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            activeThreads.remove(Thread.currentThread());
        }
    }


}
