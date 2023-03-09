package com.dudek.footballbalancer.model.entity;

import lombok.*;
import org.bouncycastle.asn1.its.Latitude;
import org.springframework.data.geo.Point;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;

    private String zipCode;

    private String street;

    private int number;

    private double latitude;

    private double longitude;

    @OneToMany(mappedBy = "fieldLocation", cascade = CascadeType.MERGE)
    private Set<Room> room = new HashSet<>();
}
