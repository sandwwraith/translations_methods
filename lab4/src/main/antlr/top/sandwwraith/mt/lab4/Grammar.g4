grammar Grammar;

/*file : package? members? rule*;

package : '$package' PKG_NAME;
members : '$members' CODE;

rule : parserRule ';' | lexerRule ';';
parserRule : NT_ID inAttrs? (':' outAttr)? ':=' nt_prod ('|' nt_prod)*;*/

file: lexerRule*;

lexerRule
	: T_ID '=' term_value  # tokenRule
	| T_ID '=>' term_value # skipRule;

term_value
	: REGEX
	| STRING
	;

T_ID : [A-Z][A-Z0-9]*;
REGEX : '\'' (~('\''|'\r' | '\n') | '\\\'')* '\'';
STRING : '"' (~('"') | '\\"')* '"';

WS  : [ \t\r\n]+ -> skip ;