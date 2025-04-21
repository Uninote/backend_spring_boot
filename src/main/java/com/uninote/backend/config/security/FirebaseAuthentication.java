package com.uninote.backend.config.security;

import com.google.firebase.auth.FirebaseToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

public class FirebaseAuthentication extends AbstractAuthenticationToken {

    private final FirebaseToken firebaseToken;
    private final String uid;
    private final String email;

    public FirebaseAuthentication(FirebaseToken firebaseToken) {
        super(Collections.emptyList()); 
        this.firebaseToken = firebaseToken;
        this.uid = firebaseToken.getUid();  
        this.email = firebaseToken.getEmail();  
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return uid;
    }

    @Override
    public Object getPrincipal() {
        return uid; 
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public FirebaseToken getFirebaseToken() {
        return firebaseToken;
    }
}
