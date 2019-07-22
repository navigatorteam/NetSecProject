package navigatorteam.cryptoproxy;

import rawhttp.core.EagerHttpResponse;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

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
            P22Node p22Node = new P22Node(Consts.P22Port);

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
        print("Port: " + socketWithP21.getLocalPort());


    }


    private void startListening() throws IOException {
        print("Started listening...");
        listen = true;

        while (listen) {
            Socket socket = socketWithP21.accept(); //waits for new connection/request

            print("new request from P21...");
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


        socketWithP21.close();

    }


    public void handleRequest(Socket p21Socket) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p21Socket.getInputStream()));
            String request = bufferedReader.readLine();

            print(request);

            RawHttpRequest rawHttpRequest = rawHttp.parseRequest(request);
            URI uri = rawHttpRequest.getUri();


            print("Request to external server...");
            Socket outSocket = new Socket(uri.getHost(), 80);

            rawHttpRequest.writeTo(outSocket.getOutputStream());

            print("Waiting response...");
            RawHttpResponse<?> response = rawHttp.parseResponse(outSocket.getInputStream());

            // call "eagerly()" in order to download the body
            EagerHttpResponse<?> eagerResponse = response.eagerly();
            String resp = eagerResponse.toString();
            print("RESPONSE: " + resp);


            print("Sending response to P21...");
            PrintWriter p21Out = new PrintWriter(p21Socket.getOutputStream(), true);
            p21Out.println(resp);
            p21Out.println("cicciobaubaubenzina");
            print("Response sent.");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            activeThreads.remove(Thread.currentThread());
        }
    }


}
