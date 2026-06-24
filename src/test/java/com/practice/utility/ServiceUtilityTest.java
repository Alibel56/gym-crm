package com.practice.utility;

import com.practice.entity.Trainee;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServiceUtilityTest {

    @Test
    void generateUserName_ShouldReturnBaseUsername_WhenUsernameIsFree() {
        Trainee trainee = new Trainee();
        trainee.setUserName("Mike.Tyson");

        String result = ServiceUtility.generateUserName(
                "John",
                "Smith",
                List.of(trainee)
        );

        assertEquals("John.Smith", result);
    }

    @Test
    void generateUserName_ShouldAppendOne_WhenBaseUsernameExists() {
        Trainee trainee = new Trainee();
        trainee.setUserName("John.Smith");

        String result = ServiceUtility.generateUserName(
                "John",
                "Smith",
                List.of(trainee)
        );

        assertEquals("John.Smith1", result);
    }

    @Test
    void generateUserName_ShouldFindFirstAvailableSuffix() {
        Trainee trainee1 = new Trainee();
        trainee1.setUserName("John.Smith");

        Trainee trainee2 = new Trainee();
        trainee2.setUserName("John.Smith1");

        Trainee trainee3 = new Trainee();
        trainee3.setUserName("John.Smith2");

        String result = ServiceUtility.generateUserName(
                "John",
                "Smith",
                List.of(trainee1, trainee2, trainee3)
        );

        assertEquals("John.Smith3", result);
    }

    @Test
    void generateUserName_ShouldWorkWithEmptyList() {
        String result = ServiceUtility.generateUserName(
                "John",
                "Smith",
                List.of()
        );

        assertEquals("John.Smith", result);
    }

    @Test
    void generatePassword_ShouldGeneratePasswordWithLength10() {
        String password = ServiceUtility.generatePassword();

        assertNotNull(password);
        assertEquals(10, password.length());
    }

    @Test
    void generatePassword_ShouldGenerateDifferentPasswords() {
        String password1 = ServiceUtility.generatePassword();
        String password2 = ServiceUtility.generatePassword();

        assertNotEquals(password1, password2);
    }

    @Test
    void generatePassword_ShouldNotContainHyphen() {
        String password = ServiceUtility.generatePassword();

        assertFalse(password.contains("-"));
    }
}
