package org.perrierFrancois.turing.generators;

import org.perrierFrancois.turing.definition.Action;
import org.perrierFrancois.turing.definition.Move;

import java.util.List;
import java.util.function.IntFunction;

import static org.perrierFrancois.turing.TuringMachine.EMPTY_SYMBOL;

public abstract class TuringMachineDefinitionGeneratorSupport {

    protected abstract List<String> symbols();

    protected void moveN(List<Action> actions, IntFunction<String> inStateTemplate, Move direction, int offset, String toState) {
        for (int i = 0; i < offset; i++) {
            String cur = inStateTemplate.apply(i);
            String next = i == offset - 1 ? toState : inStateTemplate.apply(i + 1);
            actions.add(
                    Action.builder()
                            .inState(cur)
                            .whenReading(EMPTY_SYMBOL)
                            .write(EMPTY_SYMBOL)
                            .move(direction)
                            .toState(next)
                            .build()
            );

            symbols().forEach(s -> actions.add(
                    Action.builder()
                            .inState(cur)
                            .whenReading(s)
                            .write(s)
                            .move(direction)
                            .toState(next)
                            .build()
            ));
        }
    }

    protected void ifSymbolMove(List<Action> actions, String inState, String symbol, Move move, String toState) {
        ifSymbolWriteAndMove(actions, inState, symbol, symbol, move, toState);
    }

    protected void ifSymbolWriteAndMove(List<Action> actions, String inState, String symbol, String writeSymbol, Move move, String toState) {
        actions.add(
                Action.builder()
                        .inState(inState)
                        .whenReading(symbol)
                        .write(writeSymbol)
                        .move(move)
                        .toState(toState)
                        .build()
        );
    }


    protected void write(List<Action> actions, String inState, String symbol, String toState) {
        writeAndMove(actions, inState, symbol, Move.DONT_MOVE, toState);
    }

    protected void writeAndMove(List<Action> actions, String inState, String symbol, Move move, String toState) {
        actions.add(
                Action.builder()
                        .inState(inState)
                        .whenReading(EMPTY_SYMBOL)
                        .write(symbol)
                        .move(move)
                        .toState(toState)
                        .build()
        );

        symbols().forEach(s -> actions.add(
                Action.builder()
                        .inState(inState)
                        .whenReading(s)
                        .write(symbol)
                        .move(move)
                        .toState(toState)
                        .build()
        ));
    }
}
