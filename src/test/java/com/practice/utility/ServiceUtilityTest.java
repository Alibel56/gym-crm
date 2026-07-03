package com.practice.utility;

import com.practice.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class ServiceUtilityTest {

    private User buildUser(String userName) {
        return User.builder().userName(userName).build();
    }

    @Test
    void generateUserName_noExistingUsers_returnsBaseUsername() {
        String result = ServiceUtility.generateUserName("John", "Doe", List.of());

        assertEquals("John.Doe", result);
    }

    @Test
    void generateUserName_noCollision_returnsBaseUsername() {
        List<User> existing = List.of(buildUser("Jane.Smith"), buildUser("Bob.Brown"));

        String result = ServiceUtility.generateUserName("John", "Doe", existing);

        assertEquals("John.Doe", result);
    }

    @Test
    void generateUserName_baseUsernameTaken_appendsCounterOne() {
        List<User> existing = List.of(buildUser("John.Doe"));

        String result = ServiceUtility.generateUserName("John", "Doe", existing);

        assertEquals("John.Doe1", result);
    }

    @Test
    void generateUserName_baseAndFirstSuffixTaken_appendsCounterTwo() {
        List<User> existing = List.of(buildUser("John.Doe"), buildUser("John.Doe1"));

        String result = ServiceUtility.generateUserName("John", "Doe", existing);

        assertEquals("John.Doe2", result);
    }

    @Test
    void generateUserName_severalSuffixesTaken_skipsAllOfThem() {
        List<User> existing = List.of(
                buildUser("John.Doe"),
                buildUser("John.Doe1"),
                buildUser("John.Doe2"),
                buildUser("John.Doe3")
        );

        String result = ServiceUtility.generateUserName("John", "Doe", existing);

        assertEquals("John.Doe4", result);
    }

    @Test
    void generateUserName_gapInSuffixSequence_stillReturnsFirstFreeSlot() {
        // Doe1 is free even though Doe and Doe2 are taken - method should stop at first gap
        List<User> existing = List.of(buildUser("John.Doe"), buildUser("John.Doe2"));

        String result = ServiceUtility.generateUserName("John", "Doe", existing);

        assertEquals("John.Doe1", result);
    }

    @Test
    void generateUserName_unrelatedExistingUsernames_returnsBaseUsername() {
        List<User> existing = List.of(buildUser("Alice.Wonder"), buildUser("Mark.Twain"));

        String result = ServiceUtility.generateUserName("John", "Doe", existing);

        assertEquals("John.Doe", result);
    }

    @Test
    void generatePassword_returnsStringOfLengthTen() {
        String password = ServiceUtility.generatePassword();

        assertEquals(10, password.length());
    }

    @Test
    void generatePassword_containsNoHyphens() {
        String password = ServiceUtility.generatePassword();

        assertFalse(password.contains("-"));
    }

    @Test
    void generatePassword_containsOnlyHexCharacters() {
        String password = ServiceUtility.generatePassword();

        assertTrue(Pattern.matches("[0-9a-f]{10}", password),
                "Password should consist of exactly 10 lowercase hex characters, was: " + password);
    }

    @RepeatedTest(20)
    void generatePassword_repeatedCalls_produceDifferentValues() {
        String first = ServiceUtility.generatePassword();
        String second = ServiceUtility.generatePassword();

        assertNotEquals(first, second);
    }
}