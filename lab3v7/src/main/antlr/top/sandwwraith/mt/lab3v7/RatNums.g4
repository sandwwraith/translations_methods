grammar RatNums;

program : func* main? EOF;

func  : 'fun' ID '(' ID* ')' BEGIN lines? ret END;
main  : 'main' BEGIN lines END;

lines : line | line lines;
line  : def | assign | call | ext_call | io | cond_stmt;

cond_stmt
	: 'if' '(' cond ')' '{' lines '}' ('else' '{' lines '}')? #ifCond
	| 'while' '(' cond ')' '{' lines '}' #whileCond
	;

cond: andd | cond '||' andd;
andd: comps | andd '&&' comps;

comps: comp;

comp
	: ID '==' ID #compEq
	| ID '<' ID #compLt
	| ID '<=' ID #compLte
	| ID '>' ID #compGt
	| ID '>=' ID #compGte
	| ID '!=' ID #compNe
	| '(' cond ')' #compBrackets
	;


def   : 'def' ID;
assign : ID '=' arithm;

call : ID '(' ID* ')' '->' ID;
ext_call : ID '(' ID* ')';
io : '>>' ID+ | '<<' ID+;
ret : 'ret' arithm;

arithm : fst | arithm '+' fst | arithm '-' fst;
fst : scnd | fst '*' scnd | fst '/' scnd;
scnd: primary | '-' scnd;
primary : ID | CONST | '(' arithm ')' ;


CONST : [0-9]+;
BEGIN : '{';
END : '}';
ID : [a-zA-Z] ( [a-zA-Z_] | [0-9] )*;
WS  : [ \t\r\n]+ -> skip ;
