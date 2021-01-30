package org.perrierFrancois.turing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.perrierFrancois.turing.definition.Action;
import org.perrierFrancois.turing.definition.Move;
import org.perrierFrancois.turing.definition.TuringMachineDefinition;

import java.util.*;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class TuringMachine {
    public static final String EMPTY_SYMBOL = "";

    //// config
    private final String initialState;

    private final Set<String> finalStates;

    private final Map<ActionKey, Runnable> actionTable;

    //runtime
    @Getter
    private MachineState machineState;

    @Getter
    private Ribbon ribbon;

    @Getter
    private String internalState;


    public TuringMachine(TuringMachineDefinition definition) {
        this.initialState = definition.getInitialState();
        this.finalStates = new HashSet<>(definition.getFinalStates());
        this.actionTable = definition.getActions().stream()
                .collect(toMap(ActionKey::new, ActionCommand::new));

        reset();
    }

    public void reset() {
        this.internalState = this.initialState;
        this.ribbon = null;
        this.machineState = MachineState.READY;
    }

    public void initialize(List<String> ribbon) {
        assertState(MachineState.READY);
        this.ribbon = new Ribbon(ribbon);
        this.machineState = MachineState.RUNNING;
    }

    public void nextStep() {
        assertState(MachineState.RUNNING);

        final String currentSymbol = ribbon.read();
        final Runnable action = actionTable.get(ActionKey.of(internalState, currentSymbol));

        if (action == null) {
            this.machineState = MachineState.ILLEGAL_STATE;
            return;
        }

        action.run();
    }

    @Override
    public String toString() {
        String result = "Machine state:  " + machineState.name() + lineSeparator() +
                "Internal state: " + internalState + lineSeparator();

        if (ribbon != null) {
            result += lineSeparator() + ribbon.toString();
        }

        return result;
    }

    private void assertState(MachineState expectedMachineState) {
        if (this.machineState != expectedMachineState) {
            throw new IllegalStateException(format("Machine should be in state %s for this operation", expectedMachineState));
        }
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    private static class ActionKey {
        private final String state;
        private final String symbol;

        public ActionKey(Action action) {
            this.state = action.getInState();
            this.symbol = action.getWhenReading();
        }
    }

    @Data
    private class ActionCommand implements Runnable {
        private final String symbolToWrite;
        private final Move move;
        private final String nextState;

        public ActionCommand(Action action) {
            this.symbolToWrite = action.getWrite();
            this.move = action.getMove();
            this.nextState = action.getToState();
        }

        @Override
        public void run() {
            ribbon.write(symbolToWrite);
            ribbon.move(move);
            internalState = nextState;
            if (finalStates.contains(internalState)) {
                machineState = MachineState.ACCEPTED;
            }
        }
    }

    public static class Ribbon {

        private final LinkedList<String> symbols;

        @Getter
        private int position;

        public Ribbon(List<String> initialState) {
            this.symbols = new LinkedList<>(initialState);
            if (symbols.isEmpty()) {
                symbols.add(EMPTY_SYMBOL);
            }

            this.position = 0;
        }

        public List<String> getSymbols() {
            return unmodifiableList(symbols);
        }

        private void move(Move move) {
            switch (move) {

                case DONT_MOVE:
                    break;
                case LEFT:
                    if (position == 0) {
                        symbols.addFirst(EMPTY_SYMBOL);
                    } else {
                        position--;
                    }
                    break;
                case RIGHT:
                    position++;
                    if (position == symbols.size()) {
                        symbols.addLast(EMPTY_SYMBOL);
                    }
                    break;
            }
        }

        private String read() {
            return symbols.get(position);
        }

        private void write(String symbol) {
            symbols.set(position, symbol);
        }

        @Override
        public String toString() {
            int maxSymbolSize = Math.max(1, symbols.stream().mapToInt(String::length).max().orElse(0));

            final String format = "%" + maxSymbolSize + "s";

            final String symbols = this.symbols.stream()
                    .map(symbol -> format(format, symbol))
                    .collect(joining("|", "|", "|"));

            final String marker = format("%" + (position * (maxSymbolSize + 1) + 2) + "s", "^");

            return symbols + "\n" + marker;
        }
    }
}
