package com.dudek.footballbalancer.model;

import com.dudek.footballbalancer.model.entity.FieldSection;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Builder
public class Formation {

    @NotNull
    private int providedFormationSchema;

    @NotNull
    private int providedPlayersQuantityPerTeam;

    @Size(min = 2)
    private List<FieldSection> providedFieldSections;
}
