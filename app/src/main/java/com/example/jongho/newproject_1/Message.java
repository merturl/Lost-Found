package com.example.jongho.newproject_1;

/**
 * Created by woong on 2017-12-11.
 */

public class Message {
    private String to;
    private String from;
    private String msg;
    private boolean send;

    public Message() {

    }

    public Message(String to, String from, String msg) {
        this.to =to;
        this.from=from;
        this.msg =msg;
        this.send = false;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSend() {
        return send;
    }

    public void setSend(boolean send) {
        this.send = send;
    }

}


