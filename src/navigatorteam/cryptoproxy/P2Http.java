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


    private static RawHttp rawHttp = new RawHttp();
    private static Gson gson = new Gson();


    private TcpRawHttpServer httpServer;
    private CryptoServiceProvider crypto = null;





    public static void main(String args[]) {
        try {
            P2Http p2node = new P2Http(ConstsAndUtils.P2Port);

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





    public P2Http(int port) throws IOException {
        //serverSocketWithP1 = new ServerSocket(port);
        httpServer = new TcpRawHttpServer(port);

        //serverSocketWithP1.setSoTimeout(100000);	//if needed to add timeout
        log().info("Port: " + port);

        if(ConstsAndUtils.PLAINTEXT_MODE) {
            crypto = new DummyCrypto();
        } else {
            crypto = new CryptoServiceImplementation();
        }

    }


    private void startListening() throws IOException {
        log().info("Started listening...");

        httpServer.start(req -> {

            if (req.getUri().toString().endsWith("/auth")) {
                try {
                    log().info("Received auth request...");
                    Optional<? extends BodyReader> bodyReaderOpt = req.getBody();
                    if (bodyReaderOpt.isPresent()) {
                        EagerBodyReader bodyReader = null;
                        bodyReader = bodyReaderOpt.get().eager();
                        String jsonReq = bodyReader.decodeBodyToString(Charset.forName("UTF-8"));
                        log().info("AUTH: ---> " + jsonReq);
                        AsymmetricKey otherpartyPublicKey = gson.fromJson(jsonReq, RSAKey.class);

                        crypto.setOtherEntityPublicKey(otherpartyPublicKey);
                        crypto.generateKeys();
                        AsymmetricKey publicKey = crypto.getPublicKey();
                        String jsonResp = gson.toJson(publicKey);
                        log().info("AUTH: <--- "+jsonResp);
                        RawHttpResponse<Void> rawHttpResponse = rawHttp.parseResponse("200 OK\n" +
                                "Content-Length: " + jsonResp.length() + "\n" +
                                "\n" +
                                jsonResp);
                        log().info("End auth phase.");
                        return Optional.of(rawHttpResponse);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    int id = ConstsAndUtils.nextID();
                    log().info("Got http request from P1. LogID = "+id);
                    Optional<? extends BodyReader> bodyReaderOpt = req.getBody();
                    if (bodyReaderOpt.isPresent()) {
                        EagerBodyReader bodyReader = bodyReaderOpt.get().eager();
                        String b64Req = bodyReader.decodeBodyToString(Charset.forName("UTF-8"));
                        String cryptedReq = new String(Base64.getDecoder().decode(b64Req));
                        String jsonReq = crypto.decrypt(cryptedReq);
                        log().info("REQ"+id+": ---> " + jsonReq);
                        ReqContainer clientReq = gson.fromJson(jsonReq, ReqContainer.class);

                        RawHttpRequest rawClientReq = clientReq.getReq();

                        RawHttpClient<Void> rawHttpClient = new TcpRawHttpClient();
                        RawHttpResponse<Void> serverResp = rawHttpClient.send(rawClientReq).eagerly();
                        RespContainer respContainer = new RespContainer(serverResp);
                        String jsonResp = gson.toJson(respContainer);
                        log().info("RSP"+id+": <--- " + jsonResp);
                        String cryptedResp = crypto.encrypt(jsonResp);
                        String b64Resp = Base64.getEncoder().encodeToString(cryptedResp.getBytes());

                        String utf8Resp = new String(b64Resp.getBytes(StandardCharsets.UTF_8));
                        RawHttpResponse<Void> rawHttpResponse = rawHttp.parseResponse("200 OK\n" +
                                "Content-Length: " + utf8Resp.length() + "\n" +
                                "\n" +
                                utf8Resp);

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


            log().info("Something failed. Returning 500 to P1.");
            return Optional.ofNullable((RawHttpResponse<Void>) rawHttp.parseResponse("HTTP/1.1 500 Internal Server Error\n" +
                    "Content-Type: text/plain"
            ).withBody(new StringBody("Error in proxy server.")));

        });
    }





}
