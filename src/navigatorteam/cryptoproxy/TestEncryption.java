package navigatorteam.cryptoproxy;

public class TestEncryption {

    public static void main(String args[]) {
        CryptoServiceProvider beppeBergomi = new CryptoServiceImplementation();
        beppeBergomi.generateKeys();
        String cieloAzzurro = beppeBergomi.encrypt("{\"req\":{\"requestLine\":{\"method\":\"GET\",\"uri\":\"http://www.apache.org/\",\"httpVersion\":\"HTTP_1_1\"},\"senderAddress\":\"127.0.0.1\",\"headers\":{\"headersByCapitalizedName\":{\"HOST\":{\"values\":[\"www.apache.org\"]},\"USER-AGENT\":{\"values\":[\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:68.0) Gecko/20100101 Firefox/68.0\"]},\"ACCEPT\":{\"values\":[\"text/html,application/xhtml+xml,application/xml;q\\u003d0.9,*/*;q\\u003d0.8\"]},\"ACCEPT-LANGUAGE\":{\"values\":[\"it-IT,it;q\\u003d0.8,en-US;q\\u003d0.5,en;q\\u003d0.3\"]},\"ACCEPT-ENCODING\":{\"values\":[\"identity\"]},\"DNT\":{\"values\":[\"1\"]},\"CONNECTION\":{\"values\":[\"keep-alive\"]},\"UPGRADE-INSECURE-REQUESTS\":{\"values\":[\"1\"]},\"CACHE-CONTROL\":{\"values\":[\"max-age\\u003d0\"]}},\"headerNames\":[\"Host\",\"User-Agent\",\"Accept\",\"Accept-Language\",\"Accept-Encoding\",\"DNT\",\"Connection\",\"Upgrade-Insecure-Requests\",\"Cache-Control\"]}}}");
        System.out.println("Encrypted message = " + cieloAzzurro);
        String sopraBerlino = beppeBergomi.decrypt(cieloAzzurro);
        System.out.println("Decrypted message = " + sopraBerlino);
    }

}
