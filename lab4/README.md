# Лабораторная работа 4

Написание автоматического генератора трансляторов

> Необходимо написать некоторый упрощенный аналог генератора трансляторов. Рекомендуется брать за основу синтаксис ANTLR или Bison. Рекомендуется для чтения входного файла с грамматикой сгенерировать разборщик с помощью ANTLR или Bison.

Реализовано: 

* (10 баллов) LL(1)-грамматики, нисходящий разбор
* (10 баллов) поддержка синтезируемых атрибутов
* (10 баллов) поддержка наследуемых атрибутов

Пример грамматики: 

```
+package top.sandwwraith.mt.lab4.examples.hello

|> hello

hello : String := HELLO a { ("Hello, " + a + "!").capitalize() };
a : String := ID { ID };

HELLO = "hello";
ID = '[A-Z][a-z]*';
WS => '\s+';
```

Поддерживаются директивы `+package` и `+members`. Нетерминалы начинаются со строчных букв, а терминалы - с заглавных.
Символом `|>` обозначается стартовый нетерминал. Два типа терминалов: литерал (в двойных кавычках) и регулярное выражение (в одинарных).
Символом `=>` обозначаются токены лексера, которые по умолчанию пропускаются. [Полное описание грамматики.](src/main/antlr/top/sandwwraith/mt/lab4/Grammar.g4)

Примеры есть в файлах *.gram. Можно запустить `genExamples.sh` и посмотреть на сгенерированный код в подпроекте `examples`.