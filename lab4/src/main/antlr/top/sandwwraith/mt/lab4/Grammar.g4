grammar Grammar;

file : pckg? members? (begin rulee+)? EOF;

begin: '|>' NT_ID;
pckg : '+package' PKG_NAME;
members : '+members' CODE;

rulee
	: parserRulee ';'
	| lexerRule ';'
	;

parserRulee : NT_ID inAttrs? (':' outAttr)? ':=' prods ('|' prods)*;

inAttrs : '<' param (',' param)* '>';
param : paramName ':' paramType;
paramType : T_ID;
paramName : NT_ID;
outAttr: T_ID;

prods: prod* CODE?;
prod: NT_ID args? | T_ID;
args: '(' CODE (',' CODE)* ')';


lexerRule
	: T_ID '=' term_value  # tokenRule
	| T_ID '=>' term_value # skipRule
	;

term_value
	: REGEX
	| STRING
	;

NT_ID : [a-z][a-z0-9]*;
T_ID : [A-Z][a-zA-Z0-9]*;

REGEX : '\'' (~('\''|'\r' | '\n') | '\\\'')* '\'';
STRING : '"' (~('"') | '\\"')* '"';

CODE : '{' (~[{}]+ CODE?)* '}' ;
PKG_NAME : ([a-z] | '.')+;

WS  : [ \t\r\n]+ -> skip ;