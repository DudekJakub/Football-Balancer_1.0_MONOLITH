package com.dudek.footballbalancer.model.entity;

import com.dudek.footballbalancer.model.Role;
import com.dudek.footballbalancer.model.SexStatus;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements MessageParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String username;

    @NotBlank
    private String lastName;

    @NotBlank
    private String password;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private SexStatus sex;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private Role role;

    @Email
    private String email;

    @ManyToMany(mappedBy = "adminsInRoom")
    private Set<Room> roomsAsAdminUser = new HashSet<>();

    @ManyToMany(mappedBy = "usersInRoom")
    private Set<Room> roomsAsStandardUser = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Player> linkedPlayers = new HashSet<>();

    @Override
    public String getParticipantName() {
        return this.username;
    }

    @Override
    public Long getParticipantId() {
        return this.id;
    }
}
