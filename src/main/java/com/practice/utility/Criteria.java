package com.practice.utility;

import lombok.*;

import java.time.LocalDate;
import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Criteria {

    private LocalDate fromDate;
    private LocalDate toDate;
    private String trainerName;
    private String traineeName;
    private String trainingType;
}
