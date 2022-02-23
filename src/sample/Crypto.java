package sample;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import javax.crypto.Cipher;
import org.bouncycastle.jce.spec.IESParameterSpec;

public class Crypto {
    private SecureRandom random;
    private int keySize;
    protected KeyPair key;
    protected PrivateKey privateKey;

    public Crypto() throws Exception{
        this.random = new SecureRandom();
    }

    public void establishKeys(String keysize) throws Exception {
        ECGenParameterSpec     ecGenSpec = new ECGenParameterSpec(keysize);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        keyGen.initialize(ecGenSpec, random);
        this.key = keyGen.generateKeyPair();
        this.keySize = Integer.valueOf( (ecGenSpec.getName().substring(4, 7)) ).intValue();
    }

    public byte[] encrypt(byte[] plainText) throws Exception {
        // get ECIES cipher objects
        Cipher acipher = Cipher.getInstance("ECIES");
        //  generate derivation and encoding vectors
        byte[]  d = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
        byte[]  e = new byte[] { 8, 7, 6, 5, 4, 3, 2, 1 };
        IESParameterSpec param = new IESParameterSpec(d, e, 256);
        acipher.init(Cipher.ENCRYPT_MODE, key.getPublic(), param);
        return acipher.doFinal(plainText);
    }

    public byte[] decrypt(byte[] cipherText) throws Exception {
        // get ECIES cipher objects
        Cipher bcipher = Cipher.getInstance("ECIES");
        //  generate derivation and encoding vectors
        byte[]  d = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
        byte[]  e = new byte[] { 8, 7, 6, 5, 4, 3, 2, 1 };
        IESParameterSpec param = new IESParameterSpec(d, e, 256);
        bcipher.init(Cipher.DECRYPT_MODE,privateKey, param);
        return bcipher.doFinal(cipherText);
    }
}