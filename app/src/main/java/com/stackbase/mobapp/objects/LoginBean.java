package com.stackbase.mobapp.objects;

public class LoginBean extends JSONObj {
    String encryptpwd = "";
    long id = 0;
    String tip = "";
    private boolean retCode = true;

    public LoginBean(String jsonStr) {
        this.fromJSONStr(jsonStr);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEncryptpwd() {
        return encryptpwd;
    }

    public void setEncryptpwd(String encryptpwd) {
        this.encryptpwd = encryptpwd;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public boolean getRetCode() {
        return retCode;
    }

    public void setRetCode(boolean retCode) {
        this.retCode = retCode;
    }
}