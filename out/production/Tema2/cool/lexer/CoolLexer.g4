lexer grammar CoolLexer;

tokens { ERROR } 

@header{
package cool.lexer;
import java.util.HashMap;
}

@members{    
    private void raiseError(String msg) {
        setText(msg);
        setType(ERROR);
    }
}

WS
    :   [ \n\f\r\t]+ -> skip
    ;
CLASS : 'class';
IF : 'if';
THEN : 'then';
ELSE : 'else';
FI: 'fi';

BOOL : 'true' | 'false';

fragment LETTER : [a-zA-Z];
fragment DIGIT : [0-9];

TYPE : 'Int' | 'String' | 'Bool' | 'Object' | 'IO' | [A-Z](LETTER | '_' | DIGIT)*;

NOT : 'not';

NEW : 'new';

LET : 'let';
IN : 'in';

WHILE: 'while';
LOOP: 'loop';
POOL: 'pool';

CASE : 'case';
OF : 'of';
RESULTS : '=>';
ESAC : 'esac';

INHERITS : 'inherits';
AT : '@';
ISVOID : 'isvoid';
POINT: '.';


ID : [a-z](LETTER | '_' | DIGIT)*;
INT : DIGIT+;

STRING :
    '"' ('\\"' | '\\'NEW_LINE | .)*?
    (

        '"'     {
                    String text = getText();
                    HashMap<CharSequence, CharSequence> backslash_sequences = new HashMap<>() {{
                        put("\\n", "\n");
                    	put("\\b", "\b");
                        put("\\f", "\f");
                        put("\\t", "\t");
                    }};
                    text = text.substring(1, text.length() - 1);
                    for (var entry : backslash_sequences.entrySet()) {
                        text = text.replace(entry.getKey(), entry.getValue());
                    }
                    for (int i = 0; i < text.length(); i++) {
                        if (text.charAt(i) == '\\' && text.charAt(i + 1) != '\\') {
                            text = text.substring(0, i) + text.substring(i + 1);
                        }
                    }
                    if (text.length() >= 1024) {
                        raiseError("String constant too long");
                        return;
                    }
                    setText(text);
                }
        |    NEW_LINE         { raiseError("Unterminated string constant");}
        |   '\u0000'(.)*?'"'    { raiseError("String contains null character");}
        |   EOF                 { raiseError("EOF in string constant"); }
    ) ;
fragment NEW_LINE : '\r'? '\n';

LINE_COMMENT
    : '--' .*? (NEW_LINE | EOF) -> skip
    ;

SEMI : ';';

COLON : ':';

COMMA : ',';

ASSIGN : '<-';

EQUAL : '=';

LPAREN : '(';

RPAREN : ')';

LBRACE : '{';

RBRACE : '}';

PLUS : '+';

MINUS : '-';

MULT : '*';

DIV : '/';

LT : '<';

LE : '<=';

TILDA : '~';

BLOCK_COMMENT
    : '(*'
      (BLOCK_COMMENT | .)*?
      (
        EOF { raiseError("EOF in comment"); }
      | '*)' {skip();}
      )
    ;
END_BLOCK_COMMENT : '*)' {raiseError("Unmatched *)");};

INVALID_CHARACTER : . { raiseError("Invalid character: " + getText()); };