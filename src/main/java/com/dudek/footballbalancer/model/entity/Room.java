package com.dudek.footballbalancer.model.entity;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 30)
    private String name;

    @NotBlank
    private String password;

    @Size(max = 500)
    private String description;

    private boolean isPublic = true;

    private LocalDateTime nextMatchDate;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "field_location_id")
    private FieldLocation fieldLocation;

    @ManyToMany(targetEntity = Player.class, cascade = CascadeType.MERGE)
    @JoinTable(name = "room_player", joinColumns = @JoinColumn(name = "room_id", referencedColumnName = "id"),
                                     inverseJoinColumns = @JoinColumn(name = "player_id", referencedColumnName = "id"))
    private Set<Player> playersInRoom = new HashSet<>();

    @ManyToMany(targetEntity = User.class, cascade = CascadeType.MERGE)
    @JoinTable(name = "room_user", joinColumns = @JoinColumn(name = "room_id", referencedColumnName = "id"),
                                   inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private Set<User> usersInRoom = new HashSet<>();

    @ManyToMany(targetEntity = User.class, cascade = CascadeType.MERGE)
    @JoinTable(name = "room_admin", joinColumns = @JoinColumn(name = "room_id", referencedColumnName = "id"),
                                    inverseJoinColumns = @JoinColumn(name = "admin_id", referencedColumnName = "id"))
    private Set<User> adminsInRoom = new HashSet<>();

    @ManyToMany(targetEntity = SkillTemplate.class, cascade = CascadeType.MERGE)
    @JoinTable(name = "room_skill_template", joinColumns = @JoinColumn(name = "room_id", referencedColumnName = "id"),
                                             inverseJoinColumns = @JoinColumn(name = "skill_template_id", referencedColumnName = "id"))
    private Set<SkillTemplate> skillTemplatesForRoom = new HashSet<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private Set<MatchFormationStrategy> formationStrategies = new HashSet<>();
}
