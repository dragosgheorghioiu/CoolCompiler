parser grammar CoolParser;

options {
    tokenVocab = CoolLexer;
}

@header{
    package cool.parser;
}

program : (classes+=class+) EOF;

class : CLASS name=TYPE (INHERITS parent=TYPE)? LBRACE ((features+=feature)SEMI)* RBRACE SEMI;

feature : attribute | method;

attribute : name=ID COLON type=TYPE (ASSIGN value=expr)?;

method : name=ID LPAREN (formals+=formal (COMMA formals+=formal)*)? RPAREN COLON type=TYPE LBRACE method_body=expr RBRACE;

formal : name=ID COLON type=TYPE;

case_branch: temp_name=ID COLON new_type=TYPE RESULTS body=expr SEMI;

expr
    : NEW name=TYPE                                                                                                                     # new
    | method_name=ID LPAREN (parameters+=expr (COMMA parameters+= expr)*)? RPAREN                                                       # selfDispatch
    | object=expr (AT parent=TYPE)? POINT method_name=ID LPAREN (parameters+=expr (COMMA parameters+= expr)*)? RPAREN                   # explicitDispatch
    | TILDA expr                                                                                                                        # unaryMinus
    | left=expr op=(MULT | DIV) right=expr                                                                                              # mulDiv
    | left=expr op=(PLUS | MINUS) right=expr                                                                                            # addSub
    | left=expr op=(LT | LE | EQUAL) right=expr                                                                                         # relational
    | NOT expr                                                                                                                          # not
    | LET (local_vars+=attribute (COMMA local_vars+=attribute)*)? IN body=expr                                                          # let
    | CASE checked_expr=expr OF branches+=case_branch+ ESAC                                                                             # case
    | WHILE cond=expr LOOP body=expr POOL                                                                                               # while
    | LET (local_vars+=attribute (COMMA local_vars+=attribute)*) IN body=expr                                                           # let
    | IF cond=expr THEN thenBranch=expr ELSE elseBranch=expr FI                                                                         # if
    | name=ID ASSIGN e=expr                                                                                                             # assign
    | LPAREN e=expr RPAREN                                                                                                              # paren
    | LBRACE (exprs+=expr | (exprs+=expr SEMI (exprs+=expr SEMI)+)) RBRACE                                                              # block
    | ISVOID expr                                                                                                                       # isvoid
    | ID                                                                                                                                # id
    | INT                                                                                                                               # int
    | STRING                                                                                                                            # string
    | BOOL                                                                                                                              # bool
    ;
