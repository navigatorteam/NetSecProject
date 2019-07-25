package navigatorteam.cryptoproxy;


import java.math.BigInteger;
import java.util.Base64;

public class CryptoNoIntegrity extends CryptoService implements CryptoServiceProvider {



    public CryptoNoIntegrity(AsymmetricKey privateKey, AsymmetricKey publicKey, AsymmetricKey otherEntityPublicKey) {
        super(privateKey, publicKey, otherEntityPublicKey);
    }


    @Override
    public String encrypt(String input) {
        SymmetricKey sharedKey = new SharedKey(generateSharedKey());
        byte[] encryptedRequest = encryptSymmetric(input.getBytes(), sharedKey, "Encrypt");
        BigInteger keyBigInteger = new BigInteger(1, sharedKey.getKey().getBytes());
        //otherEntityPublicKey = new RSAKey(publicKey.getExponent(), publicKey.getModulus());
        byte[] encryptedKey = keyBigInteger.modPow(otherEntityPublicKey.getExponent(), otherEntityPublicKey.getModulus()).toByteArray();
        ExchangedObjectNoIntegrity messageToSend = new ExchangedObjectNoIntegrity(Base64.getEncoder().encode(encryptedRequest), Base64.getEncoder().encode(encryptedKey));
        return gson.toJson(messageToSend);

    }

    @Override
    public String decrypt(String input) {
        ExchangedObjectNoIntegrity messageReceived = gson.fromJson(input, ExchangedObjectNoIntegrity.class);
        BigInteger keyBigInteger = new BigInteger(1, Base64.getDecoder().decode(messageReceived.encryptedSharedKey.getBytes()));
        String decryptedKey = new String(keyBigInteger.modPow(privateKey.getExponent(), privateKey.getModulus()).toByteArray());
        SymmetricKey sharedKey = new SharedKey(decryptedKey);
        byte[] decryptedRequest = encryptSymmetric(Base64.getDecoder().decode(messageReceived.encryptedRequest), sharedKey, "Decrypt");
        return new String(decryptedRequest);
    }




}
