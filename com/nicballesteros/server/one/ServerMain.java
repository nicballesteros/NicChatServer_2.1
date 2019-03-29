package com.nicballesteros.server.one;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {
    private List<ServerClient> clients = new ArrayList();
    private int port;
    private Server server;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public ServerMain(int port){
        generateKeys();
        this.port = port;
        int nextID = -2500;
        try {
            nextID = loadDatabase();
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("NextID is gonna be jacked up");
        }

        //testEncryption();

        this.server = new Server(port, clients, publicKey, privateKey, nextID);
    }

    public static void main(String[] args) {
        if(args.length > 1){
            System.out.print("Usage: java -jar ServerMain.jar [port]");
        }
        else{
            int port = Integer.parseInt(args[0]);
            new ServerMain(port);
        }
    }

    private int loadDatabase() throws Exception{
        String url = "jdbc:mysql://localhost:3306/clients?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(url, "javaserver"/*"serveraccess"*/, "u4tOEoxL"/*"@mKkubjMYbc96RVMR"*/); //u4tOEoxL
        Statement stmt = conn.createStatement();

        String strSelect = "select id, name from clientuserandpass";

        ResultSet rset = stmt.executeQuery(strSelect);

        int rowCount = 0;
        while(rset.next()){

            int id = rset.getInt("id");
            String name = rset.getString("name");

            ServerClient c = new ServerClient(id, name);
            clients.add(c);
            ++rowCount;
        }
        System.out.println("rows: " + rowCount);
        return rowCount + 1;
    }

    private void generateKeys() {
        KeyPairGenerator keygen;
        try {
            keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(1024);

            KeyPair pair = keygen.generateKeyPair();
            this.privateKey = pair.getPrivate();
            this.publicKey = pair.getPublic();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}
