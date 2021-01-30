package org.perrierFrancois.turing.definition;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class TuringMachineDefinition {

    private String initialState;

    @Singular
    private Set<String> finalStates;

    @Singular
    private List<Action> actions;
}
