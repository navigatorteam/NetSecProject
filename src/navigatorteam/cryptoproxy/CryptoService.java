package navigatorteam.cryptoproxy;

import com.google.gson.Gson;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Created on 2019-07-25.
 */
public abstract class CryptoService implements CryptoServiceProvider {
    protected static Gson gson = new Gson();
    protected AsymmetricKey publicKey;
    protected AsymmetricKey privateKey;
    protected AsymmetricKey otherEntityPublicKey;


    public CryptoService(AsymmetricKey privateKey, AsymmetricKey publicKey, AsymmetricKey otherEntityPublicKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.otherEntityPublicKey = otherEntityPublicKey;
    }

    @Override
    public abstract String encrypt(String input);

    @Override
    public abstract String decrypt(String input) throws IntegrityCheckFailedException;

    protected byte[] encryptSymmetric(byte[] input, SymmetricKey key, String cipherMode) {
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
                if (cipherMode.equals("Encrypt"))
                    cipherToUse.init(Cipher.ENCRYPT_MODE, secretKeyToUse);
                else if (cipherMode.equals("Decrypt"))
                    cipherToUse.init(Cipher.DECRYPT_MODE, secretKeyToUse);
                return cipherToUse.doFinal(input);
            }
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public static byte[] generateHash(byte[] input) {
        //System.err.println("generateHash -- "+new String(input));

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return null;
    }



    protected String generateSharedKey() {
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

    @Override
    public String encryptToken(String token) {
        byte[] tokenBytes = token.getBytes();
        byte[] tokenBase64 = Base64.getEncoder().encode(tokenBytes);
        BigInteger tokenBigInteger = new BigInteger(1, tokenBase64);
        BigInteger encTokenBigInteger = tokenBigInteger.modPow(privateKey.getExponent(), privateKey.getModulus());
        byte[] encTokenBytes = encTokenBigInteger.toByteArray();
        byte[] encB64Token = Base64.getEncoder().encode(encTokenBytes);
        return new String(encB64Token);
    }

    @Override
    public String decryptToken(String encryptedToken) {
        byte[] encB64Token = encryptedToken.getBytes();

        byte[] encTokenBytes = Base64.getDecoder().decode(encB64Token);
        BigInteger encTokenBigInteger = new BigInteger(1, encTokenBytes);
        BigInteger tokenBigInteger = encTokenBigInteger.modPow(otherEntityPublicKey.getExponent(), otherEntityPublicKey.getModulus());
        byte[] tokenBase64 = tokenBigInteger.toByteArray();

        byte[] tokenBytes = Base64.getDecoder().decode(tokenBase64);
        return new String(tokenBytes);
    }

}
