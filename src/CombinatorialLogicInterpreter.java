import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class Position {
    String text;
    int line, pos, index;

    int getLine() {
        return line;
    }

    int getPos() {
        return pos;
    }

    int getIndex() {
        return index;
    }

    String getText() {
        return text;
    }

    Position(String text) {
        this.text = text;
        line = pos = 1;
        index = 0;
    }

    Position(Position p) {
        this.text = p.getText();
        this.line = p.getLine();
        this.pos = p.getPos();
        this.index = p.getIndex();
    }

    @Override
    public String toString() {
        return "(" + line + "," + pos + ")";
    }

    boolean isEOF() {
        return index == text.length();
    }

    int getCode() {
        return isEOF() ? -1 : text.codePointAt(index);
    }

    boolean isWhitespace() {
        return !isEOF() && Character.isWhitespace(getCode());
    }

    boolean isDigit() {
        return !isEOF() && String.valueOf(text.charAt(index)).matches("[0-9]");
    }

    /*
    boolean isBracket() {
        return !isEOF() && (text.charAt(index) == '(' || text.charAt(index) == ')');
    }

    boolean isCapLetter() {
        return !isEOF() && String.valueOf(text.charAt(index)).matches("[A-Z]");
    }

    boolean isDigit() {
        return !isEOF() && String.valueOf(text.charAt(index)).matches("[0-9]");
    }

    boolean isSymbol() {
        return !isEOF() && text.charAt(index) != '<' && text.charAt(index) != '>' && text.charAt(index) != '{' && text.charAt(index) != '}' && !isCapLetter();
    }*/

    boolean isNewLine() {
        if (isEOF()) {
            return true;
        }

        if (text.charAt(index) == '\r' && index + 1 < text.length()) {
            return (text.charAt(index + 1) == '\n');
        }

        return (text.charAt(index) == '\n');
    }

    Position next() {
        Position p = new Position(this);
        if (!p.isEOF()) {
            if (p.isNewLine()) {
                if (p.text.charAt(p.index) == '\r')
                    p.index++;
                p.line++;
                p.pos = 1;
            } else {
                if (Character.isHighSurrogate(p.text.charAt(p.index)))
                    p.index++;
                p.pos++;
            }
            p.index++;
        }
        return p;
    }
}

class Fragment {
    Position starting;
    Position following;

    Fragment(Position starting, Position following) {
        this.starting = starting;
        this.following = following;
    }

    public String toString() {
        return starting.toString() + "-" + following.toString();
    }
}

class Message {
    boolean isError;
    String text;
    Position coord;

    Message(boolean isError, String text, Position coord) {
        this.isError = isError;
        this.text = text;
        this.coord = coord;
    }
}

enum DomainTag {
    LEFT_BRACKET("("),
    RIGHT_BRACKET(")"),
    COMBINATOR("comb"),
    INFINITY_COMP("inf"),
    EXPONENT_COMP("exp"),
    QUADRATE_COMP("quad"),
    QUESTION_SIGN("?"),
    EQUAL_SIGN("="),
    K_COMB("K"),
    S_COMB("S"),
    I_COMB("I"),
    ERROR("ERROR"),
    END_OF_PROGRAM("$");

    String text;

    DomainTag(String text) {
        this.text = text;
    }
}

abstract class Token {
    DomainTag tag;
    Fragment coords;
    String attr;

    Token(String attr, DomainTag tag, Position starting, Position following) {
        this.attr = attr;
        this.tag = tag;
        this.coords = new Fragment(starting, following);
    }

    @Override
    public String toString() {
        return coords.toString() + ": " + attr;
    }
}

class LeftBracketToken extends Token {
    LeftBracketToken(String attr, Position starting, Position following) {
        super(attr, DomainTag.LEFT_BRACKET, starting, following);
    }

    @Override
    public String toString() {
        return "LEFT_BRACKET " + super.toString();
    }
}

class RightBracketToken extends Token {
    RightBracketToken(String attr, Position starting, Position following) {
        super(attr, DomainTag.RIGHT_BRACKET, starting, following);
    }

    @Override
    public String toString() {
        return "RIGHT_BRACKET " + super.toString();
    }
}

class CombinatorToken extends Token {
    CombinatorToken(String attr, Position starting, Position following) {
        super(attr, DomainTag.COMBINATOR, starting, following);
    }

    @Override
    public String toString() {
        return "COMBINATOR " + super.toString();
    }
}

class InfinityCompToken extends Token {
    InfinityCompToken(String attr, Position starting, Position following) {
        super(attr, DomainTag.INFINITY_COMP, starting, following);
    }

    @Override
    public String toString() {
        return "INFINITY_COMP " + super.toString();
    }
}

class ExponentCompToken extends Token {
    ExponentCompToken(String attr, Position starting, Position following) {
        super(attr, DomainTag.EXPONENT_COMP, starting, following);
    }

    @Override
    public String toString() {
        return "EXPONENT_COMP " + super.toString();
    }
}

class QuadrateCompToken extends Token {
    QuadrateCompToken(String attr, Position starting, Position following) {
        super(attr, DomainTag.QUADRATE_COMP, starting, following);
    }

    @Override
    public String toString() {
        return "QUADRATE_COMP " + super.toString();
    }
}

class QuestionSignToken extends Token {
    QuestionSignToken(String attr, Position starting, Position following) {
        super(attr, DomainTag.QUESTION_SIGN, starting, following);
    }

    @Override
    public String toString() {
        return " QUESTION_SIGN " + super.toString();
    }
}

class EqualSignToken extends Token {
    EqualSignToken(String attr, Position starting, Position following) {
        super(attr, DomainTag.EQUAL_SIGN, starting, following);
    }

    @Override
    public String toString() {
        return "EQUAL_SIGN " + super.toString();
    }
}

class KCombToken extends Token {
    KCombToken(String attr, Position starting, Position following) {
        super(attr, DomainTag.K_COMB, starting, following);
    }

    @Override
    public String toString() {
        return "K_COMB " + super.toString();
    }
}

class SCombToken extends Token {
    SCombToken(String attr, Position starting, Position following) {
        super(attr, DomainTag.S_COMB, starting, following);
    }

    @Override
    public String toString() {
        return "S_COMB " + super.toString();
    }
}

class ICombToken extends Token {
    ICombToken(String attr, Position starting, Position following) {
        super(attr, DomainTag.I_COMB, starting, following);
    }

    @Override
    public String toString() {
        return "I_COMB " + super.toString();
    }
}

class ErrorToken extends Token {
    ErrorToken(String attr, Position starting, Position following) {
        super(attr, DomainTag.ERROR, starting, following);
    }

    @Override
    public String toString() {
        return attr;
    }
}

class EndOfProgramToken extends Token {
    EndOfProgramToken(String attr, DomainTag tag, Position starting, Position following) {
        super(attr, tag, starting, following);
        assert (tag == DomainTag.END_OF_PROGRAM);
    }

    @Override
    public String toString() {
        return "END_OF_PROGRAM " + super.toString();
    }
}

class Scanner {
    public final String program;
    private final Compiler compiler;
    private Position cur;

    public Scanner(String program, Compiler compiler) {
        this.compiler = compiler;
        this.program = program;
        cur = new Position(program);
    }

    public Token nextToken() {
        while (!cur.isEOF()) {
            while (cur.isWhitespace())
                cur = cur.next();
            Token token = switch (cur.getCode()) {
                case '(' -> new LeftBracketToken("(", cur, cur.next());
                case ')' -> new RightBracketToken(")", cur, cur.next());
                case '?' -> new QuestionSignToken("?", cur, cur.next());
                case '=' -> new EqualSignToken("=", cur, cur.next());
                case 'i' -> new InfinityCompToken("i", cur, cur.next().next().next());
                case 'e' -> new ExponentCompToken("e", cur, cur.next().next().next());
                case 'q' -> new QuadrateCompToken("q", cur, cur.next().next().next().next());
                case 'K' -> new KCombToken("K", cur, cur.next());
                case 'S' -> new SCombToken("S", cur, cur.next());
                case 'I' -> new ICombToken("I", cur, cur.next());
                case 'C' -> readComb(cur);
                default -> {
                    compiler.addMessage(true, cur, "Unexpected symbol: " + cur.text.charAt(cur.index));
                    yield new ErrorToken(String.valueOf(cur.text.charAt(cur.index)), cur, cur.next());
                }
            };
            cur = token.coords.following;
            if (token.tag != DomainTag.ERROR) {
                return token;
            }
        }
        return new EndOfProgramToken("", DomainTag.END_OF_PROGRAM, cur, cur);
    }

    private Token readComb(Position cur) {
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toChars(cur.getCode()));
        Position p = cur.next();

        if (p.isDigit()) {
            sb.append(Character.toChars(p.getCode()));
            p = p.next();
            while (p.isDigit()) {
                sb.append(Character.toChars(p.getCode()));
                p = p.next();
            }
            return new CombinatorToken(sb.toString(), cur, p);
        } else {
            compiler.addMessage(true, cur, "Wrong combinator: " + sb.toString());
            return new ErrorToken(sb.toString(), cur, p);
        }
    }
}

class Compiler {
    private final List<Message> messages;

    public Compiler() {
        messages = new ArrayList<>();
    }

    public void addMessage(boolean isErr, Position c, String text) {
        messages.add(new Message(isErr, text, c));
    }

    public void outputMessages() {
        for (Message m : messages) {
            System.out.print(m.isError ? "Error" : "Warning");
            System.out.print(" " + m.coord + ": ");
            System.out.println(m.text);
        }
    }

    public Scanner getScanner(String program) {
        return new Scanner(program, this);
    }
}

class Lexer {
    void lex(String arg) {
        Compiler compiler = new Compiler();
        String program = null;
        try {
            program = new String(Files.readAllBytes(Paths.get(arg)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scanner scanner = compiler.getScanner(program);
        boolean isAfterQuestion = false;
        while (true) {
            Token token = scanner.nextToken();
            if (token.tag == DomainTag.COMBINATOR) {
                CombinatorialLogicInterpreter.seq_lexems.append(isAfterQuestion ? " " : "\n");
            } else if (token.tag == DomainTag.QUESTION_SIGN) {
                isAfterQuestion = true;
                CombinatorialLogicInterpreter.seq_lexems.append("\n");
            } else if (token.tag == DomainTag.EQUAL_SIGN) {
                CombinatorialLogicInterpreter.seq_lexems.append(" ");
            }

            CombinatorialLogicInterpreter.seq_lexems.append(token.tag.text);

            if (token.tag == DomainTag.QUESTION_SIGN || token.tag == DomainTag.EQUAL_SIGN) {
                CombinatorialLogicInterpreter.seq_lexems.append(" ");
            }

            CombinatorialLogicInterpreter.tokens.add(token);
            if (token.tag == DomainTag.END_OF_PROGRAM) {
                break;
            }
        }
        compiler.outputMessages();
    }
}

public class CombinatorialLogicInterpreter {
    static ArrayList<Token> tokens = new ArrayList<>();
    static StringBuilder seq_lexems = new StringBuilder();

    public static void main(String[] args) {
        Lexer l = new Lexer();
        l.lex(args[0]);
        System.out.println(seq_lexems);
        //CombinatorialLogicInterpreter interpreter = new CombinatorialLogicInterpreter();
        //interpreter.interpret();
    }
}
