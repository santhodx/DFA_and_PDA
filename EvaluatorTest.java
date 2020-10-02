import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorTest {

    @org.junit.jupiter.api.BeforeEach
    void setUp() {

    }

    @org.junit.jupiter.api.Test
    void evaluate() throws UnsupportedEncodingException {
        String data = "5 4 +\n";
        Evaluator evaluator = new Evaluator(new Evaluator.Lexer(new ByteArrayInputStream(data.getBytes("UTF-8"))));
        assertEquals(new Double(9),  evaluator.evaluate());
        data = "f = 5 4 -\n" +
                "f\n" +
                "f ~\n" +
                "4 8 2 * +\n" +
                "5 + 4\n" +
                "6 0 /\n" +
                "4 5 =\n";
        evaluator = new Evaluator(new Evaluator.Lexer(new ByteArrayInputStream(data.getBytes("UTF-8"))));
        assertEquals(new Double(1),  evaluator.evaluate());
        assertEquals(new Double(1),  evaluator.evaluate());
        assertEquals(new Double(-1),  evaluator.evaluate());
        assertEquals(new Double(20),  evaluator.evaluate());
        assertNull(evaluator.evaluate());
        assertNull(evaluator.evaluate());
        assertNull(evaluator.evaluate());
    }

    @org.junit.jupiter.api.Test
    void testLexer() throws UnsupportedEncodingException {
        String data = "5 + 4\n";
        Evaluator.Lexer lexer = new Evaluator.Lexer(new ByteArrayInputStream(data.getBytes("UTF-8")));
        assertEquals(Evaluator.Lexer.NUMBER, lexer.nextToken());
        assertEquals(Evaluator.Lexer.ADD_OP, lexer.nextToken());
        assertEquals(Evaluator.Lexer.NUMBER, lexer.nextToken());
        assertEquals(Evaluator.Lexer.EOL, lexer.nextToken());
        data = "= - ab5f/ ~5&\n";
        lexer = new Evaluator.Lexer(new ByteArrayInputStream(data.getBytes("UTF-8")));
        assertEquals(Evaluator.Lexer.ASSIGN_OP, lexer.nextToken());
        assertEquals(Evaluator.Lexer.SUBTRACT_OP, lexer.nextToken());
        assertEquals(Evaluator.Lexer.VARIABLE, lexer.nextToken());
        assertEquals("ab5f", lexer.getText());
        assertEquals(Evaluator.Lexer.DIVIDE_OP, lexer.nextToken());
        assertEquals(Evaluator.Lexer.MINUS_OP, lexer.nextToken());
        assertEquals(Evaluator.Lexer.NUMBER, lexer.nextToken());
        assertEquals("5", lexer.getText());
        assertEquals(Evaluator.Lexer.BAD_TOKEN, lexer.nextToken());
        assertEquals(Evaluator.Lexer.EOL, lexer.nextToken());
    }
}