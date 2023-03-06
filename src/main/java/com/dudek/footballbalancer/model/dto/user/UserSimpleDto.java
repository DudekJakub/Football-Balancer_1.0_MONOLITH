package com.dudek.footballbalancer.model.dto.user;

import com.dudek.footballbalancer.model.Role;
import com.dudek.footballbalancer.model.SexStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UserSimpleDto {

    private Long id;
    private String firstName;
    private String lastName;
    private SexStatus sex;
    private Role role;
    private String email;
}
