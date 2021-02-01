package org.perrierFrancois.turing;

import lombok.*;
import org.perrierFrancois.turing.definition.Action;
import org.perrierFrancois.turing.definition.Move;
import org.perrierFrancois.turing.definition.TuringMachineDefinition;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class TuringMachine {
    public static final String EMPTY_SYMBOL = "";

    // config
    private final String initialState;

    private final Set<String> finalStates;

    private final Map<ActionKey, Runnable> actionTable;

    // runtime
    /**
     * Transitions counter (purely informal)
     */
    @Getter
    private int transitions;

    @Getter
    private MachineState machineState;

    @Getter
    private Tape tape;

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
        this.transitions = 0;
        this.internalState = this.initialState;
        this.tape = null;
        this.machineState = MachineState.READY;
    }

    public void initialize(List<String> ribbon) {
        assertState(MachineState.READY);
        this.tape = new Tape(ribbon);
        this.machineState = MachineState.RUNNING;
    }

    public void nextStep() {
        assertState(MachineState.RUNNING);

        final String currentSymbol = tape.read();
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

        if (tape != null) {
            result += lineSeparator() + tape.toString();
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
            tape.write(symbolToWrite);

            tape.move(move);

            internalState = nextState;
            transitions++;

            if (finalStates.contains(internalState)) {
                machineState = MachineState.ACCEPTED;
            }
        }
    }

    public static class Tape implements Iterable<Cell> {

        private Cell currentCell;
        private Cell firstCell;

        //keep track of the position to avoid having to iterate on the whole tape to find where we are;
        @Getter
        private int position = 0;

        public Tape(List<String> initialState) {
            if (initialState.isEmpty()) {
                firstCell = Cell.empty();
            } else {
                firstCell = Cell.of(initialState.get(0));
                //noinspection ResultOfMethodCallIgnored
                initialState.stream()
                        .skip(1)
                        .map(Cell::of)
                        .reduce(firstCell, (prev, cur) -> {
                            prev.next = cur;
                            cur.prev = prev;
                            return cur;
                        });
            }

            currentCell = firstCell;
        }

        public List<String> getSymbols() {
            return this.stream()
                    .map(Cell::getSymbol)
                    .collect(Collectors.toList());
        }

        public int getPosition() {
            return position;
        }

        private void move(Move move) {
            switch (move) {

                case DONT_MOVE:
                    break;
                case LEFT:
                    if (currentCell.getPrev() == null) {
                        currentCell.prev = Cell.empty();
                        currentCell.prev.next = currentCell;
                        firstCell = currentCell.getPrev();
                    } else {
                        position--;
                    }
                    currentCell = currentCell.getPrev();
                    break;
                case RIGHT:
                    if (currentCell.getNext() == null) {
                        currentCell.next = Cell.empty();
                        currentCell.next.prev = currentCell;
                    }

                    position++;

                    currentCell = currentCell.getNext();
                    break;
            }
        }

        private String read() {
            return currentCell.getSymbol();
        }

        private void write(String symbol) {
            currentCell.symbol = symbol;
        }

        @Override
        public String toString() {
            final List<String> symbols = getSymbols();

            final int maxSymbolSize = Math.max(1, symbols.stream()
                    .mapToInt(String::length)
                    .max().orElse(0));

            final String format = "%" + maxSymbolSize + "s";

            final String tape = symbols.stream()
                    .map(symbol -> format(format, symbol))
                    .collect(joining("|", "|", "|"));

            final String marker = format("%" + (position * (maxSymbolSize + 1) + 2) + "s", "^");

            return tape + lineSeparator() + marker;
        }

        public Stream<Cell> stream() {
            return StreamSupport.stream(spliterator(), false);
        }

        @Override
        public Iterator<Cell> iterator() {
            return new Iterator<>() {
                private Cell next = firstCell;

                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public Cell next() {
                    if (next == null) {
                        throw new NoSuchElementException();
                    }

                    final Cell next = this.next;
                    this.next = next.next;
                    return next;
                }
            };
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Cell {
        private String symbol;
        private Cell next;
        private Cell prev;

        public static Cell of(String symbol) {
            return new Cell(symbol, null, null);
        }

        public static Cell empty() {
            return of(EMPTY_SYMBOL);
        }

        @Override
        public String toString() {
            return symbol;
        }
    }
}
