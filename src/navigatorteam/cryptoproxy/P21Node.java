package navigatorteam.cryptoproxy;

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

/**
 * Created on 2019-07-22.
 */
public class P21Node implements LogProducer{

    private final ServerSocket socketWithP1;
    private final Socket socketWithP22;


    private boolean listen = false;
    private final Set<Thread> activeThreads = Collections.synchronizedSet(new HashSet<>());

    @Override
    public String getLoggerName() {
        return "P21";
    }

    public static void main(String args[]) {
        try {
            P21Node p21Node = new P21Node(Consts.P21Port);

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


    public P21Node(int port) throws IOException {
        socketWithP1 = new ServerSocket(port);
        socketWithP22 = new Socket(Consts.P22Host, Consts.P22Port);

        //socketWithP1.setSoTimeout(100000);	//if needed to add timeout
        print("Port: " + socketWithP1.getLocalPort());


    }


    private void startListening() throws IOException {
        print("Started listening...");
        listen = true;

        while (listen) {
            Socket socket = socketWithP1.accept(); //waits for new connection/request

            print("new request from P1...");
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


        socketWithP1.close();
        socketWithP22.close();

    }


    //multithreaded
    public void handleRequest(Socket clientSocket) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String s = bufferedReader.readLine();

            print(s);


            PrintWriter out = new PrintWriter(socketWithP22.getOutputStream(), true);
            out.println(s);

        } catch (IOException e) {
            e.printStackTrace();
        }finally {

            activeThreads.remove(Thread.currentThread());
        }

    }
}
