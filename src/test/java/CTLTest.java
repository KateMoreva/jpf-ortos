import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.ctl.CTLLexer;
import org.ctl.CTLParser;
import org.junit.Test;

import ctl.Formula;
import ctl.Generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CTLTest {
    /**
     * Generates abstract syntax tree from parse tree.
     */
    private static final Generator generator = new Generator();

    /**
     * Number of times tests that involve randomness are repeated.
     */
    protected static final int TIMES = 1000;

    public static Formula parse(String formula) {

        CharStream input = CharStreams.fromString(formula);
        CTLLexer lexer = new CTLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CTLParser parser = new CTLParser(tokens);
        ParseTree tree = parser.formula();
        return generator.visit(tree);
    }

    @Test
    public void test() {
        // generate a random abstract syntax tree
        Formula randomFormula = Formula.random();
        // obtain the abstract syntax tree of the textual representation of the abstract syntax tree
        Formula formula = parse(randomFormula.toString());
        assertNotNull(formula);
        assertEquals(randomFormula, formula);
    }
}
