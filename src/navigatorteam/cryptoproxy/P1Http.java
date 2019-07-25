package navigatorteam.cryptoproxy;


import com.google.gson.Gson;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpHeaders;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.body.BodyReader;
import rawhttp.core.body.BytesBody;
import rawhttp.core.body.HttpMessageBody;
import rawhttp.core.body.StringBody;
import rawhttp.core.server.TcpRawHttpServer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class P1Http implements LogProducer {

    private static RawHttp rawHttp = new RawHttp();
    private static Gson gson = new Gson();


    private TcpRawHttpServer httpServer;
    private CryptoServiceProvider crypto = null;
    private String myIdToken = null;



    public static void main(String args[]) {
        try {
            P1Http p1Node = new P1Http(ConstsAndUtils.P1Port);
            p1Node.auth();
            p1Node.startListening();

        } catch (SocketException se) {
            System.out.println("Socket Exception when connecting");
            se.printStackTrace();
        } catch (SocketTimeoutException ste) {
            System.out.println("Timeout occurred when connecting");
        } catch (UnknownHostException uhe) {
            System.out.println("Unknown P2 host");
        } catch (IOException io) {
            System.out.println("IO exception when connecting");
        }
    }


    public P1Http(int port) {
        httpServer = new TcpRawHttpServer(port);


        log().info("Port: " + port);


    }



    private void auth() throws IOException {


        log().info("Begin auth request.");

        URL url = new URL("http://" + ConstsAndUtils.P2Host + ":" + ConstsAndUtils.P2Port + "/auth");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        KeyPairGenerator keyPairGenerator = new KeyPairGenerator();
        keyPairGenerator.generateKeys();
        AsymmetricKey publicKey = keyPairGenerator.getPublicKey();
        AsymmetricKey privateKey = keyPairGenerator.getPrivateKey();




        String jsonReq = gson.toJson(new AuthRequest(publicKey));
        log().info("AUTH: ---> " + jsonReq);
        byte[] outputInBytes = jsonReq.getBytes(StandardCharsets.UTF_8);

        con.setUseCaches(false);
        con.setDoOutput(true);

        OutputStream p2out = con.getOutputStream();
        p2out.write(outputInBytes);
        p2out.flush();
        p2out.close();


        InputStream is = null;

        if (con.getResponseCode() > 299) {
            is = con.getErrorStream();
        } else {
            is = con.getInputStream();
        }


        StringBuilder content = new StringBuilder();
        byte[] buf = new byte[512];

        while (true) {
            int bytesRead = is.read(buf, 0, 512);
            if (bytesRead <= 0) {
                break;
            }
            String s = new String(Arrays.copyOfRange(buf, 0, bytesRead), StandardCharsets.UTF_8);
            content.append(s);
        }


        String jsonResp = content.toString();
        log().info("AUTH: <--- " + jsonResp);

        AuthResponse authResponse = gson.fromJson(jsonResp, AuthResponse.class);

        AsymmetricKey otherEntityPublicKey = authResponse.getP2PublicKey();
        myIdToken = authResponse.getEncryptedChosenToken();

        if (ConstsAndUtils.PLAINTEXT_MODE) {
            crypto = new DummyCrypto();
        } else if (ConstsAndUtils.INTEGRITY_CHECK) {
            crypto = new CryptoServiceImplementation(privateKey, publicKey, otherEntityPublicKey);
        } else {
            crypto = new CryptoNoIntegrity(privateKey, publicKey, otherEntityPublicKey);
        }


        con.disconnect();

        log().info("End auth phase.");


    }


    private void startListening() throws IOException {
        log().info("Started P1 Server...");

        httpServer.start(req -> {
            int id = ConstsAndUtils.nextID();
            try {
                log().info("Got http request from client. LogID = " + id);

                URL url = new URL("http://" + ConstsAndUtils.P2Host + ":" + ConstsAndUtils.P2Port + "/");
                //open connection with P2
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //prepare a POST with the serialized, encrypted, encoded original request
                con.setRequestMethod("POST");
                //con.setRequestProperty("Content-Type", "application/json");
                ReqContainer reqC = new ReqContainer(req);
                String jsonReq = gson.toJson(reqC);
                log().info("REQ" + id + ": ---> " + jsonReq);
                String cryptedReq = crypto.encrypt(jsonReq);
                String b64Req = Base64.getEncoder().withoutPadding().encodeToString(cryptedReq.getBytes());


                P1Request p1Request = new P1Request(myIdToken, b64Req);
                String p1RequestString = gson.toJson(p1Request);

                byte[] outputInBytes = p1RequestString.getBytes(StandardCharsets.UTF_8);
                con.setUseCaches(false);
                con.setDoOutput(true);

                OutputStream p2out = con.getOutputStream();
                p2out.write(outputInBytes);
                p2out.flush();
                p2out.close();


                InputStream is = null;

                if (con.getResponseCode() > 299) {
                    is = con.getErrorStream();
                } else {
                    is = con.getInputStream();
                }


                StringBuilder content = new StringBuilder();
                byte[] buf = new byte[512];

                while (true) {
                    int bytesRead = is.read(buf, 0, 512);
                    if (bytesRead <= 0) {
                        break;
                    }
                    String s = new String(Arrays.copyOfRange(buf, 0, bytesRead), StandardCharsets.UTF_8);
                    content.append(s);
                }


                //log().info(content.toString());

                if (con.getResponseCode() == 200) {
                    String b64Resp = content.toString();
                    System.out.println(b64Resp);
                    String cryptResp = new String(Base64.getDecoder().decode(b64Resp.trim()));
                    String jsonResp = crypto.decrypt(cryptResp);
                    log().info("RSP" + id + ": <--- " + jsonResp);

                    RespContainer resp = gson.fromJson(jsonResp, RespContainer.class);
                    con.disconnect();

                    RawHttpResponse<Void> serverResp = resp.getResponse(rawHttp);

                    Optional<? extends BodyReader> body = serverResp.getBody();
                    List<String> contentTypes = serverResp.getHeaders().get("Content-Type");
                    List<String> transferEncodings = serverResp.getHeaders().get("Transfer-Encoding");
                    if (body.isPresent()
                            && (!contentTypes.isEmpty() && contentTypes.stream()
                            .map(String::toLowerCase)
                            .noneMatch(x -> x.startsWith("plain") || x.startsWith("text"))
                            || !transferEncodings.isEmpty() && transferEncodings.stream()
                            .map(String::toLowerCase)
                            .noneMatch(x -> x.equals("chunked")))) {
                        BodyReader bodyReaderResp = body.get();
                        byte[] decodedBodyBytes = Base64.getDecoder().decode(bodyReaderResp.decodeBody());
                        HttpMessageBody decodedBody = new BytesBody(decodedBodyBytes);
                        serverResp = serverResp.withBody(decodedBody);

                        RawHttpHeaders newContentSize = RawHttpHeaders.newBuilder().with("Content-Length", "" + decodedBodyBytes.length).build();
                        serverResp = serverResp.withHeaders(newContentSize);
                    }


                    return Optional.ofNullable(serverResp);
                } else {
                    log().info("RSP" + id + ": Received " + con.getResponseCode() + " response code from P2!");
                    return Optional.ofNullable(rawHttp.parseResponse(compileHeaders(con.getHeaderFields()) +
                            "\r\n" + content.toString()));
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IntegrityCheckFailedException e) {
                //TODO manage
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }


            if (ConstsAndUtils.DEBUG_CLOSE_ON_FAIL) {
                System.out.flush();
                System.err.flush();
                System.exit(1);
            }

            log().info("ID " + id + ": Something went wrong. Sending 500 response to client.");

            return Optional.ofNullable((RawHttpResponse<Void>) rawHttp.parseResponse("HTTP/1.1 500 Internal Server Error\r\n" +
                    "Content-Type: text/plain"
            ).withBody(new StringBody("Error in proxy server.")));
        });
    }


    private static String compileHeaders(Map<String, List<String>> headerFields) {
        StringBuilder sb = new StringBuilder();
        headerFields.forEach((k, l) -> {

            if (k != null) {
                sb.append(k).append(": ");
            }
            for (String s : l) {
                sb.append(s).append(" ");
            }
            sb.append("\r\n");
        });

        return sb.toString();
    }


}