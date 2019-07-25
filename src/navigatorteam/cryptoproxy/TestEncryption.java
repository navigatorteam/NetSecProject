package navigatorteam.cryptoproxy;

import java.util.Arrays;
import java.util.Base64;

public class TestEncryption {

    public static final String beppe =  "{\"req\":{\"requestLine\":{\"method\":\"GET\",\"uri\":\"http://www.apache.org/\",\"httpVersion\":\"HTTP_1_1\"},\"senderAddress\":\"127.0.0.1\",\"headers\":{\"headersByCapitalizedName\":{\"HOST\":{\"values\":[\"www.apache.org\"]},\"USER-AGENT\":{\"values\":[\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:68.0) Gecko/20100101 Firefox/68.0\"]},\"ACCEPT\":{\"values\":[\"text/html,application/xhtml+xml,application/xml;q\\u003d0.9,*/*;q\\u003d0.8\"]},\"ACCEPT-LANGUAGE\":{\"values\":[\"it-IT,it;q\\u003d0.8,en-US;q\\u003d0.5,en;q\\u003d0.3\"]},\"ACCEPT-ENCODING\":{\"values\":[\"identity\"]},\"DNT\":{\"values\":[\"1\"]},\"CONNECTION\":{\"values\":[\"keep-alive\"]},\"UPGRADE-INSECURE-REQUESTS\":{\"values\":[\"1\"]},\"CACHE-CONTROL\":{\"values\":[\"max-age\\u003d0\"]}},\"headerNames\":[\"Host\",\"User-Agent\",\"Accept\",\"Accept-Language\",\"Accept-Encoding\",\"DNT\",\"Connection\",\"Upgrade-Insecure-Requests\",\"Cache-Control\"]}}}-";

    public static void main(String args[]) {

        for(int i = 0; i < 128 ; i++){
            System.out.println("\n\n------");
            String c = Character.toString(i);
            String myStr = beppe + c;
            System.out.println(myStr.length());

            KeyPairGenerator keyPairGenerator = new KeyPairGenerator();
            keyPairGenerator.generateKeys();

            CryptoServiceProvider beppeBergomi = new CryptoServiceImplementation(
                    keyPairGenerator.getPrivateKey(),
                    keyPairGenerator.getPublicKey(),
                    keyPairGenerator.getPublicKey());



            //System.out.println("before message    = "+myStr);
            String cieloAzzurro = beppeBergomi.encrypt(myStr);
            //System.out.println("Encrypted message = " + cieloAzzurro);
            String sopraBerlino = null;
            try {
                sopraBerlino = beppeBergomi.decrypt(cieloAzzurro);
            } catch (IntegrityCheckFailedException e) {
                System.out.println("ERRORE CON "+c);
              //  System.out.println("Decrypted message = "+e.getMessage());
                sopraBerlino = e.getMessage();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
//            System.out.println(Arrays.equals(myStr.getBytes(), sopraBerlino.getBytes()));
//            System.out.println(Arrays.equals(CryptoServiceImplementation.generateHash(myStr.getBytes()), CryptoServiceImplementation.generateHash(sopraBerlino.getBytes())));
//            System.out.println("Decrypted message = " + sopraBerlino);
        }
    }

}
