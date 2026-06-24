package com.practice.entity;

import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Trainee extends User{
    private LocalDate dateOfBirth;
    private String address;
}
