package com.nicballesteros.server.one;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private String hashedPass;

    private List<Integer> acquaintedClients;

    //TODO change the constructor so that it accepts an InetAddress instead of string

    public ServerClient(int ID, String name, String hashedPass){
        this.ID = ID;
        this.name = name;
        this.isConnected = false;
        this.recipient = -1;
        this.receivedUsername = "";
        this.receivedPassword = "";
        this.hashedPass = hashedPass;
        this.acquaintedClients = new ArrayList<>();
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
        this.receivedUsername = "";
        this.receivedPassword = "";
        this.acquaintedClients = new ArrayList<>();
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

    public String getHashedPass(){
        return hashedPass;
    }

    public int getRecipient(){
        return this.recipient;
    }

    public boolean getIsConnected() {
        return this.isConnected;
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

    public void setAddress(InetAddress ipAddress){
        this.ipAddress = ipAddress;
    }

    public void setPort(int port){
        this.port = port;
    }

    public void setIsConnected(boolean isConnected){
        this.isConnected = isConnected;
    }

    public void disconnectClient(){
        this.ipAddress = null;
        this.port = -1;
        this.isConnected = false;
        this.receivedPassword = null;
        this.receivedUsername = null;
        this.recipient = -1;
        this.key = null;
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

    public void setName(){
        this.name = this.receivedUsername;
    }

    public byte[] encryptByteAES(byte[] in){
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
        if(!this.receivedPassword.equals("") && !this.receivedUsername.equals("")){
            return true;
        }
        return false;
    }

    public void setAESkey(byte[] data){
        AESkey = new SecretKeySpec(data, "AES");
    }

    public void setHashedPass(String hashedPass) {
        this.hashedPass = hashedPass;
    }

    public List<Integer> getAcquainedClients(){
        return acquaintedClients;
    }

    public void addAcquaintance(int idOfClient){
        acquaintedClients.add(idOfClient);
    }

    public boolean doesAcquaintanceAlreadyExist(int id){
        for(int a : acquaintedClients){
            if(a == id){
                return true;
            }
        }
        return false;
    }
}
