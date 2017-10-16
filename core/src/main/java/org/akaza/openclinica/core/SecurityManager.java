/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.core;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author Krikor Krumlian
 *
 */
public class SecurityManager {

    private PasswordEncoder encoder;
    private SaltSource saltSource;

    private AuthenticationProvider providers[];

    /**
     * Generates a random password with default length
     *
     */
    public String genPassword() {
        return genPassword(8);
    }

    /**
     * Generates a random password by length
     *
     * @param howmany
     */
    public String genPassword(int howmany) {

        String ret = "";
        String core = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rand = new Random();

        for (int i = 0; i < howmany; i++) {
            int thisOne = rand.nextInt(core.length());
            char thisOne2 = core.charAt(thisOne);
            ret += thisOne2;
        }

        return ret;
    }

    public String encrytPassword(String password, UserDetails userDetails) throws NoSuchAlgorithmException {
        Object salt = null;

        if (this.saltSource != null) {
            salt = this.saltSource.getSalt(userDetails);
        }
        return encoder.encodePassword(password, salt);
    }

    /**
     * @deprecated Use {@link #verifyPassword(String, UserDetails)} instead.
     * @param encPass
     * @param rawPass
     * @param userDetails
     * @return
     */
    @Deprecated
    public boolean isPasswordValid(String encPass, String rawPass, UserDetails userDetails) {
        Object salt = null;

        if (this.saltSource != null) {
            salt = this.saltSource.getSalt(userDetails);
        }

        return encoder.isPasswordValid(encPass, rawPass, salt);
    }

    public boolean verifyPassword(String clearTextPassword, UserDetails userDetails) {

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
                clearTextPassword);

        for (AuthenticationProvider p : providers) {
            try {
                p.authenticate(authentication);
                return true;
            } catch (AuthenticationException e) {
                // Nothing to do
            }

        }

        return false;
    }

    public PasswordEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public SaltSource getSaltSource() {
        return saltSource;
    }

    public void setSaltSource(SaltSource saltSource) {
        this.saltSource = saltSource;
    }

    public AuthenticationProvider[] getProviders() {
        return providers;
    }

    public void setProviders(AuthenticationProvider[] providers) {
        this.providers = providers;
    }

}