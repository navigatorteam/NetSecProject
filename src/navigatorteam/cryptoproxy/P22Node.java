package navigatorteam.cryptoproxy;

import rawhttp.core.EagerHttpResponse;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;

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

    private final ServerSocket socketWithP21;

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
        socketWithP21 = new ServerSocket(port);


        //socketWithP1.setSoTimeout(100000);	//if needed to add timeout
        Logger.getLogger(getLoggerName()).info("Port: " + socketWithP21.getLocalPort());


    }


    private void startListening() throws IOException {
        Logger.getLogger(getLoggerName()).info("Started listening...");
        listen = true;

        while (listen) {
            Socket socket = socketWithP21.accept(); //waits for new connection/request

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
        }


        socketWithP21.close();

    }


    public void handleRequest(Socket p21Socket, int internalReqID) {
        try {
            RawHttpRequest rawHttpRequest = rawHttp.parseRequest(p21Socket.getInputStream()).eagerly();

            String request = rawHttpRequest.toString();

            Logger.getLogger(getLoggerName()).info("RQ" + internalReqID + ": " + request);

            URI uri = rawHttpRequest.getUri();



            Logger.getLogger(getLoggerName()).info("RQ" + internalReqID + ": " + "Request to external server...");
            Socket outSocket = new Socket(uri.getHost(), uri.getPort() > 0 ? uri.getPort() : 80);


//            PrintWriter out = new PrintWriter(outSocket.getOutputStream(), true);
//            out.println(request);
            rawHttpRequest.writeTo(outSocket.getOutputStream());

            Logger.getLogger(getLoggerName()).info("RQ" + internalReqID + ": " + "Waiting response...");
            RawHttpResponse<?> response = rawHttp.parseResponse(outSocket.getInputStream());

            // call "eagerly()" in order to download the body
            EagerHttpResponse<?> eagerResponse = response.eagerly();
            if (eagerResponse != null) {
                String resp = eagerResponse.toString();
                Logger.getLogger(getLoggerName()).info("RQ" + internalReqID + ": " + ("RESPONSE: " + resp));

                Logger.getLogger(getLoggerName()).info("RQ" + internalReqID + ": " + "Sending response to P21...");
                PrintWriter p21Out = new PrintWriter(p21Socket.getOutputStream(), true);
                p21Out.println(resp);

                Logger.getLogger(getLoggerName()).info("RQ" + internalReqID + ": " + "Response sent.");
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            activeThreads.remove(Thread.currentThread());
        }
    }


    public String removeAcceptEncoding(String request) {

        BufferedReader reader = new BufferedReader(new StringReader(request));
        return reader.lines()/*.map(s -> {
            if (s.toLowerCase().startsWith("accept-encoding")) {
                return "Accept-Encoding: identity";
            } else {
                return s;
            }
        })*/.collect(Collectors.joining("\n"))+"\n";


    }

}
