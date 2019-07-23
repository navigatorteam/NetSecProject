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
import java.util.*;
import java.util.logging.Logger;

public class P1Http implements LogProducer {

    public static RawHttp rawHttp = new RawHttp();
    public static Gson gson = new Gson();


    private TcpRawHttpServer httpServer;


    private CryptoServiceProvider crypto = null;


    private boolean listen = false;
    private final Set<Thread> activeThreads = Collections.synchronizedSet(new HashSet<>());

    @Override
    public String getLoggerName() {
        return "P1";
    }

    public static void main(String args[]) {
        try {
            P1Http p1Node = new P1Http(ConstsAndUtils.P1Port);

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


    public P1Http(int port) {
        httpServer = new TcpRawHttpServer(port);


        //serverSocketWithClient.setSoTimeout(100000);	//if needed to add timeout
        Logger.getLogger(getLoggerName()).info("Port: " + port);


    }


    private void startListening() throws IOException {
        Logger.getLogger(getLoggerName()).info("Started P1 Server...");




        httpServer.start(req -> {
            try {


                URL url = new URL("http://" + ConstsAndUtils.P21Host + ":" + ConstsAndUtils.P21Port + "/");
                //open connection with P2
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //prepare a POST with the serialized, encrypted, encoded original request
                con.setRequestMethod("POST");
                //con.setRequestProperty("Content-Type", "application/json");
                ReqContainer reqC = new ReqContainer(req);
                String jsonReq = gson.toJson(reqC);
                System.out.println(jsonReq);
                //TODO encrypt
                String b64Req = Base64.getEncoder().encodeToString(jsonReq.getBytes());
                byte[] outputInBytes = b64Req.getBytes("UTF-8");
                con.setUseCaches(false);
                con.setDoOutput(true);


                //TODO is this the body?

                OutputStream p2out = con.getOutputStream();
                p2out.write(outputInBytes);
                p2out.flush();
                p2out.close();


                int status = con.getResponseCode();
                InputStream inFromP2;

                if (status > 299) {
                    inFromP2 = con.getErrorStream();
                } else {
                    inFromP2 = con.getInputStream();
                }

                RawHttpResponse<Void> httpResponse = rawHttp.parseResponse(inFromP2).eagerly();
                Optional<? extends BodyReader> body = httpResponse.getBody();
                if (body.isPresent()) {
                    EagerBodyReader bodyReader = body.get().eager();
                    String b64Resp = bodyReader.decodeBodyToString(Charset.forName("UTF-8"));
                    //TODO decrypt
                    String jsonResp = new String(Base64.getDecoder().decode(b64Resp));
                    System.out.println("<--- "+jsonResp);
//                    RawHttpResponse<Void> dummy =  rawHttp.parseResponse("HTTP/1.1 200 OK\n" +
//                            "Content-Type: text/plain"
//                    ).withBody(new StringBody("Hello RawHTTP!"));
                    RespContainer resp = gson.fromJson(jsonResp, RespContainer.class);

                    return Optional.ofNullable(resp.getResponse(rawHttp));
                } else {
                    return Optional.of(httpResponse);
                }

                //String contentType = con.getHeaderField("Content-Type");
                //con.setConnectTimeout(5000);
                //con.setReadTimeout(5000);




                /* //TODO recursive method to handle redirects
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM) {
                    String location = con.getHeaderField("Location");
                    URL newUrl = new URL(location);
                    con = (HttpURLConnection) newUrl.openConnection();
                }

                 */


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

/*
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


        serverSocketWithClient.close();

    }
*/

    //multithreaded
    public void handleRequest(Socket clientSocket, int internalReqID) {
        try (Socket p21Socket = new Socket(ConstsAndUtils.P21Host, ConstsAndUtils.P21Port)) {

            BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter p21out = new PrintWriter(p21Socket.getOutputStream(), true);
            Pipe pipeClientToP21 = new Pipe(activeThreads, clientIn, p21out, s -> {
                Logger.getLogger(getLoggerName()).info("REQ" + internalReqID + ": ---> " + s);
                return gson.toJson(crypto.encrypt(s));
            });
            activeThreads.add(pipeClientToP21);

            BufferedReader p21In = new BufferedReader(new InputStreamReader(p21Socket.getInputStream()));
            PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream());
            Pipe pipeP21toClient = new Pipe(activeThreads, p21In, clientOut, s -> {
                String decS = crypto.decrypt(s);
                Logger.getLogger(getLoggerName()).info("RSP" + internalReqID + ": <--- " + decS);
                return decS;
            });
            activeThreads.add(pipeP21toClient);

            pipeClientToP21.start();
            pipeP21toClient.start();

            pipeClientToP21.join();
            pipeP21toClient.join();


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            activeThreads.remove(Thread.currentThread());
        }
    }


}