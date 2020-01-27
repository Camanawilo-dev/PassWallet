package com.ecb825.passwordwalletv2;

public class authPreferences {
    private String passHash;
    private String authMode;

    public authPreferences(String pas,String auth){
        this.authMode = auth;
        this.passHash = pas;

    }

    public String getPassHash() {
        return passHash;
    }

    public void setPassHash(String passHash) {
        this.passHash = passHash;
    }

    public String getAuthMode() {
        return authMode;
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }



}
