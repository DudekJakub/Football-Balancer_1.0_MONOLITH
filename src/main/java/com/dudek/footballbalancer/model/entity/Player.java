package com.dudek.footballbalancer.model.entity;

import com.dudek.footballbalancer.model.SexStatus;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private SexStatus sex;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Column(name = "OVERALL")
    private double generalOverall;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Skill> skills = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id")
    private User user;
}
