// define a grammar called Hello
grammar Hello;
r   : 'hello' name ;
name : ID ID ;
ID  : [a-z]+ ;
WS  : [ \t\r\n]+ -> skip ;