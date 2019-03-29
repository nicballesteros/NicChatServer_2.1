package com.nicballesteros.server.one;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.PublicKey;
import org.apache.commons.codec.binary.Base64;


public class ServerClient {
    private String name;
    private InetAddress ipAddress;
    private int port;

    private final int ID;
    private boolean isConnected;
    private final int attempt = 0;
    private int recipient;

    private PublicKey publickey;

    private SecretKeySpec AESkey;
    private byte[] key;

    private String receivedUsername;
    private String receivedPassword;

    //TODO change the constructor so that it accepts an InetAddress instead of string

    public ServerClient(int ID, String name){
        this.ID = ID;
        this.name = name;
    }

    public ServerClient(int ID, String name, String ipAddress, int port, boolean isConnected){
        this.name = name;
        try {
            this.ipAddress = InetAddress.getByName(ipAddress);
        }
        catch(UnknownHostException u){
            System.out.println("UnknownHostException: " + u);
        }
        this.port = port;
        this.ID = ID;
        this.isConnected = isConnected;
        this.recipient = -1;
        this.receivedUsername = null;
        this.receivedPassword = null;
    }

    public int getID(){
        return this.ID;
    }

    public String getName(){
        return this.name;
    }

    public InetAddress getAddress() {
        return this.ipAddress;
    }

    public int getPort(){
        return this.port;
    }

    public PublicKey getPublickey() {
        return this.publickey;
    }

    public int getRecipient(){
        return this.recipient;
    }

    public boolean getIsConnected() {
        return this.isConnected;
    }

    public void setPublickey(PublicKey publickey) {
        this.publickey = publickey;
    }

    public void setRecipient(int recipient) {
        this.recipient = recipient;
    }


    public void setReceivedUsername(String username){
        this.receivedUsername = username;
    }

    public void setReceivedPassword(String password){
        this.receivedPassword = password;
    }

    public String getReceivedUsername(){
        return this.receivedUsername;
    }

    public String getReceivedPassword(){
        return this.receivedPassword;
    }

    public SecretKeySpec getAESkey(){
        return this.AESkey;
    }

    public byte[] decryptByteAES(byte[] in){
        try{
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, this.AESkey);
            return cipher.doFinal(in);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public byte[] encrypteByteAES(byte[] in){
        try{
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, AESkey);
            return cipher.doFinal(in);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean enteredCreds(){
        if(this.receivedPassword != null && this.receivedUsername != null){
            return true;
        }
        return false;
    }

    public void setAESkey(byte[] data){
        AESkey = new SecretKeySpec(data, "AES");
    }
}
