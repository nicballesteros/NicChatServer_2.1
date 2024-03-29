package com.nicballesteros.server.one;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

public class Server implements Runnable{

    private List<ServerClient> clients = new ArrayList();
    private DatagramSocket socket;
    private int port;
    private boolean running = false;
    private int nextID;

    private Thread run;
    private Thread manage;
    private Thread send;
    private Thread receive;
    private Thread authenticate;
    private String recipient;

    private PublicKey serverPublicKey;
    private PrivateKey serverPrivateKey;

    private List<ServerClient> usersLoggingOn;

    public Server(int port, List<ServerClient> clients, PublicKey serverPublicKey, PrivateKey serverPrivateKey, int nextID){
        this.usersLoggingOn = new ArrayList<>();
        this.clients = clients;
        this.port = port;

        this.nextID = nextID;

        this.serverPrivateKey = serverPrivateKey;
        this.serverPublicKey = serverPublicKey;

        try{
            this.socket = new DatagramSocket(port);
        }
        catch(SocketException se){
            System.out.println("SocketException: " + se);
            return;
        }

        this.run = new Thread(this, "Server");
        this.run.start();
    }

    @Override
    public void run(){
        this.running = true;
        System.out.println("Server started on port " + this.port);
        this.manageClients();
        this.receive();
    }

    private void manageClients(){
        this.manage = new Thread("Manage") {
            public void run(){
                while(running){

                }
            }
        };
        this.manage.start();
    }

    private void disconnectNewUser(ServerClient client){
        socket.close();
        running = false;
        usersLoggingOn.remove(client);
        client.disconnectClient();
    }

    private void disconnectRegisteredUser(ServerClient client){
        socket.close();
        running = false;
        client.disconnectClient();
    }

//    private void saveMessageToDatabase(String msg, ServerClient from, ServerClient to){
//        try(Connection conn = DriverManager.getConnection(
//                "jdbc:mysql://localhost:3306/clients?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
//                "javaserver",
//                "u4tOEoxL");
//            Statement stmt = conn.createStatement()){
//            String strSelect = "insert into messages values (" + from.getID() + "," + to.getID() + "," + msg + ")";
//
//            stmt.executeUpdate(strSelect);
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
//    }

    private void saveNewClientCredsToDatabase(int id, String username, String password){
        String url = "jdbc:mysql://localhost:3306/clients?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
        String mysqlUsername = "javaserver";
        String mysqlPassword = "u4tOEoxL";

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            //System.out.println("Connecting to clients database");
            Connection conn = DriverManager.getConnection(url, mysqlUsername, mysqlPassword);
            System.out.println("Connected to clients database successfully");


            //System.out.println("Inserting new user into username and password table");
            Statement stmt = conn.createStatement();

            String sql = "INSERT INTO clientuserandpass VALUES (" + id + ", '" + username + "', '" + password + "');";
            System.out.println(sql);
            stmt.executeUpdate(sql);
            System.out.println("Inserted Data into table");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void createAcquaintanceTable(ServerClient client){
        String url = "jdbc:mysql://localhost:3306/clients?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
        String mysqlUsername = "javaserver";
        String mysqlPassword = "u4tOEoxL";

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            //System.out.println("Connecting to clients database");
            Connection conn = DriverManager.getConnection(url, mysqlUsername, mysqlPassword);
            System.out.println("Connected to clients database successfully");


            //System.out.println("Inserting new user into username and password table");
            Statement stmt = conn.createStatement();

            String sql = "CREATE TABLE " + client.getName() + " (id int);";
            stmt.executeUpdate(sql);
            System.out.println("table created");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

//    private String getStoredHashedPassword(ServerClient client){
//        String url = "jdbc:mysql://localhost:3306/clients?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
//        String mysqlUsername = "javaserver";
//        String mysqlPassword = "u4tOEoxL";
//
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            Connection conn = DriverManager.getConnection(url, "javaserver"/*"serveraccess"*/, "u4tOEoxL"/*"@mKkubjMYbc96RVMR"*/); //u4tOEoxL
//            Statement stmt = conn.createStatement();
//
//            String strSelect = "select pass from clientuserandpass where id = " + client.getID() + ";";
//
//            ResultSet rset = stmt.executeQuery(strSelect);
//
//            if(rset.next()) {
//                return rset.getString("pass");
//            }
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }

    //    private void loadOldMessagesFromDatabase(int from, int to){
//        from = clients.get(from).getID();
//        to = clients.get(to).getID();
//
//        String url = "jdbc:mysql://localhost:3306/clients?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
//        String mysqlUsername = "javaserver";
//        String mysqlPassword = "u4tOEoxL";
//
//        try{
//            Connection conn = DriverManager.getConnection(url, mysqlUsername, mysqlPassword);
//
//            Statement stmt = conn.createStatement();
//
//            String sqlCommand = "select fromid, toid, message from messages";
//
//            ResultSet rset = stmt.executeQuery(sqlCommand);
//
//            List<Message> messagesInvolvingRecipientAndClient = new ArrayList<>();
//
//            int rowCount = 0;
//            while(rset.next()){
//                int fromid = rset.getInt("fromid");
//                int toid = rset.getInt("toid");
//                String msg = rset.getString("message");
//
//                if((fromid == from && toid == to) || (fromid == to && toid == from)){
//                    messagesInvolvingRecipientAndClient.add(new Message(fromid, toid, msg));
//                }
//                ++rowCount;
//            }
//
//            for(int i = 0; i < messagesInvolvingRecipientAndClient.size(); i++){
//                send(messagesInvolvingRecipientAndClient.get(i).getMessage().getBytes(), clients.get(from).getAddress(), clients.get(from).getPort());
//            }
//        }
//        catch(SQLException sql){
//            sql.printStackTrace();
//        }
//
//    }

    /*That's a tool that will help us later*/
    private void showByteDataOnConsole(byte[] data){
        System.out.print("{");
        for(byte dat : data){
            int num = dat;
            System.out.print(num + ", ");
        }
        System.out.println("}");
    }

    private int findClient(String name){
        /**
         * Returns Index of the client
         * Returns -1 if the client does not exist
         */

        for(int i = 0; i < clients.size(); i++){
            if(clients.get(i).getName().equals(name)){
                return i;
            }
        }

        return -1;
    }

    private void receive(){
        this.receive = new Thread("Receive") {
            public void run(){
                DatagramPacket packet;
                while(running){
                    byte[] data = new byte[1024];
                    packet = new DatagramPacket(data, data.length);

                    try{
                        socket.receive(packet);
                        //showByteDataOnConsole(packet.getData());
                        whichFormOfEncryption(packet);
                    }
                    catch (IOException io){
                        System.out.println("ln66:server.java IOException: " + io);
                    }
                }
            }
        };
        this.receive.start();
    }

    private void whichFormOfEncryption(DatagramPacket packet){
        byte[] data = packet.getData();
        byte encryptionType = data[0];
        //Cuts off the trailing zeros at the end. The byte[] will have a 75 at the end of the [] to signify the end and where to cut off the line
        int cutOff = 0;
        for(int j = data.length - 1; j > 0; j--){
            if(data[j] == (byte)75){
                cutOff = j;
                break;
            }
        }
        data = Arrays.copyOfRange(data, 0, cutOff);


        if(encryptionType == (byte)100){ //unencrypted
            data = Arrays.copyOfRange(data, 1, data.length);
            byte userType = data[0];

            //System.out.println("No Encryption");

            if(userType == (byte)100){//new user unencrypted
                usersLoggingOn.add(new ServerClient(nextID,"", packet.getAddress().getHostAddress() ,packet.getPort(),false));

                this.sendPublicKey(nextID, packet.getAddress(), packet.getPort());

                nextID++;
            }
            else if(userType == (byte)101){ //old user unencrypted
                data = Arrays.copyOfRange(data, 1, data.length);

                String name = new String(data);

                int index = findClient(name);
                if(index != -1){
                    ServerClient client = clients.get(index);
                    sendPublicKey(client.getID(), packet.getAddress(), packet.getPort());
                    client.setAddress(packet.getAddress());
                    client.setPort(packet.getPort());
                    usersLoggingOn.add(client);
                }
                else{
                    System.out.println("User does not exist");
                    sendErrorSignal(packet.getAddress(), packet.getPort());
                }
            }
            else{
                System.out.println("error not encrypted");
            }
        }
        else { //encrypted
            //System.out.println("Encrypted");
            byte[] idBytes = Arrays.copyOfRange(data, 1, 5);
            data = Arrays.copyOfRange(data, 5, data.length);

            ByteBuffer wrapped = ByteBuffer.wrap(idBytes);
            int id = wrapped.getInt();

            //TODO check this cause it may send off a -1 and crash the program if user sends an id thats doesnt make sense
            int clientIndex = -1;
            for(int i = 0; i < clients.size(); i++){
                if(clients.get(i).getID() == id){
                    clientIndex = i;
                    clients.get(i).setAddress(packet.getAddress());
                    clients.get(i).setPort(packet.getPort());
                    break;
                }
            }
            //TODO delete from the usersLoggingOn Array

            if(clientIndex == -1){
                for(int i = 0; i < usersLoggingOn.size(); i++){
                    if(usersLoggingOn.get(i).getID() == id){
                        clientIndex = i;
                        break;
                    }
                }
                if(clientIndex != -1){
                    pickEncryption(data, usersLoggingOn.get(clientIndex), encryptionType);
                }
            }
            else{
                pickEncryption(data, clients.get(clientIndex), encryptionType);
            }
        }
    }

    private void pickEncryption(byte[] data, ServerClient currentClient, byte encryptionType){
        if (encryptionType == (byte) 101) { //rsa
            //System.out.println("RSA");

            byte[] out = decryptByteRSA(data);

            process(out, currentClient);
        }
        else if (encryptionType == (byte) 102) { //aes
            //System.out.println("AES");

            data = currentClient.decryptByteAES(data);

            process(data, currentClient);
        } else {
            System.out.println("Packet does not contain data from client");
        }
    }

    //TODO make the name equal the username in the server client class when everything is cool

    private void process(byte[] in, ServerClient currentClient){
        byte userType = in[0];
        in = Arrays.copyOfRange(in, 1, in.length); //cut off the usertype byte

        if(userType == (byte)100){  //NEW USER
            handleNewUser(in, currentClient);
        }
        else if (userType == (byte)101){ //REGISTERED USER
            handleRegisteredUser(in, currentClient);
        }
        else{
            System.out.println("Error in process");
        }
    }

    private void handleNewUser(byte[] in, ServerClient currentClient){
        System.out.println("New User");
        byte dataType = in[0];
        in = Arrays.copyOfRange(in, 1, in.length); //cut off the data type byte
        if(dataType == (byte)100){ //if data is a username
            String username = new String(in);
            System.out.println("Username: " + username);
            currentClient.setReceivedUsername(username);
        }
        else if(dataType == (byte)101){ //if data is a password hash
            String password = Base64.encodeBase64String(in);
            System.out.println("Password Hash");
            currentClient.setReceivedPassword(password);
            currentClient.setHashedPass(password);
        }
        else if(dataType == (byte)102){ //data is an AES key
            System.out.println("Set AES key");
            currentClient.setAESkey(in); //TODO make this func bool so we can check if the aes key was good or nah and then send a 25 signal
            sendConfirmationCode(12345, currentClient);//12345 is the confirmation code telling the user that it has received the AES key
        }
        else{ //some error
            System.out.println("Error in handleNewUser");
        }

        if(currentClient.enteredCreds()){
            //send to the database and add to the clients array
            int id = doesClientExist(currentClient.getReceivedUsername());
            System.out.println(id);
            if(id == -1){
                saveNewClientCredsToDatabase(currentClient.getID(), currentClient.getReceivedUsername(), currentClient.getReceivedPassword());

                System.out.println("Sent to the database");
                sendConfirmationCode(54321, currentClient); //54321 signals the user was made
                usersLoggingOn.remove(currentClient);
                clients.add(currentClient);
                currentClient.setName();
                createAcquaintanceTable(currentClient);
            }
            else{
                //TODO send an error msg saying the user already exists
            }

            //disconnectNewUser(currentClient);
        }
    }

    private void handleRegisteredUser(byte[] in, ServerClient currentClient){
        //System.out.println("Registered User");

        byte dataType = in[0];
        in = Arrays.copyOfRange(in, 1, in.length); //cut off the data type byte

         //client is not connected
        if(dataType == (byte)100){ //data is a username
            String username = new String(in);
            //System.out.println("Username: " + username);
            //TODO check for name clashes. If the name clashes then we need to send an error message
            currentClient.setReceivedUsername(username);
            if(checkCreds(currentClient)){
                usersLoggingOn.remove(currentClient);
            }
        }
        else if(dataType == (byte)101){ //data is a password hash
            String password = Base64.encodeBase64String(in);
            if(password.equals(currentClient.getHashedPass())){
                //System.out.print("Password Hash: ");
                //System.out.println(password);

                currentClient.setReceivedPassword(password);

                sendConfirmationCode(34251, currentClient);
                if(checkCreds(currentClient)){
                    usersLoggingOn.remove(currentClient);
                }
            }
            else{
               // System.out.println("falseAlarm");
            }
        }
        else if(dataType == (byte)102){ //data is an aes key
            currentClient.setAESkey(in); //TODO check this is valid
            sendConfirmationCode(24242, currentClient);
        }
        //CONNECTED FROM THIS POINT ON
        else if(dataType == (byte)103 && currentClient.getIsConnected()){ //data is a recipient
            ByteBuffer wrapped = ByteBuffer.wrap(in);

            int thisID = wrapped.getInt();

            if(doesIDExist(thisID) != null){
                currentClient.setRecipient(thisID); //in should be 4 bytes
            }
        }
        else if(dataType == (byte)104 && currentClient.getIsConnected()){ //data is a message
            //if has recipient
            if(currentClient.getRecipient() != -1 && currentClient.getRecipient() != currentClient.getID() && clients.get(currentClient.getRecipient() - 1).getIsConnected()){
                ServerClient recipient = clients.get(currentClient.getRecipient() - 1);

                sendSenderNameToUser(recipient, currentClient);
                sendToUser(in, recipient, currentClient);
            }
        }
        else if(dataType == (byte)105 && currentClient.getIsConnected()){ //data is a request for history of a certain acquaintance

        }
        else if(dataType == (byte)106 && currentClient.getIsConnected()){ //data is a request for the a list of acquainted clients
            sendAcquaintedClients(currentClient);
            System.out.println("Requesting acquai");
        }
        else if(dataType == (byte)107 && currentClient.getIsConnected()){
            sendConfirmationCode(6789, currentClient);
        }
        else if(dataType == (byte)108 && currentClient.getIsConnected()){ //Disconnect the user
            System.out.println(currentClient.getName() + " disconnected");
            currentClient.setIsConnected(false);
            sendConfirmationCode(9876, currentClient);
        }
        //DEALING WITH ADDING AN ACQUAINTANCE
        else if(dataType == (byte)41){ //check if the client exists and give a confirmation code
            int id = doesClientExist(new String(in));
            if(id != -1 && !currentClient.doesAcquaintanceAlreadyExist(id)){
                sendID(id, clients.get(id - 1).getName(), currentClient);
                currentClient.addAcquaintance(id);
                addAquaintanceInDatabase(currentClient.getName(), id);
            }
            else{
                sendConfirmationCode(2565, currentClient);
            }
        }
        else{
            System.out.println("Error in handleRegisteredUser");
            sendErrorSignal(currentClient.getAddress(), currentClient.getPort());
        }

    }

    private boolean checkCreds(ServerClient client){
        if(client.getReceivedPassword().equals(client.getHashedPass()) && client.getReceivedUsername().equals(client.getName())){
            //System.out.println(client.getReceivedPassword());
            sendConfirmationCode(55555, client); //confirmation code saying to proceed to the next slide
            System.out.println(client.getName() + " is now online");
            client.setIsConnected(true);
            return true;
        }
        return false;
    }

    private int addAquaintanceInDatabase(String name, int id){
        String url = "jdbc:mysql://localhost:3306/clients?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
        String mysqlUsername = "javaserver";
        String mysqlPassword = "u4tOEoxL";

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, mysqlUsername, mysqlPassword);
            System.out.println("Connected to clients database successfully");

            Statement stmt = conn.createStatement();

            String sql = "INSERT INTO " + name + " values(" + id + ");";

            //System.out.println(sql);
            stmt.executeUpdate(sql);
            System.out.println("Inserted Data into table");
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return -1;
    }

    private int doesClientExist(String name){
        String url = "jdbc:mysql://localhost:3306/clients?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
        String mysqlUsername = "javaserver";
        String mysqlPassword = "u4tOEoxL";

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, mysqlUsername, mysqlPassword);
            System.out.println("Connected to clients database successfully");

            Statement stmt = conn.createStatement();

            String sql = "SELECT id FROM clientuserandpass WHERE name = '" + name + "'";

            ResultSet rset = stmt.executeQuery(sql);
            if(!rset.next()){
                return -1;
            }
            else{

                int id = rset.getInt("id");

                return id;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return -1;
    }

    private void disconnect(){
        //ServerClient c = null;

        for(int i = 0; i < clients.size(); i++){

        }
    }

    private byte[] decryptByteRSA(byte[] data){
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, serverPrivateKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private void send(final byte[] data, final InetAddress address, final int port){
        send = new Thread("Send"){
            public void run(){
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                try{
                    output.write(data);
                    output.write((byte)75);
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                byte[] out = output.toByteArray();

                DatagramPacket packet = new DatagramPacket(out, out.length, address, port);

                try{
                    socket.send(packet);
                }
                catch(IOException io){
                    System.out.println("ln110 server.java IOException: " + io);
                }
            }
        };
        send.start();
    }

    private void sendToUser(byte[] message, ServerClient recipient, ServerClient from){
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream firstOut = new ByteArrayOutputStream();

        byte[] byteArray = ByteBuffer.allocate(4).putInt(11111).array();
        try{
            firstOut.write(byteArray);
            firstOut.write(message);

            output.write((byte)101);
            output.write(recipient.encryptByteAES(firstOut.toByteArray()));
        }
        catch (Exception e){
            e.printStackTrace();
        }

        send(output.toByteArray(), recipient.getAddress(), recipient.getPort());
    }

    private void sendSenderNameToUser(ServerClient recipient, ServerClient from){
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream firstOut = new ByteArrayOutputStream();

        byte[] byteArray = ByteBuffer.allocate(4).putInt(22222).array();
        try{
            firstOut.write(byteArray);
            firstOut.write(from.getName().getBytes());

            output.write((byte)101);
            output.write(recipient.encryptByteAES(firstOut.toByteArray()));
        }
        catch (Exception e){
            e.printStackTrace();
        }

        send(output.toByteArray(), recipient.getAddress(), recipient.getPort());
    }

    private void sendPublicKey(int id, InetAddress address, int port){
        byte[] keyToSendToClient = serverPublicKey.getEncoded();
        byte identifier = (byte)100;
        byte[] idInByteArray = ByteBuffer.allocate(4).putInt(id).array();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            output.write(identifier);
            output.write(idInByteArray);
            output.write(keyToSendToClient);
        }
        catch(IOException e){
            e.printStackTrace();
        }

        send(output.toByteArray(), address, port);
    }

    private void sendErrorSignal(InetAddress address, int port){
        byte[] out = {(byte)25};
        send(out, address, port);
    }

    private void sendConfirmationCode(int code, ServerClient client){
        byte[] byteArray = ByteBuffer.allocate(4).putInt(code).array(); //12345 is the confirmation code telling the user that it has received the AES key
        byteArray = client.encryptByteAES(byteArray);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try{
            output.write((byte)101);
            output.write(byteArray);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        send(output.toByteArray(), client.getAddress(), client.getPort());
    }

    private String doesIDExist(int id){
        String url = "jdbc:mysql://localhost:3306/clients?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
        String mysqlUsername = "javaserver";
        String mysqlPassword = "u4tOEoxL";

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, mysqlUsername, mysqlPassword);
            System.out.println("Connected to clients database successfully");

            Statement stmt = conn.createStatement();

            String sql = "SELECT name FROM clientuserandpass WHERE id = " + id + ";";

            ResultSet rset = stmt.executeQuery(sql);
            if(rset.next()){
                return rset.getString("name");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    private void sendAcquaintedClients(ServerClient client){
        List<Integer> aquaintances = client.getAcquainedClients();

        for(Integer acquaintance : aquaintances){
            System.out.println(acquaintance);
            ByteArrayOutputStream arrayBuilder = new ByteArrayOutputStream();
            ByteArrayOutputStream arrayBuilderEncrypt = new ByteArrayOutputStream();

            int num = acquaintance;
            byte[] intBytes = ByteBuffer.allocate(4).putInt(num).array();
            byte[] code = ByteBuffer.allocate(4).putInt(7777).array();
            String name;
            if(clients.get(num - 1).getID() == num){
                name = clients.get(num - 1).getName();
            }
            else{
                name = doesIDExist(num);
                if(name == null){
                    name = " ";
                    System.out.println("Trying to send aquaintance of user that dont exist");
                }
            }
            try{
                arrayBuilderEncrypt.write((byte)101);

                arrayBuilder.write(code);
                arrayBuilder.write(intBytes);
                arrayBuilder.write(name.getBytes());

                arrayBuilderEncrypt.write(client.encryptByteAES(arrayBuilder.toByteArray()));

                send(arrayBuilderEncrypt.toByteArray(), client.getAddress(), client.getPort());
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        //label
        //encrypt
    }

    private void sendID(int id, String name, ServerClient client){
        byte[] byteArray = ByteBuffer.allocate(4).putInt(999).array(); //12345 is the confirmation code telling the user that it has received the AES key
        byte[] idToByteArray = ByteBuffer.allocate(4).putInt(id).array(); //12345 is the confirmation code telling the user that it has received the AES key
        ByteArrayOutputStream firstOut = new ByteArrayOutputStream();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try{
            firstOut.write(byteArray);
            firstOut.write(idToByteArray);
            firstOut.write(name.getBytes());

            output.write((byte)101);
            output.write(client.encryptByteAES(firstOut.toByteArray()));
        }
        catch (Exception e){
            e.printStackTrace();
        }

        send(output.toByteArray(), client.getAddress(), client.getPort());
    }
}

