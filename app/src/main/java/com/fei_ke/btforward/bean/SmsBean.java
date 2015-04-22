package com.fei_ke.btforward.bean;

/**
 * Created by 杨金阳 on 2015/4/19.
 */
public class SmsBean {
    private String from;
    private String body;

    public SmsBean(String from, String body) {
        this.from = from;
        this.body = body;
    }

    public SmsBean() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "SmsBean{" +
                "from='" + from + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
