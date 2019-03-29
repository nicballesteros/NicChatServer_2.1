package com.nicballesteros.server.one;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

public class Encrypt {
    private Cipher cipher;
    private PrivateKey privateKey;

    public Encrypt(String recipient){
        //open database and see if there is a client online and if they have a public key stored with their name
        try {
            this.cipher = Cipher.getInstance("RSA");
        }
        catch(NoSuchAlgorithmException | NoSuchPaddingException i){
            i.printStackTrace();
        }



    }
}
