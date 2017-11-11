package com.jonghyeon.tutorial;

/**
 * Created by merturl on 2017-11-12.
 */

public class Memo {

    private String key;
    private String txt;
    private String title;
    private long createDate, updateDate;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        if(txt != null){
            if(txt.indexOf("\n") > -1){
                return txt.substring(0, txt.indexOf("\n"));
            }else{
                return txt;
            }
        }
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }


}
