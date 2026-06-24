package com.practice.entity;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString(exclude = "password")
public class User {

    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private boolean isActive;
}
