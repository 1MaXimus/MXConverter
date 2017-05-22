package com.example.maxim.mxconverter;

public class Curren {

    public  String ncode;
    public  String scode;
    public  String name;
    public  float value;

    public Curren(String nc, String sc, String na, float va){
        this.ncode = nc;
        this.scode = sc;
        this.name = na;
        this.value = va;
    }
    public Curren (){
        this.ncode = "";
        this.scode = "";
        this.name = "";
        this.value = 0;
    }

}
