package com.secot.thiagow.secot;

/**
 * Created by ThiagoW on 2015-04-23.
 */
public class Inscrito {

    private String id;
    private String name;
    private String qrid;
    private String presente;

    public Inscrito(String id, String name, String qrid, String presente){
        this.id = id;
        this.name = name;
        this.qrid = qrid;
        this.presente = presente;
    }

    public String getID(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getQRID(){
        return this.qrid;
    }

    public String getPresente(){
        return this.presente;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
