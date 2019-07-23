package navigatorteam.cryptoproxy;

import com.google.gson.Gson;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoServiceImplementation implements CryptoServiceProvider {

    private AsymmetricKey publicKey;
    private AsymmetricKey privateKey;
    private AsymmetricKey otherEntityPublicKey;
    private Gson gson = new Gson();
    //TODO SymmetricKey

    private BigInteger p;
    private BigInteger q;
    private BigInteger n;
    private BigInteger phiN;
    private BigInteger e;
    private BigInteger d;

    private int bigIntegerLength = 512;

    //TODO settare la chiave otherEntityPublicKey


    /*@Override
    public ExchangedObject encrypt(String input) {
        return null;
    }

    @Override
    public String decrypt(String input) {
        return null;
    }*/

    @Override
    public String encrypt(String input) {
        SymmetricKey sharedKey = new SharedKey(generateSharedKey());
        byte[] encryptedRequest = encryptSymmetric(input.getBytes(), sharedKey, "Encrypt");
        BigInteger keyBigInteger = new BigInteger(1, sharedKey.getKey().getBytes());
        otherEntityPublicKey = new RSAKey(publicKey.getExponent(), publicKey.getModulus());
        byte[] encryptedKey = keyBigInteger.modPow(otherEntityPublicKey.getExponent(), otherEntityPublicKey.getModulus()).toByteArray();
        ExchangedObject messageToSend = new ExchangedObject(Base64.getEncoder().encode(encryptedRequest), Base64.getEncoder().encode(encryptedKey));
        return gson.toJson(messageToSend);

    }

    @Override
    public String decrypt(String input) {
        ExchangedObject messageReceived = gson.fromJson(input, ExchangedObject.class);
        BigInteger keyBigInteger = new BigInteger(1, Base64.getDecoder().decode(messageReceived.encryptedSharedKey.getBytes()));
        String decryptedKey = new String(keyBigInteger.modPow(privateKey.getExponent(), privateKey.getModulus()).toByteArray());
        SymmetricKey sharedKey = new SharedKey(decryptedKey);
        byte[] decryptedRequest = encryptSymmetric(Base64.getDecoder().decode(messageReceived.encryptedRequest), sharedKey, "Decrypt");
        return new String(decryptedRequest);
    }

    private byte[] encryptSymmetric(byte[] input, SymmetricKey key, String cipherMode)
    {
        String algorithm = "AES";
        String mode = "ECB";
        String padding = "PKCS5Padding";
        Cipher cipherToUse = null;
        try {
            cipherToUse = Cipher.getInstance(algorithm + "/" + mode + "/" + padding);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        SecretKey secretKeyToUse = new SecretKeySpec(key.getKey().getBytes(), algorithm);
        try {
            if (cipherToUse != null) {
                if(cipherMode.equals("Encrypt"))
                    cipherToUse.init(Cipher.ENCRYPT_MODE, secretKeyToUse);
                else if(cipherMode.equals("Decrypt"))
                    cipherToUse.init(Cipher.DECRYPT_MODE, secretKeyToUse);
                return cipherToUse.doFinal(input);
            }
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    @Override
    public void generateKeys() {
        generateRandomPrimes();
        generateN();
        generateE();
        generateD();
        publicKey = new RSAKey(e, n);
        privateKey = new RSAKey(d, n);
    }

    @Override
    public AsymmetricKey getPublicKey() {
        return publicKey;
    }

    @Override
    public void setOtherEntityPublicKey(AsymmetricKey key) {
        this.otherEntityPublicKey = key;
    }

    private String generateSharedKey() {
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        if (keyGen != null) {
            keyGen.init(128);
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        }
        return null;
    }

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
