package com.example.changoo.fishing.model;

/**
 * Created by changoo on 2017-03-05.
 */

public class Notice {
    String timestr;
    String msg;

    public Notice(String timestr, String msg) {
        this.timestr = timestr;
        this.msg = msg;
    }

    public String getTimestr() {
        return timestr;
    }

    public void setTimestr(String timestr) {
        this.timestr = timestr;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
