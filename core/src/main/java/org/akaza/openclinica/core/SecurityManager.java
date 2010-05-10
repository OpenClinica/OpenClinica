/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.core;

import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * 
 * @author Krikor Krumlian
 * 
 */
public class SecurityManager {

    private PasswordEncoder encoder;
    private SaltSource saltSource;

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

    public boolean isPasswordValid(String encPass, String rawPass, UserDetails userDetails) {
        Object salt = null;

        if (this.saltSource != null) {
            salt = this.saltSource.getSalt(userDetails);
        }

        return encoder.isPasswordValid(encPass, rawPass, salt);
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

}