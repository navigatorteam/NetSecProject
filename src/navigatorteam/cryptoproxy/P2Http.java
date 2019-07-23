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
            P2Http p2node = new P2Http(ConstsAndUtils.P21Port);


            p2node.waitForAuth();


            p2node.startListening();
        } catch (SocketException se) {
            System.out.println("Socket Exception when connecting to client");
            se.printStackTrace();
        } catch (SocketTimeoutException ste) {
            System.out.println("Timeout occured while connecting to client");
        } catch (IOException io) {
            System.out.println("IO exception when connecting to client");
        }
    }


    private void waitForAuth() {
        // TODO: implement
    }


    public P2Http(int port) throws IOException {
        //serverSocketWithP1 = new ServerSocket(port);
        httpServer = new TcpRawHttpServer(port);

        //serverSocketWithP1.setSoTimeout(100000);	//if needed to add timeout
        Logger.getLogger(getLoggerName()).info("Port: " + port);

        crypto = new CryptoServiceImplementation();

    }


    private void startListening() throws IOException {
        Logger.getLogger(getLoggerName()).info("Started listening...");
        listen = true;

        httpServer.start(req -> {

            if (req.getUri().toString().endsWith("/auth")) {
                try {
                    Optional<? extends BodyReader> bodyReaderOpt = req.getBody();
                    if (bodyReaderOpt.isPresent()) {
                        EagerBodyReader bodyReader = null;
                        bodyReader = bodyReaderOpt.get().eager();
                        String jsonReq = bodyReader.decodeBodyToString(Charset.forName("UTF-8"));
                        System.out.println("---> " + jsonReq);
                        AsymmetricKey otherpartyPublicKey = gson.fromJson(jsonReq, RSAKey.class);

                        crypto.setOtherEntityPublicKey(otherpartyPublicKey);
                        crypto.generateKeys();
                        AsymmetricKey publicKey = crypto.getPublicKey();
                        String jsonResp = gson.toJson(publicKey);
                        System.out.println("<--- "+jsonResp);
                        RawHttpResponse<Void> rawHttpResponse = rawHttp.parseResponse("200 OK\n" +
                                "Content-Length: " + jsonResp.length() + "\n" +
                                "\n" +
                                jsonResp);

                        return Optional.of(rawHttpResponse);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {

                    Optional<? extends BodyReader> bodyReaderOpt = req.getBody();
                    if (bodyReaderOpt.isPresent()) {
                        EagerBodyReader bodyReader = bodyReaderOpt.get().eager();
                        String b64Req = bodyReader.decodeBodyToString(Charset.forName("UTF-8"));
                        String cryptedReq = new String(Base64.getDecoder().decode(b64Req));
                        String jsonReq = crypto.decrypt(cryptedReq);
                        System.out.println("---> " + jsonReq);
                        ReqContainer clientReq = gson.fromJson(jsonReq, ReqContainer.class);

                        RawHttpRequest rawClientReq = clientReq.getReq();

                        RawHttpClient<Void> rawHttpClient = new TcpRawHttpClient();
                        RawHttpResponse<Void> serverResp = rawHttpClient.send(rawClientReq).eagerly();
                        RespContainer respContainer = new RespContainer(serverResp);
                        String jsonResp = gson.toJson(respContainer);
                        System.out.println("<--- " + jsonResp);
                        String cryptedResp = crypto.encrypt(jsonResp);
                        String b64Resp = Base64.getEncoder().encodeToString(cryptedResp.getBytes());

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


}
