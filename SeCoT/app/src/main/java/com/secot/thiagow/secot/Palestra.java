package com.secot.thiagow.secot;

/**
 * Created by ThiagoW on 2015-04-23.
 */
public class Palestra {
    private String id;
    private String name;
    private String speaker;

    public Palestra(String id, String name, String speaker){
        this.id = id;
        this.name = name;
        this.speaker = speaker;
        System.out.println(this.id);
    }

    public String getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getSpeaker(){
        return this.speaker;
    }

    @Override
    public String toString() {
        return this.getSpeaker() + ": " + this.getName();
    }
}
