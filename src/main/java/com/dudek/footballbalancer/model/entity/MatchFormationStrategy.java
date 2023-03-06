package com.dudek.footballbalancer.model.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchFormationStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int formation;

    @Setter(AccessLevel.NONE)
    private final int playersQuantity = countPlayersQuantity();

    @Setter(AccessLevel.NONE)
    private final int fieldSectionsQuantity = countFieldSectionsQuantity();

    @OneToMany(cascade = CascadeType.MERGE)
    @JoinColumn(name = "fieldSection_id")
    @Size(min = 2)
    private Set<FieldSection> fieldSections = new HashSet<>();

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Room room;

    private int countPlayersQuantity() {
        String formation = String.valueOf(getFormation());
        IntStream chars = formation.chars();
        return chars.sum();
    }

    private int countFieldSectionsQuantity() {
        String formation = String.valueOf(getFormation());
        return formation.length();
    }
}
