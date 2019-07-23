package navigatorteam.cryptoproxy;

public class TestEncryption {

    public static void main(String args[]) {
        CryptoServiceProvider beppeBergomi = new CryptoServiceImplementation();
        beppeBergomi.generateKeys();
        String cieloAzzurro = beppeBergomi.encrypt("ANDIAMO A BERLINO!");
        System.out.println("Encrypted message = " + cieloAzzurro);
        String sopraBerlino = beppeBergomi.decrypt(cieloAzzurro);
        System.out.println("Decrypted message = " + sopraBerlino);
    }

}
