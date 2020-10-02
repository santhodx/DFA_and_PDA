import java.io.InputStream;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Simulating a PDA to evaluate a series of postfix expressions provided by a lexer.
 * The constructor argument is the lexer of type Lexer. A single line is evaluated and its
 * value is printed. Expression values can also be assigned to variables for later use.
 * If no variable is explicitly assigned, then the default variable "it" is assigned
 * the value of the most recently evaluated expression.
 *
 * @author Devdutt Santhosh
 */
public class Evaluator {

   /**
    * Running the desk calculator.
    */
   public static void main(String[] args) {
      Evaluator evaluator = new Evaluator(new Lexer(System.in));
      evaluator.run();
   }

   private Lexer lexer; // providing a stream of tokens
   private LinkedList<Double> stack; // operands
   private HashMap<String, Double> symbols; // symbol table for variables
   private String target; // variable assigned the latest expression value

   public Evaluator(Lexer lexer) {
      this.lexer = lexer;
      stack = new LinkedList<>();
      symbols = new HashMap<>();
      target = "it";
   }

   /**
    * Evaluating a single line of input, which is a complete expression
    * optionally assigned to a variable; if no variable is assigned to, then
    * the result is assigned to "it". In any case, returning the value of the
    * expression, or "no value" if there was some sort of error.
    */
   public Double evaluate() {
       stack.clear();
       int curToken = lexer.nextToken();
       if (curToken == Lexer.VARIABLE) {
           String variable = lexer.getText();
           if (variable.equals("exit")) {
               System.exit(0);
           }
           curToken = lexer.nextToken();
           if (curToken == Lexer.ASSIGN_OP) {
               target = variable;
           } else {
               Double number = symbols.get(variable);
               if (number == null) {
                   error("the list does not contains this variable");
                   target = "no value";
                   lexer.flush();
                   return null;
               }
               if (curToken == Lexer.EOL) {
                   lexer.flush();
                   return number;
               } else {
                   lexer.unread();
                   stack.add(number);
               }
           }
       } else lexer.unread();
       Double res = evaluateExpression();
       if (res != null) {
           symbols.put(target, res);
       }
       lexer.flush();
       return res;

   } // evaluate

    /**
     * Evaluating expression and returning result of expression or null if the result is not possible to calculate
     */
    private Double evaluateExpression() {
        int curToken;
        while ((curToken = lexer.nextToken()) != Lexer.EOL) {
            if (curToken == Lexer.NUMBER) {
                stack.add(Double.parseDouble(lexer.getText()));
            } else if (curToken == Lexer.VARIABLE) {
                Double number = symbols.get(lexer.getText());
                if (number == null) error("the map does not contains this variable");
                stack.add(number);
            } else if (isBinaryOp(curToken)) {
                if (stack.size() < 2) {
                    error("not enough numbers in the stack");
                    return null;
                }
                Double n1 = stack.removeLast();
                Double n2 = stack.removeLast();
                //apply a binary operator
                switch (curToken) {
                    case Lexer.ADD_OP:
                        stack.add(n2 + n1);
                        break;
                    case Lexer.SUBTRACT_OP:
                        stack.add(n2 - n1);
                        break;
                    case Lexer.DIVIDE_OP:
                        if (n1 == 0) {
                            error("you can not divide by zero");
                            return null;
                        }
                        stack.add(n2 / n1);
                        break;
                    case Lexer.MULTIPLY_OP:
                        stack.add(n2 * n1);
                        break;
                }
                ////applying a unary operator
            } else if (isUnaryOp(curToken)) {
                if (stack.size() < 1) {
                    error("not enough numbers in the stack");
                    return null;
                }
                stack.add(-stack.removeLast());
            } else {
                error("Bad token");
                return null;
            }
        }
        return stack.removeLast();
    }

    /**
     * checking whether the token is a unary operator
     */
    private boolean isUnaryOp(int token) {
        return token == Lexer.MINUS_OP;
    }

    /**
     * checking whether the token is a binary operator
     */
    private boolean isBinaryOp(int token) {
        return token == Lexer.ADD_OP || token == Lexer.SUBTRACT_OP || token == Lexer.MULTIPLY_OP || token == Lexer.DIVIDE_OP;
    }

   /**
    * Running evaluate on each line of input and print the result forever.
    */
   public void run() {
      while (true) {
         Double value = evaluate();
         if (value == null)
            System.out.println("no value");
         else
            System.out.println(value);
      }
   }

   /**
    * Printing an error message, display the offending line with the current
    * location marked, and flushing the lexer in preparation for the next line.
    *
    * @param msg what to print as an error indication
    */
   private void error(String msg) {
      System.out.println(msg);
      String line = lexer.getCurrentLine();
      int index = lexer.getCurrentChar();
      System.out.print(line);
      for (int i = 1; i < index; i++) System.out.print(' ');
      System.out.println("^");
      lexer.flush();
   }

   ////////////////////////////////
   ///////// Lexer Class //////////

   /**
   * Reading terminal input and converting it to a token type, and also recording the text
   * of each token. Whitespace is skipped. The input comes from stdin, and each line
   * is prompted for.
   */
   public static class Lexer {

      // language token codes
      public static final int ADD_OP      = 3;
      public static final int SUBTRACT_OP = 4;
      public static final int MULTIPLY_OP = 5;
      public static final int DIVIDE_OP   = 6;
      public static final int MINUS_OP    = 7;
      public static final int ASSIGN_OP   = 8;
      public static final int EOL         = 9;
      public static final int NUMBER      = 11;
      public static final int VARIABLE    = 12;
      public static final int BAD_TOKEN   = 100;

      private Scanner input;     // for reading lines from stdin
      private String line;       // next input line
      private int index;         // current character in this line
      private String text;       // text of the current token

      public Lexer(InputStream in) {
         input = new Scanner(in);
         line = "";
         index = 0;
         text = "";
      }

      /**
       * Fetching the next character from the terminal. If the current line is
       * exhausted, then will prompt the user and wait for input. If end-of-file occurs,
       * then will exit the program.
       */
      private char nextChar() {
         if (index == line.length()) {
            System.out.print(">> ");
            if (input.hasNextLine()) {
               line = input.nextLine() + "\n";
               index = 0;
            } else {
               System.out.println("\nBye");
               System.exit(0);
            }
         }
         char ch = line.charAt(index);
         index++;
         return ch;
      }

      /**
       * Putting the last character back on the input line.
       */
      private void unread() { index -= 1; }

      /**
       * Returning the next token from the terminal.
       */
      public int nextToken() {
          char nc = nextChar();
          while (nc == ' ') {
              nc = nextChar();
          }
          if (Character.isDigit(nc)) {
              String res = nc + "";
              while (Character.isDigit(nc = nextChar())) {
                  res += nc;
              }
              if (nc == '.') {
                  res += nc;
                  while (Character.isDigit(nc = nextChar())) {
                      res += nc;
                  }
              }
              unread();
              text = res;
              return NUMBER;
          } else if (Character.isLetter(nc)) {
              String res = nc + "";
              while (Character.isLetterOrDigit(nc = nextChar())) {
                  res += nc;
              }
              unread();
              text = res;
              return VARIABLE;
          } else {
              switch (nc) {
                  case '+':
                      return ADD_OP;
                  case '-':
                      return SUBTRACT_OP;
                  case '*':
                      return MULTIPLY_OP;
                  case '/':
                      return DIVIDE_OP;
                  case '~':
                      return MINUS_OP;
                  case '=':
                      return ASSIGN_OP;
                  case '\n':
                      return EOL;
              }
          }

          return BAD_TOKEN;

      } // nextToken

      /**
       * Returning the current line for error messages.
       */
      public String getCurrentLine() { return line; }

      /**
       * Returning the current character index for error messages.
       */
      public int getCurrentChar() { return index; }

      /**
       * /** Returning the text of the current token.
       */
      public String getText() { return text; }

      /**
       * Clearing the current line after an error
       */
      public void flush() { index = line.length(); }

   } // Lexer

} // Evaluator
