package com.stackbase.mobapp.objects;

public class BorrowerData extends JSONObj {
    private int datumId;
    private String datumName;

    public int getDatumId() {
        return datumId;
    }

    public void setDatumId(int datumId) {
        this.datumId = datumId;
    }

    public String getDatumName() {
        return datumName;
    }

    public void setDatumName(String datumName) {
        this.datumName = datumName;
    }

}
