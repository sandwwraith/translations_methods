# Лабораторная работа 2

Вариант 6. Описание переменных в Си. [Грамматика](GRAMMAR.md).

Сборка осуществляется с помощью `../gradlew lab2v6:fatJar`. 
После этого можно запустить исполняемый jar-файл:

	java -jar build/output/lab2v6-1.0-SNAPSHOT.jar
	
Первым аргументом командой строки программа ожидает строку, которую необходимо разобрать.
После этого в стандартый вывод будет напечатано дерево разбора в формате [dot](https://en.wikipedia.org/wiki/DOT_(graph_description_language)).

Если передать вторым аргументом строку `name`, то будет сгенерировано два файла: `name.gv` и `name.svg` - дерево разбора и его визуализация.
Для создания визуализации необходима программа `dot` из пакета `graphviz`.