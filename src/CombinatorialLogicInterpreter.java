import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

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
    USER_COMBINATOR("c"),
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

abstract class Token implements Cloneable {
    DomainTag tag;
    Fragment coords;
    String attr;

    Token(String attr, DomainTag tag, Position starting, Position following) {
        this.attr = attr;
        this.tag = tag;
        this.coords = new Fragment(starting, following);
    }

    Token(Token token) {
        this.tag = token.tag;
        this.coords = new Fragment(token.coords.starting, token.coords.following);
        this.attr = token.attr;
    }

    abstract protected Token clone() throws CloneNotSupportedException;

    @Override
    public String toString() {
        return coords.toString() + ": " + attr;
    }
}

class LeftBracketToken extends Token implements Cloneable {
    LeftBracketToken(Position starting, Position following) {
        super("(", DomainTag.LEFT_BRACKET, starting, following);
    }

    LeftBracketToken(LeftBracketToken token) {
        super(token);
    }

    @Override
    protected Token clone() throws CloneNotSupportedException {
        return new LeftBracketToken(this);
    }

    @Override
    public String toString() {
        return "LEFT_BRACKET " + super.toString();
    }
}

class RightBracketToken extends Token implements Cloneable {
    RightBracketToken(Position starting, Position following) {
        super(")", DomainTag.RIGHT_BRACKET, starting, following);
    }

    RightBracketToken(RightBracketToken token) {
        super(token);
    }

    @Override
    protected Token clone() throws CloneNotSupportedException {
        return new RightBracketToken(this);
    }

    @Override
    public String toString() {
        return "RIGHT_BRACKET " + super.toString();
    }
}

class UserCombinatorToken extends Token implements Cloneable {
    UserCombinatorToken(String attr, Position starting, Position following) {
        super(attr, DomainTag.USER_COMBINATOR, starting, following);
    }

    UserCombinatorToken(UserCombinatorToken token) {
        super(token);
    }

    @Override
    protected Token clone() throws CloneNotSupportedException {
        return new UserCombinatorToken(this);
    }

    @Override
    public String toString() {
        return "USER_COMBINATOR " + super.toString();
    }
}

class InfinityCompToken extends Token implements Cloneable {
    InfinityCompToken(Position starting, Position following) {
        super("inf", DomainTag.INFINITY_COMP, starting, following);
    }

    InfinityCompToken(InfinityCompToken token) {
        super(token);
    }

    @Override
    protected Token clone() throws CloneNotSupportedException {
        return new InfinityCompToken(this);
    }

    @Override
    public String toString() {
        return "INFINITY_COMP " + super.toString();
    }
}

class ExponentCompToken extends Token implements Cloneable {
    ExponentCompToken(Position starting, Position following) {
        super("exp", DomainTag.EXPONENT_COMP, starting, following);
    }

    ExponentCompToken(ExponentCompToken token) {
        super(token);
    }

    @Override
    protected Token clone() throws CloneNotSupportedException {
        return new ExponentCompToken(this);
    }

    @Override
    public String toString() {
        return "EXPONENT_COMP " + super.toString();
    }
}

class QuadrateCompToken extends Token implements Cloneable {
    QuadrateCompToken(Position starting, Position following) {
        super("quad", DomainTag.QUADRATE_COMP, starting, following);
    }

    QuadrateCompToken(QuadrateCompToken token) {
        super(token);
    }

    @Override
    protected Token clone() throws CloneNotSupportedException {
        return new QuadrateCompToken(this);
    }

    @Override
    public String toString() {
        return "QUADRATE_COMP " + super.toString();
    }
}

class QuestionSignToken extends Token implements Cloneable {
    QuestionSignToken(Position starting, Position following) {
        super("?", DomainTag.QUESTION_SIGN, starting, following);
    }

    QuestionSignToken(QuestionSignToken token) {
        super(token);
    }

    @Override
    protected Token clone() throws CloneNotSupportedException {
        return new QuestionSignToken(this);
    }

    @Override
    public String toString() {
        return " QUESTION_SIGN " + super.toString();
    }
}

class EqualSignToken extends Token implements Cloneable {
    EqualSignToken(Position starting, Position following) {
        super("=", DomainTag.EQUAL_SIGN, starting, following);
    }

    EqualSignToken(EqualSignToken token) {
        super(token);
    }

    @Override
    protected Token clone() throws CloneNotSupportedException {
        return new EqualSignToken(this);
    }

    @Override
    public String toString() {
        return "EQUAL_SIGN " + super.toString();
    }
}

class KCombToken extends Token implements Cloneable {
    KCombToken(Position starting, Position following) {
        super("K", DomainTag.K_COMB, starting, following);
    }

    KCombToken(KCombToken token) {
        super(token);
    }

    @Override
    protected Token clone() throws CloneNotSupportedException {
        return new KCombToken(this);
    }

    @Override
    public String toString() {
        return "K_COMB " + super.toString();
    }
}

class SCombToken extends Token implements Cloneable {
    SCombToken(Position starting, Position following) {
        super("S", DomainTag.S_COMB, starting, following);
    }

    SCombToken(SCombToken token) {
        super(token);
    }

    @Override
    protected Token clone() throws CloneNotSupportedException {
        return new SCombToken(this);
    }

    @Override
    public String toString() {
        return "S_COMB " + super.toString();
    }
}

class ICombToken extends Token implements Cloneable {
    ICombToken(Position starting, Position following) {
        super("I", DomainTag.I_COMB, starting, following);
    }

    ICombToken(ICombToken token) {
        super(token);
    }

    @Override
    protected Token clone() throws CloneNotSupportedException {
        return new ICombToken(this);
    }

    @Override
    public String toString() {
        return "I_COMB " + super.toString();
    }
}

class ErrorToken extends Token implements Cloneable {
    ErrorToken(String attr, Position starting, Position following) {
        super(attr, DomainTag.ERROR, starting, following);
    }

    ErrorToken(ErrorToken token) {
        super(token);
    }

    @Override
    protected Token clone() throws CloneNotSupportedException {
        return new ErrorToken(this);
    }

    @Override
    public String toString() {
        return attr;
    }
}

class EndOfProgramToken extends Token implements Cloneable {
    EndOfProgramToken(String attr, DomainTag tag, Position starting, Position following) {
        super(attr, tag, starting, following);
        assert (tag == DomainTag.END_OF_PROGRAM);
    }

    EndOfProgramToken(EndOfProgramToken token) {
        super(token);
    }

    @Override
    protected Token clone() throws CloneNotSupportedException {
        return new EndOfProgramToken(this);
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
                case '(' -> new LeftBracketToken(cur, cur.next());
                case ')' -> new RightBracketToken(cur, cur.next());
                case '?' -> new QuestionSignToken(cur, cur.next());
                case '=' -> new EqualSignToken(cur, cur.next());
                case 'i' -> new InfinityCompToken(cur, cur.next().next().next());
                case 'e' -> new ExponentCompToken(cur, cur.next().next().next());
                case 'q' -> new QuadrateCompToken(cur, cur.next().next().next().next());
                case 'K' -> new KCombToken(cur, cur.next());
                case 'S' -> new SCombToken(cur, cur.next());
                case 'I' -> new ICombToken(cur, cur.next());
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
            return new UserCombinatorToken(sb.toString(), cur, p);
        } else {
            compiler.addMessage(true, cur, "Wrong user_combinator: " + sb.toString());
            return new ErrorToken(sb.toString(), cur, p);
        }
    }
}

class Compiler {
    private final ArrayList<Message> messages;

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
            if (token.tag == DomainTag.USER_COMBINATOR) {
                CombinatorialLogicInterpreter.seq_lexemes.append(isAfterQuestion ? "" : "\n");
            } else if (token.tag == DomainTag.QUESTION_SIGN) {
                isAfterQuestion = true;
                CombinatorialLogicInterpreter.seq_lexemes.append("\n");
            } else if (token.tag == DomainTag.EQUAL_SIGN) {
                CombinatorialLogicInterpreter.seq_lexemes.append(" ");
            }

            CombinatorialLogicInterpreter.seq_lexemes.append(token.tag.text);

            if (token.tag == DomainTag.QUESTION_SIGN || token.tag == DomainTag.EQUAL_SIGN) {
                CombinatorialLogicInterpreter.seq_lexemes.append(" ");
            }

            CombinatorialLogicInterpreter.tokens.add(token);
            if (token.tag == DomainTag.END_OF_PROGRAM) {
                break;
            }
        }
        compiler.outputMessages();
    }
}

class AnonComb extends Token implements Cloneable {
    ArrayList<Token> tokensInBrackets = new ArrayList<>();

    AnonComb(Position starting) {
        super("", DomainTag.USER_COMBINATOR, starting, starting);
    }

    AnonComb(AnonComb token) throws CloneNotSupportedException {
        super(token);
        for (Token t : token.tokensInBrackets) {
            this.tokensInBrackets.add(t.clone());
        }
    }

    @Override
    protected Token clone() throws CloneNotSupportedException {
        return new AnonComb(this);
    }

    void addToken(Token token) {
        if (tokensInBrackets.size() == 0)
            coords.following = token.coords.starting;
        coords.following = token.coords.following;
        tokensInBrackets.add(token);
        if (token.tag == DomainTag.USER_COMBINATOR) {
            attr += "(" + token.attr + ")";
        } else
            attr += token.attr;
    }
}

public class CombinatorialLogicInterpreter {
    static ArrayList<Token> tokens = new ArrayList<>();
    static StringBuilder seq_lexemes = new StringBuilder();

    int numberOfCurrentToken;
    Token currentToken;

    private final HashMap<String, AnonComb> userCombs = new HashMap<>();
    private final ArrayList<Token> taskTokens = new ArrayList<>();
    private int infOrExpOrQuadComp = 0;
    private int maxNumberOfInterpretations = 0;
    private int numberOfBasicCombsInTask = 0;

    private int numberOfInterpretations = 0;

    public static void main(String[] args) throws CloneNotSupportedException {
        Lexer l = new Lexer();
        l.lex(args[0]);
        System.out.println(seq_lexemes + "\n");
        CombinatorialLogicInterpreter interpreter = new CombinatorialLogicInterpreter();
        interpreter.parse();
        System.out.println("Max number of interpretations: " + ((interpreter.infOrExpOrQuadComp != 0) ? interpreter.maxNumberOfInterpretations : "infinity"));
        System.out.println("Start task: " + interpreter.printTree(interpreter.taskTokens) + "\n");
        ArrayList<Token> normCombs = interpreter.interpret(interpreter.taskTokens);
        System.out.println("\nNumber of interpretations: " + interpreter.numberOfInterpretations);
        System.out.println("Result task: " + interpreter.printTree(normCombs));
    }

    public String printTree(ArrayList<Token> tokens) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Token token : tokens) {
            if (token.tag == DomainTag.USER_COMBINATOR) {
                stringBuilder.append("(").append(printTree(((AnonComb) token).tokensInBrackets)).append(")");
            } else
                stringBuilder.append(token.attr);
        }

        return stringBuilder.toString();
    }

    public ArrayList<Token> interpret(ArrayList<Token> tokens) throws CloneNotSupportedException {
        int numberOfCombs;

        while ((infOrExpOrQuadComp == 0) || (numberOfInterpretations < maxNumberOfInterpretations)) {
            numberOfCombs = tokens.size();

            Token firstComb = tokens.get(0);
            if (firstComb.tag == DomainTag.I_COMB) {
                if (numberOfCombs > 1) {
                    System.out.print(numberOfInterpretations + 1 + ") " + printTree(tokens));
                    tokens.remove(0);

                    numberOfInterpretations++;
                    System.out.println(" -> " + printTree(tokens));
                } else
                    break;
            } else if (firstComb.tag == DomainTag.K_COMB) {
                if (numberOfCombs > 2) {
                    System.out.print(numberOfInterpretations + 1 + ") " + printTree(tokens));
                    tokens.remove(2);
                    tokens.remove(0);

                    numberOfInterpretations++;
                    System.out.println(" -> " + printTree(tokens));
                } else
                    break;
            } else if (firstComb.tag == DomainTag.S_COMB) {
                if (numberOfCombs > 3) {
                    System.out.print(numberOfInterpretations + 1 + ") " + printTree(tokens));
                    Token tempToken2 = tokens.get(2);
                    Token tempToken3 = tokens.get(3);

                    AnonComb tempAnonComb = new AnonComb(tempToken2.coords.starting);
                    tempAnonComb.addToken(tempToken2);
                    tempAnonComb.addToken(tempToken3.clone());

                    tokens.set(2, tempToken3);
                    tokens.set(3, tempAnonComb);
                    tokens.remove(0);

                    numberOfInterpretations++;
                    System.out.println(" -> " + printTree(tokens));
                } else
                    break;
            } else {
                AnonComb token = (AnonComb) tokens.remove(0);
                ArrayList<Token> combsList = token.tokensInBrackets; // interpret(token.tokensInBrackets);
                combsList.addAll(tokens);
                tokens = combsList;
            }
        }

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.tag == DomainTag.USER_COMBINATOR) {
                AnonComb anonComb = (AnonComb) token;
                interpret(anonComb.tokensInBrackets);
                getSoloCombsFromBrackets(anonComb, tokens, i);
            }
        }

        return tokens;
    }

    private void getSoloCombsFromBrackets(AnonComb anonComb, ArrayList<Token> setInTokens, int i) throws CloneNotSupportedException {
        if (anonComb.tokensInBrackets.size() == 1) {
            Token childToken = anonComb.tokensInBrackets.get(0);
            if (childToken.tag == DomainTag.USER_COMBINATOR) {
                getSoloCombsFromBrackets((AnonComb) childToken, setInTokens, i);
            } else {
                setInTokens.set(i, childToken.clone());
            }
        }
    }

    private void nextTok() {
        currentToken = tokens.get(numberOfCurrentToken);
        numberOfCurrentToken++;
    }

    public void parse() throws CloneNotSupportedException {
        numberOfCurrentToken = 1;
        currentToken = tokens.get(0);
        parseProg();
        if (infOrExpOrQuadComp == 1)
            maxNumberOfInterpretations = (int) Math.pow(2, numberOfBasicCombsInTask + 1);
        else if (infOrExpOrQuadComp == 2)
            maxNumberOfInterpretations = (numberOfBasicCombsInTask + 1) * (numberOfBasicCombsInTask + 1);
    }

    //Prog = Comp {Rule} '?' Task
    private void parseProg() throws CloneNotSupportedException {
        parseComp();
        while (currentToken.tag == DomainTag.USER_COMBINATOR) {
            parseRule();
        }
        if (currentToken.tag == DomainTag.QUESTION_SIGN) {
            nextTok();
            parseTask();
            if (currentToken.tag != DomainTag.END_OF_PROGRAM)
                endProgram("expected end_of_program");
        } else
            endProgram("expected question_sign");
    }

    //Comp = "inf" | "exp" | "quad"
    private void parseComp() {
        if (currentToken.tag == DomainTag.INFINITY_COMP || currentToken.tag == DomainTag.EXPONENT_COMP ||
                currentToken.tag == DomainTag.QUADRATE_COMP) {
            if (currentToken.tag == DomainTag.EXPONENT_COMP) {
                infOrExpOrQuadComp = 1;
            } else if (currentToken.tag == DomainTag.QUADRATE_COMP) {
                infOrExpOrQuadComp = 2;
            }
            nextTok();
        } else
            endProgram("expected complexity");
    }

    //Rule = c '=' BasicCombsInAndOutBrackets {BasicCombsInAndOutBrackets}
    private void parseRule() throws CloneNotSupportedException {
        if (currentToken.tag == DomainTag.USER_COMBINATOR) {
            String userCombName = currentToken.attr;
            nextTok();
            if (currentToken.tag == DomainTag.EQUAL_SIGN) {
                nextTok();

                AnonComb anonComb = new AnonComb(currentToken.coords.starting);
                Token comb;

                comb = parseBasicCombsInAndOutBrackets();
                anonComb.addToken(comb);

                while (currentToken.tag == DomainTag.LEFT_BRACKET || currentToken.tag == DomainTag.K_COMB ||
                        currentToken.tag == DomainTag.S_COMB || currentToken.tag == DomainTag.I_COMB) {
                    comb = parseBasicCombsInAndOutBrackets();
                    anonComb.addToken(comb);
                }

                userCombs.put(userCombName, anonComb);
            } else
                endProgram("expected equal_sign");
        } else
            endProgram("expected user_combinator");
    }

    //BasicCombsInAndOutBrackets = BasicComb | BasicCombsInBrackets
    private Token parseBasicCombsInAndOutBrackets() throws CloneNotSupportedException {
        Token comb = null;

        if (currentToken.tag == DomainTag.LEFT_BRACKET) {
            comb = parseBasicCombsInBrackets();
        } else if (currentToken.tag == DomainTag.K_COMB || currentToken.tag == DomainTag.S_COMB ||
                currentToken.tag == DomainTag.I_COMB) {
            comb = parseBasicComb();
        } else
            endProgram("expected left_bracket or one of K, S, I combs");

        return comb;
    }

    //BasicCombsInBrackets = '(' BasicCombsInAndOutBrackets {BasicCombsInAndOutBrackets} ')'
    private Token parseBasicCombsInBrackets() throws CloneNotSupportedException {
        AnonComb anonComb = new AnonComb(currentToken.coords.starting);
        Token comb;

        if (currentToken.tag == DomainTag.LEFT_BRACKET) {
            nextTok();

            comb = parseBasicCombsInAndOutBrackets();
            anonComb.addToken(comb);

            while (currentToken.tag == DomainTag.LEFT_BRACKET || currentToken.tag == DomainTag.K_COMB ||
                    currentToken.tag == DomainTag.S_COMB || currentToken.tag == DomainTag.I_COMB) {
                comb = parseBasicCombsInAndOutBrackets();
                anonComb.addToken(comb);
            }

            if (currentToken.tag == DomainTag.RIGHT_BRACKET) {
                nextTok();
            } else
                endProgram("expected right_bracket");
        } else
            endProgram("expected left_bracket");

        return anonComb;
    }

    //BasicComb = 'K' | 'S' | 'I'
    private Token parseBasicComb() throws CloneNotSupportedException {
        Token token = currentToken;

        if (currentToken.tag == DomainTag.K_COMB || currentToken.tag == DomainTag.S_COMB ||
                currentToken.tag == DomainTag.I_COMB) {
            token = currentToken.clone();
            numberOfBasicCombsInTask++;
            nextTok();
        } else
            endProgram("expected one of K, S, I combs");

        return token;
    }

    //Task = CombsInAndOutBrackets {CombsInAndOutBrackets}
    private void parseTask() throws CloneNotSupportedException {
        Token comb;

        comb = parseCombsInAndOutBrackets();
        taskTokens.add(comb);

        while (currentToken.tag == DomainTag.LEFT_BRACKET || currentToken.tag == DomainTag.K_COMB ||
                currentToken.tag == DomainTag.S_COMB || currentToken.tag == DomainTag.I_COMB ||
                currentToken.tag == DomainTag.USER_COMBINATOR) {
            comb = parseCombsInAndOutBrackets();
            taskTokens.add(comb);
        }
    }

    //CombsInAndOutBrackets = Comb | CombsInBrackets
    private Token parseCombsInAndOutBrackets() throws CloneNotSupportedException {
        Token comb = null;

        if (currentToken.tag == DomainTag.LEFT_BRACKET) {
            comb = parseCombsInBrackets();
        } else if (currentToken.tag == DomainTag.K_COMB || currentToken.tag == DomainTag.S_COMB ||
                currentToken.tag == DomainTag.I_COMB || currentToken.tag == DomainTag.USER_COMBINATOR) {
            comb = parseComb();
        } else
            endProgram("expected left_bracket or one of K, S, I combs or another user_combinator");

        return comb;
    }

    //CombsInBrackets = '(' CombsInAndOutBrackets {CombsInAndOutBrackets} ')'
    private Token parseCombsInBrackets() throws CloneNotSupportedException {
        AnonComb anonComb = new AnonComb(currentToken.coords.starting);
        Token comb;

        if (currentToken.tag == DomainTag.LEFT_BRACKET) {
            nextTok();

            comb = parseCombsInAndOutBrackets();
            anonComb.addToken(comb);

            while (currentToken.tag == DomainTag.LEFT_BRACKET || currentToken.tag == DomainTag.K_COMB ||
                    currentToken.tag == DomainTag.S_COMB || currentToken.tag == DomainTag.I_COMB ||
                    currentToken.tag == DomainTag.USER_COMBINATOR) {
                comb = parseCombsInAndOutBrackets();
                anonComb.addToken(comb);
            }

            if (currentToken.tag == DomainTag.RIGHT_BRACKET) {
                nextTok();
            } else
                endProgram("expected right_bracket");
        } else
            endProgram("expected left_bracket");

        return anonComb;
    }

    //Comb = BasicComb | c
    private Token parseComb() throws CloneNotSupportedException {
        Token token = currentToken;

        if (currentToken.tag == DomainTag.K_COMB || currentToken.tag == DomainTag.S_COMB ||
                currentToken.tag == DomainTag.I_COMB || currentToken.tag == DomainTag.USER_COMBINATOR) {
            if (currentToken.tag == DomainTag.USER_COMBINATOR) {
                token = userCombs.get(currentToken.attr).clone();
            } else {
                token = currentToken.clone();
                numberOfBasicCombsInTask++;
            }
            nextTok();
        } else
            endProgram("expected one of K, S, I combs or another user_combinator");

        return token;
    }

    private void endProgram(String mes) {
        System.out.println("ERROR" + currentToken.coords + ": " + mes);
        System.exit(1);
    }
}
