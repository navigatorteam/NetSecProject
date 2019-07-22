package navigatorteam.cryptoproxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created on 2019-07-22.
 */
public class P22Node implements LogProducer{
    private final ServerSocket socketWithP21;

    private boolean listen = false;
    private final Set<Thread> activeThreads = Collections.synchronizedSet(new HashSet<>());

    @Override
    public String getLoggerName() {
        return "P22";
    }

    public static void main(String args[]) {
        try {
            P22Node p22Node = new P22Node(Consts.P21Port);

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
            Thread thread = new Thread(new Requester(socket,
                    () -> activeThreads.remove(Thread.currentThread())) //lambda called at the end to keep clean the thread set
            );

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

    public static class Requester implements Runnable, LogProducer {

        private Socket p21socket;
        private final Runnable onEnd;

        public Requester(Socket p21socket, Runnable onEnd){
            this.p21socket = p21socket;
            this.onEnd = onEnd;
        }


        @Override
        public void run() {
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p21socket.getInputStream()));
                String s = bufferedReader.readLine();

                print(s);


                //TODO request to external server


            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                onEnd.run();
            }

        }

        @Override
        public String getLoggerName() {
            return "P22";
        }
    }


}
