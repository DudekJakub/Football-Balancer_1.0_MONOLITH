package com.dudek.footballbalancer.model.entity;

import lombok.*;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int playersQuantity;

    @ElementCollection
    @CollectionTable(joinColumns = { @JoinColumn(name = "fieldSection_id", referencedColumnName = "id") })
    @MapKeyColumn(name = "skillTemplate_id")
    @Column(name = "multiplier")
    private Map<SkillTemplate, Double> skillTemplatesWithMultipliers = new HashMap<>();
}
