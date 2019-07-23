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
                System.out.println("---> "+jsonReq);
                String cryptedReq = crypto.encrypt(jsonReq);
                String b64Req = Base64.getEncoder().encodeToString(cryptedReq.getBytes());
                byte[] outputInBytes = b64Req.getBytes(StandardCharsets.UTF_8);
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

                while(true){
                    int bytesRead = is.read(buf, 0, 512);
                    if(bytesRead <= 0){
                        break;
                    }
                    String s = new String(Arrays.copyOfRange(buf,0, bytesRead), StandardCharsets.UTF_8);
                    content.append(s);
                }


                System.out.println(content.toString());





                String b64Resp = content.toString();
                String plainResp = crypto.decrypt(b64Resp);
                String jsonResp = new String(Base64.getDecoder().decode(plainResp));
                System.out.println("<--- "+jsonResp);

                RespContainer resp = gson.fromJson(jsonResp, RespContainer.class);

                return Optional.ofNullable(resp.getResponse(rawHttp));




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



}