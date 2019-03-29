package com.nicballesteros.server.one;

public class Message {
    private int to;
    private int from;
    private String msg;

    public Message(int from, int to, String msg){
        this.from = from;
        this.to = to;
        this.msg = msg;
    }

    //make getters and setters
    public String getMessage(){
        return "~m|`" + this.msg;
    }

}
