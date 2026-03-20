package com.campus.exchange;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordGenTest {

    @Test
    public void generatePassword() {
        String password = "123456";
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println("GenPasswordHashStart");
        System.out.println(hash);
        System.out.println("GenPasswordHashEnd");
    }
}
