package com.ecb825.passwordwalletv2;

public class items {
    private int id;
    private String line1;
    private String line2;
    private String line3;
    private String pass;


    public items(int id,String l1,String l2, String l3,String ps){
        this.id = id;
        line1 = l1;
        line2=l2;
        line3=l3;
        pass = ps;
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getLine3() {
        return line3;
    }

    public void setLine3(String line3) {
        this.line3 = line3;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public boolean equals(items inlist,items toadd){
        if(inlist.getId() == toadd.getId()){
            return true;
        }else {
            return false;
        }
    }



}
