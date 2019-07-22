package navigatorteam.cryptoproxy;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class P1Node implements LogProducer {

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
            P1Node p1Node = new P1Node(Consts.P1Port);

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
        socketWithP21 = new Socket(Consts.P21Host, Consts.P21Port);

        //socketWithClient.setSoTimeout(100000);	//if needed to add timeout
        print("Port: " + socketWithClient.getLocalPort());


    }


    private void startListening() throws IOException {
        print("Started listening...");
        listen = true;

        while (listen) {
            Socket socket = socketWithClient.accept(); //waits for new connection/request

            print("New request from client...");
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


        socketWithClient.close();
        socketWithP21.close();

    }


    //multithreaded
    public void handleRequest(Socket clientSocket) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String s = bufferedReader.readLine();

            print(s);

            print("encrypting...");
            String c = crypto.encrypt(s);

            print("sending to P21...");
            PrintWriter out = new PrintWriter(socketWithP21.getOutputStream(), true);
            out.println(c);

            print("Waiting for response...");
            BufferedReader p21In = new BufferedReader(new InputStreamReader(socketWithP21.getInputStream()));
            String resp_crypt = p21In.lines().collect(Collectors.joining());

            print("decrypting response...");
            String resp = crypto.decrypt(resp_crypt);

            print("RESPONSE: " + resp);

            print("Sending response to client...");
            PrintWriter client_out = new PrintWriter(clientSocket.getOutputStream(), true);

            client_out.println(resp);

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            activeThreads.remove(Thread.currentThread());
        }
    }


}