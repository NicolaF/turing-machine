package org.perrierFrancois.turing.generators;

import org.junit.jupiter.api.Test;
import org.perrierFrancois.turing.MachineState;
import org.perrierFrancois.turing.TuringMachine;
import org.perrierFrancois.turing.definition.TuringMachineDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.collectingAndThen;
import static org.assertj.core.api.Assertions.assertThat;

class BinaryAdderDefinitionGeneratorTest {

    @Test
    public void testAdder() {
        final int bits = 8;

        System.out.println(format("Building %d-bits adder", bits));
        TuringMachineDefinition definition = BinaryAdderDefinitionGenerator.buildDefinition(bits);
        System.out.println(format("Machine definition contains %d actions", definition.getActions().size()));
        System.out.println();

        TuringMachine machine = new TuringMachine(definition);

        for (int a = 0; a < 1 << bits; a++) {
            for (int b = 0; b < 1 << bits; b++) {
                System.out.println(format(
                        //@formatter:off
                        "###################################################\n" +
                        "#               Computing %3d + %3d               #\n" +
                        "###################################################",
                        //@formatter:on
                        a, b
                ));
                machine.reset();
                machine.initialize(buildTape(bits, a, b));
                run(machine);
                final int result = assertResult(machine, bits, a, b);
                System.out.println(format("%d + %d = %d (%d transitions)", a, b, result, machine.getTransitions()));
            }
        }
    }

    private void run(TuringMachine turingMachine) {
        System.out.println(turingMachine.toString());

        while (!turingMachine.getMachineState().isFinal()) {
            turingMachine.nextStep();
            System.out.println("###################################################");
            System.out.println(turingMachine.toString());
        }
    }

    private List<String> buildTape(int bits, int a, int b) {
        List<String> ribbon = new ArrayList<>();
        ribbon.addAll(buildNumber(a, bits));
        ribbon.addAll(buildNumber(b, bits));

        return ribbon;
    }

    private List<String> buildNumber(int i, int bits) {
        String strI = format("%" + bits + "s", Integer.toString(i, 2)).replace(' ', '0');
        return Arrays.asList(strI.split(""));
    }

    private int assertResult(TuringMachine machine, int bits, int a, int b) {
        assertThat(machine.getMachineState()).isEqualTo(MachineState.ACCEPTED);
        assertThat(machine.getTape().getPosition()).isEqualTo(2 * bits);

        final Integer result = machine.getTape().getSymbols().subList(2 * bits, 3 * bits + 1).stream()
                .collect(collectingAndThen(Collectors.joining(), s -> Integer.valueOf(s, 2)));

        assertThat(result).isEqualTo(a + b);

        return result;
    }

}