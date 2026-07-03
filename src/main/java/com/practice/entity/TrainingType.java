package com.practice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"trainings", "trainers"})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "training_types")
public class TrainingType {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "training_type_id")
    private Long id;

    @Column(name = "training_type_name", nullable = false, unique = true)
    private String name;

    @Builder.Default
    @OneToMany(mappedBy = "trainingType", fetch = FetchType.LAZY)
    private List<Training> trainings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "trainingType", fetch = FetchType.LAZY)
    private List<Trainer> trainers = new ArrayList<>();
}
