package org.perrierFrancois.turing.generators;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.perrierFrancois.turing.definition.Action;
import org.perrierFrancois.turing.definition.Move;
import org.perrierFrancois.turing.definition.TuringMachineDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

import static org.perrierFrancois.turing.TuringMachine.EMPTY_SYMBOL;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BinaryAdderDefinitionGenerator extends TuringMachineDefinitionGeneratorSupport {

    public static final String ZERO = "0";
    public static final String ONE = "1";
    private static final List<String> SYMBOLS = Arrays.asList(ZERO, ONE);

    private final int bits;

    public static TuringMachineDefinition buildDefinition(int bits) {
        return new BinaryAdderDefinitionGenerator(bits).doBuildDefinition();
    }

    @Override
    protected List<String> symbols() {
        return SYMBOLS;
    }

    private TuringMachineDefinition doBuildDefinition() {
        final List<Action> actions = new ArrayList<>();
        moveN(actions, i -> "moveToLeftOperandDigit0_" + i, Move.RIGHT, bits - 1, "addDigits0_0");

        for (int b = 0; b < bits; b++) {
            int finalB = b;
            String nextState;
            Move lastAdderMove;
            if (b == bits - 1) {
                nextState = "end";
                lastAdderMove = Move.DONT_MOVE;
            } else {
                nextState = "moveToLeftOperandDigit" + (b + 1) + "_0";
                lastAdderMove = Move.LEFT;
            }

            fullAdder(actions, Move.RIGHT, bits, Move.RIGHT, bits + 1, i -> "addDigits" + finalB + "_" + i, b == 0, lastAdderMove, nextState);

            if (b != bits - 1) {
                moveN(actions, i -> "moveToLeftOperandDigit" + (finalB + 1) + "_" + i, Move.LEFT, 2 * bits, "addDigits" + (b + 1) + "_0");
            }
        }

        return TuringMachineDefinition.builder()
                .initialState("moveToLeftOperandDigit0_0")
                .finalState("end")
                .actions(actions)
                .build();
    }

    private void fullAdder(List<Action> actions,
                           Move directionToSecondOperand, int offsetToSecondOperand,
                           Move directionToResult, int offsetToResult,
                           IntFunction<String> stateNameTemplate, boolean handleEmptyCarry, Move lastMove, String nextState) {
        //BRANCH 1: 0 + ?, C=?
        IntFunction<String> read0StateTemplate = i -> stateNameTemplate.apply(i + 1) + " (0 + ?, C=?)";
        ifSymbolMove(actions, stateNameTemplate.apply(0), ZERO, directionToSecondOperand, read0StateTemplate.apply(0));
        moveN(actions, read0StateTemplate, directionToSecondOperand, offsetToSecondOperand - 1, read0StateTemplate.apply(offsetToSecondOperand - 1));

        {
            //BRANCH 1.1: 0 + 0, C=?
            IntFunction<String> read00StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + 1) + " (0 + 0, C=?)";
            ifSymbolMove(actions, read0StateTemplate.apply(offsetToSecondOperand - 1), ZERO, directionToSecondOperand, read00StateTemplate.apply(0));
            moveN(actions, read00StateTemplate, directionToResult, offsetToResult - 1, read00StateTemplate.apply(offsetToResult - 1));

            {
                if (handleEmptyCarry) {
                    //BRANCH 1.1.0: 0 + 0, C=_ -> 0 0
                    IntFunction<String> read00_StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + offsetToResult + 1) + " (0 + 0, C=_)";
                    ifSymbolWriteAndMove(actions, read00StateTemplate.apply(offsetToResult - 1), EMPTY_SYMBOL, ZERO, Move.LEFT, read00_StateTemplate.apply(0));
                    writeAndMove(actions, read00_StateTemplate.apply(0), ZERO, lastMove, nextState);
                }

                //BRANCH 1.1.1: 0 + 0, C=0 -> 0 0
                IntFunction<String> read000StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + offsetToResult + 1) + " (0 + 0, C=0)";
                ifSymbolWriteAndMove(actions, read00StateTemplate.apply(offsetToResult - 1), ZERO, ZERO, Move.LEFT, read000StateTemplate.apply(0));
                writeAndMove(actions, read000StateTemplate.apply(0), ZERO, lastMove, nextState);

                //BRANCH 1.1.2: 0 + 0, C=1 -> 0 1
                IntFunction<String> read001StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + offsetToResult + 1) + " (0 + 0, C=1)";
                ifSymbolWriteAndMove(actions, read00StateTemplate.apply(offsetToResult - 1), ONE, ONE, Move.LEFT, read001StateTemplate.apply(0));
                writeAndMove(actions, read001StateTemplate.apply(0), ZERO, lastMove, nextState);
            }

            //BRANCH 1.2: 0 + 1, C=?
            IntFunction<String> read01StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + 1) + " (0 + 1, C=?)";
            ifSymbolMove(actions, read0StateTemplate.apply(offsetToSecondOperand - 1), ONE, directionToSecondOperand, read01StateTemplate.apply(0));
            moveN(actions, read01StateTemplate, directionToResult, offsetToResult - 1, read01StateTemplate.apply(offsetToResult - 1));

            {
                if (handleEmptyCarry) {
                    //BRANCH 1.2.0: 0 + 1, C=_ -> 0 1
                    IntFunction<String> read01_StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + offsetToResult + 1) + " (0 + 1, C=_)";
                    ifSymbolWriteAndMove(actions, read01StateTemplate.apply(offsetToResult - 1), EMPTY_SYMBOL, ONE, Move.LEFT, read01_StateTemplate.apply(0));
                    writeAndMove(actions, read01_StateTemplate.apply(0), ZERO, lastMove, nextState);
                }

                //BRANCH 1.2.1: 0 + 1, C=0 -> 0 1
                IntFunction<String> read010StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + offsetToResult + 1) + " (0 + 1, C=0)";
                ifSymbolWriteAndMove(actions, read01StateTemplate.apply(offsetToResult - 1), ZERO, ONE, Move.LEFT, read010StateTemplate.apply(0));
                writeAndMove(actions, read010StateTemplate.apply(0), ZERO, lastMove, nextState);

                //BRANCH 1.2.2: 0 + 1, C=1 -> 1 0
                IntFunction<String> read011StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + offsetToResult + 1) + " (0 + 1, C=1)";
                ifSymbolWriteAndMove(actions, read01StateTemplate.apply(offsetToResult - 1), ONE, ZERO, Move.LEFT, read011StateTemplate.apply(0));
                writeAndMove(actions, read011StateTemplate.apply(0), ONE, lastMove, nextState);
            }
        }

        //BRANCH 2: 1 + ?, C=?
        IntFunction<String> read1StateTemplate = i -> stateNameTemplate.apply(i + 1) + " (1 + ?, C=?)";
        ifSymbolMove(actions, stateNameTemplate.apply(0), ONE, directionToSecondOperand, read1StateTemplate.apply(0));
        moveN(actions, read1StateTemplate, directionToSecondOperand, offsetToSecondOperand - 1, read1StateTemplate.apply(offsetToSecondOperand - 1));

        {
            //BRANCH 2.1: 1 + 0, C=?
            IntFunction<String> read10StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + 1) + " (1 + 0, C=?)";
            ifSymbolMove(actions, read1StateTemplate.apply(offsetToSecondOperand - 1), ZERO, directionToSecondOperand, read10StateTemplate.apply(0));
            moveN(actions, read10StateTemplate, directionToResult, offsetToResult - 1, read10StateTemplate.apply(offsetToResult - 1));

            {
                if (handleEmptyCarry) {
                    //BRANCH 2.1.0: 1 + 0, C=_ -> 0 1
                    IntFunction<String> read10_StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + offsetToResult + 1) + " (1 + 0, C=_)";
                    ifSymbolWriteAndMove(actions, read10StateTemplate.apply(offsetToResult - 1), EMPTY_SYMBOL, ONE, Move.LEFT, read10_StateTemplate.apply(0));
                    writeAndMove(actions, read10_StateTemplate.apply(0), ZERO, lastMove, nextState);
                }

                //BRANCH 2.1.1: 1 + 0, C=0 -> 0 1
                IntFunction<String> read100StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + offsetToResult + 1) + " (1 + 0, C=0)";
                ifSymbolWriteAndMove(actions, read10StateTemplate.apply(offsetToResult - 1), ZERO, ONE, Move.LEFT, read100StateTemplate.apply(0));
                writeAndMove(actions, read100StateTemplate.apply(0), ZERO, lastMove, nextState);

                //BRANCH 2.1.2: 1 + 0, C=1 -> 1 0
                IntFunction<String> read101StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + offsetToResult + 1) + " (1 + 0, C=1)";
                ifSymbolWriteAndMove(actions, read10StateTemplate.apply(offsetToResult - 1), ONE, ZERO, Move.LEFT, read101StateTemplate.apply(0));
                writeAndMove(actions, read101StateTemplate.apply(0), ONE, lastMove, nextState);
            }

            //BRANCH 2.2: 1 + 1, C=?
            IntFunction<String> read11StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + 1) + " (1 + 1, C=?)";
            ifSymbolMove(actions, read1StateTemplate.apply(offsetToSecondOperand - 1), ONE, directionToSecondOperand, read11StateTemplate.apply(0));
            moveN(actions, read11StateTemplate, directionToResult, offsetToResult - 1, read11StateTemplate.apply(offsetToResult - 1));

            {
                if (handleEmptyCarry) {
                    //BRANCH 2.2.0: 1 + 1, C=_ -> 1 0
                    IntFunction<String> read11_StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + offsetToResult + 1) + " (1 + 1, C=_)";
                    ifSymbolWriteAndMove(actions, read11StateTemplate.apply(offsetToResult - 1), EMPTY_SYMBOL, ZERO, Move.LEFT, read11_StateTemplate.apply(0));
                    writeAndMove(actions, read11_StateTemplate.apply(0), ONE, lastMove, nextState);
                }

                //BRANCH 2.2.1: 1 + 1, C=0 -> 1 0
                IntFunction<String> read110StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + offsetToResult + 1) + " (1 + 1, C=0)";
                ifSymbolWriteAndMove(actions, read11StateTemplate.apply(offsetToResult - 1), ZERO, ZERO, Move.LEFT, read110StateTemplate.apply(0));
                writeAndMove(actions, read110StateTemplate.apply(0), ONE, lastMove, nextState);

                //BRANCH 2.2.2: 1 + 1, C=1 -> 1 1
                IntFunction<String> read111StateTemplate = i -> stateNameTemplate.apply(i + offsetToSecondOperand + offsetToResult + 1) + " (1 + 1, C=1)";
                ifSymbolWriteAndMove(actions, read11StateTemplate.apply(offsetToResult - 1), ONE, ONE, Move.LEFT, read111StateTemplate.apply(0));
                writeAndMove(actions, read111StateTemplate.apply(0), ONE, lastMove, nextState);
            }
        }
    }
}
