package com.pf.warehouse.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class GoogleTokenService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenService(GoogleIdTokenVerifier verifier) {
        this.verifier = verifier;
    }

    public GoogleIdToken.Payload verify(String credential) {

        try {
            GoogleIdToken idToken = verifier.verify(credential);

            if (idToken == null) {
                throw new BadCredentialsException(
                        "Invalid Google credential");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new BadCredentialsException(
                        "Google email is not verified");
            }

            return payload;

        } catch (GeneralSecurityException | IOException ex) {
            throw new BadCredentialsException(
                    "Unable to verify Google credential", ex);
        }
    }
}