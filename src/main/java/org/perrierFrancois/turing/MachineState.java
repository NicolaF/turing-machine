package org.perrierFrancois.turing;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MachineState {
    READY(false),
    RUNNING(false),
    ACCEPTED(true),
    ILLEGAL_STATE(true);

    private final boolean finalState;

    public boolean isFinal() {
        return finalState;
    }
}
