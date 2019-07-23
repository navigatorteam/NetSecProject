package navigatorteam.cryptoproxy;

import com.google.gson.Gson;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.body.BodyReader;
import rawhttp.core.body.EagerBodyReader;
import rawhttp.core.body.StringBody;
import rawhttp.core.client.RawHttpClient;
import rawhttp.core.client.TcpRawHttpClient;
import rawhttp.core.server.TcpRawHttpServer;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created on 2019-07-22.
 */
public class P2Http implements LogProducer {


    public static RawHttp rawHttp = new RawHttp();
    public static Gson gson = new Gson();

    //private final ServerSocket serverSocketWithP1;
    private TcpRawHttpServer httpServer;

    private CryptoServiceProvider crypto = null;

    private boolean listen = false;
    private final Set<Thread> activeThreads = Collections.synchronizedSet(new HashSet<>());

    @Override
    public String getLoggerName() {
        return "P21";
    }

    public static void main(String args[]) {
        try {
            P2Http p21Node = new P2Http(ConstsAndUtils.P21Port);

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


    public P2Http(int port) throws IOException {
        //serverSocketWithP1 = new ServerSocket(port);
        httpServer = new TcpRawHttpServer(port);

        //serverSocketWithP1.setSoTimeout(100000);	//if needed to add timeout
        Logger.getLogger(getLoggerName()).info("Port: " + port);


    }


    private void startListening() throws IOException {
        Logger.getLogger(getLoggerName()).info("Started listening...");
        listen = true;

        httpServer.start(req -> {
            try {

                Optional<? extends BodyReader> bodyReaderOpt = req.getBody();
                if(bodyReaderOpt.isPresent()){
                    EagerBodyReader bodyReader = bodyReaderOpt.get().eager();
                    String b64Req = bodyReader.decodeBodyToString(Charset.forName("UTF-8"));
                    //TODO decrypt
                    String jsonReq = new String(Base64.getDecoder().decode(b64Req));
                    System.out.println("---> "+jsonReq);
                    ReqContainer clientReq = gson.fromJson(jsonReq, ReqContainer.class);

                    RawHttpRequest rawClientReq = clientReq.getReq();

                    RawHttpClient<Void> rawHttpClient = new TcpRawHttpClient();
                    RawHttpResponse<Void> serverResp = rawHttpClient.send(rawClientReq).eagerly();
                    RespContainer respContainer = new RespContainer(serverResp);
                    String jsonResp = gson.toJson(respContainer);
                    System.out.println("<--- "+jsonResp);
                    //todo encrypt
                    String b64Resp = Base64.getEncoder().encodeToString(jsonResp.getBytes());

                    String utf8Resp = new String(b64Resp.getBytes(StandardCharsets.UTF_8));
                    RawHttpResponse<Void> rawHttpResponse = rawHttp.parseResponse("200 OK\n" +
                            "Content-Length: " + utf8Resp.length() + "\n" +
                            "\n" +
                            utf8Resp);
                    System.out.println(rawHttpResponse.toString());
                    return Optional.of(rawHttpResponse);

                }




            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Optional.ofNullable((RawHttpResponse<Void>) rawHttp.parseResponse("HTTP/1.1 500 Internal Server Error\n" +
                    "Content-Type: text/plain"
            ).withBody(new StringBody("Error in proxy server.")));

        });
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


        //serverSocketWithP1.close();


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
                        return gson.toJson(crypto.encrypt(s));
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
