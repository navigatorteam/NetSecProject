package navigatorteam.cryptoproxy;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class CryptoServiceImplementation implements CryptoServiceProvider {

    private AsymmetricKey publicKey;
    private AsymmetricKey privateKey;
    private AsymmetricKey otherEntityPublicKey;
    private SymmetricKey sharedKey;
    private ExchangedObject message;
    //TODO SymmetricKey

    private BigInteger p;
    private BigInteger q;
    private BigInteger n;
    private BigInteger phiN;
    private BigInteger e;
    private BigInteger d;

    private int bigIntegerLength = 512;

    //TODO settare la chiave otherEntityPublicKey


    @Override
    public ExchangedObject encrypt(String input) {
        return null;
    }

    @Override
    public String decrypt(String input) {
        return null;
    }

    /* ho dovuto commentare perché non compilava
        @Override
        public String encrypt(String input) {
            sharedKey = new SharedKey(generateSharedKey());
            //TODO String encryptedRequest = encryptSymmetric(input);
            BigInteger keyBigInteger = new BigInteger(1, sharedKey.getKey().getBytes());
            String encryptedKey = new  String(keyBigInteger.modPow(otherEntityPublicKey.getExponent(), otherEntityPublicKey.getModulus()).toByteArray());
            //TODO integrità
            message = new ExchangedObject(encryptedRequest, encryptedKey);
            //TODO returnare il messaggio (.toString per json)
            return null;
        }

        @Override
        public String decrypt(String input) {
            //TODO deserializza l'oggetto / json
            ExchangedObject messageRecv= new ExchangedObject("","");
            BigInteger keyBigInteger = new BigInteger(1, messageRecv.encryptedSharedKey.getBytes());
            String decryptedKey = new String(keyBigInteger.modPow(privateKey.getExponent(), privateKey.getModulus()).toByteArray());
            //TODO settare in sharedkey il campo chiave
            //String decryptedRequest= decryptSymmetric(messageRecv.encryptedRequest);
            //Return decryptedRequest
            return null
        }
    */
    @Override
    public void generateKeys() {
        generateRandomPrimes();
        generateN();
        generateE();
        generateD();
        publicKey = new RSAKey(e, n);
        privateKey = new RSAKey(d, n);
    }

    /*
    private String generateSharedKey() {
        KeyGenerator keyGen = null;
        keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
    *
     */

    private void generateRandomPrimes()
    {
        Random randomNumber = new SecureRandom();
        this.p = BigInteger.probablePrime(bigIntegerLength, randomNumber);
        this.q = BigInteger.probablePrime(bigIntegerLength, randomNumber);
    }

    private void generateN() {
        this.n = p.multiply(q);
        generatePhiN();
    }

    private void generatePhiN() {
        this.phiN = p.subtract(BigInteger.valueOf(1)).multiply(q.subtract(BigInteger.valueOf(1)));
    }

    private void generateE() {
        Random randomNumber = new SecureRandom();
        BigInteger probableE;
        byte[] generatedRandom = new byte[bigIntegerLength/8];
        do {
            randomNumber.nextBytes(generatedRandom);
            probableE = new BigInteger(1, generatedRandom);
        }while((probableE.equals(BigInteger.valueOf(1))) || (phiN.compareTo(probableE) < 1) || (!(phiN.gcd(probableE).equals(BigInteger.valueOf(1)))));
        this.e = probableE;
    }

    private void generateD() {
        this.d = e.modInverse(phiN);
    }

}
