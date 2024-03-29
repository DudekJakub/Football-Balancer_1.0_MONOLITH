package com.dudek.footballbalancer.model.entity.request;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long requesterId;

    @NotBlank
    private String requesterName;

    @Enumerated(value = EnumType.STRING)
    private RequestStatus status;

    @Enumerated(value = EnumType.STRING)
    private RequestType type;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(targetEntity = RequestableEntity.class)
    @JoinColumn(name = "requestable_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Requestable requestable;

    @Column(name = "requestable_type")
    private String requestableType;
}
