package main.User;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class Password implements Serializable {
    private String encryptedPassword;

    Password() {
        encryptedPassword = null;
    }

    public Password(String password, boolean encrypted) {
        if (!encrypted) {
            encryptedPassword = encrypt(password);
        } else {
            encryptedPassword = password;
        }
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    private String encrypt(String password) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        DigestInputStream dis = new DigestInputStream(new ByteArrayInputStream(password.getBytes()), md);

        try {
            while (dis.read() != -1) {
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] digest = md.digest();

        String output = String.format("%032X", new BigInteger(1, digest));
        return output.toLowerCase();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Password password = (Password) other;
        return Objects.equals(encryptedPassword, password.encryptedPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encryptedPassword);
    }
}
