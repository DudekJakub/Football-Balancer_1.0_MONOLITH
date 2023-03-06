package com.dudek.footballbalancer.model.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkillTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 10)
    private String name;

    private boolean active = true;

    private boolean defaultSkill;

    @OneToMany(mappedBy = "skillTemplate")
    private Set<Skill> skills = new HashSet<>();

    @ManyToMany(mappedBy = "skillTemplatesForRoom")
    private Set<Room> rooms = new HashSet<>();
}
