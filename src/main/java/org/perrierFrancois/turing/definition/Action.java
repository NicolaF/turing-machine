package org.perrierFrancois.turing.definition;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Action {

    private String inState;

    private String whenReading;

    private String write;

    private Move move;

    private String toState;
}
