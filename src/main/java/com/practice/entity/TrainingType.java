package com.practice.entity;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TrainingType {
    private Long id;
    private String trainingTypeName;
}
