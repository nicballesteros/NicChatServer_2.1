package com.nicballesteros.server.generatekeys;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class GenerateKeys {

    private KeyPairGenerator keyGen;
    private KeyPair pair;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public GenerateKeys(int keyLength) throws NoSuchAlgorithmException, NoSuchProviderException {
        this.keyGen = KeyPairGenerator.getInstance("RSA");
        this.keyGen.initialize(keyLength);
    }

    public void createKeys(){
        this.pair = this.keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public PrivateKey getPrivateKey(){
        return this.privateKey;
    }

    public PublicKey getPublicKey(){
        return this.publicKey;
    }

    public void writeToFile(String path, byte[] key) throws IOException{
        File f = new File(path);
        f.getParentFile().mkdirs();

        FileOutputStream fos = new FileOutputStream(f); //eventually we will have this in a database quite possibly but this solution works as well.
        fos.write(key);
        fos.flush();
        fos.close();
    }

    public static void main(String[] args) {
        GenerateKeys gk;
        try{
            gk = new GenerateKeys(1024);
            gk.createKeys();
            gk.writeToFile("Keypair/publicKey", gk.getPublicKey().getEncoded());
            gk.writeToFile("Keypair/privateKey", gk.getPrivateKey().getEncoded());
        }
        catch(IOException i){
            System.err.println(i.getMessage());
        }
        catch(NoSuchAlgorithmException | NoSuchProviderException e){
            System.err.println(e.getMessage());
        }
    }
}