package com.practice.utility;

import com.practice.entity.User;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@UtilityClass
public class ServiceUtility {

    public String generateUserName(String firstName, String lastName, List<? extends User> trainees) {
        String baseUsername = firstName + "." + lastName;

        Set<String> existingUsernames = trainees.stream()
                .map(User::getUserName)
                .collect(Collectors.toSet());

        if (!existingUsernames.contains(baseUsername)) {
            return baseUsername;
        }

        int counter = 1;
        String username;

        do {
            username = baseUsername + counter;
            counter++;
        } while (existingUsernames.contains(username));

        return username;
    }

    public String generatePassword(){
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10);
    }
}
