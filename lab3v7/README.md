# Лабораторная работа 3

Вариант 7. Хороший язык.

> Придумайте хороший императивный язык программирования, на
котором приятно писать программы. Транслируйте с него в Си.

Данный язык - крайне простой императивный язык, который призван облегчить работу
с рациональными числами из [GMP](https://gmplib.org/manual/Rational-Number-Functions.html#Rational-Number-Functions)
и является обёрткой над ними.
  
У всех переменных один тип - рациональное число, поддерживаются 4 арифметических операции,
скобки, унарный минус, ввод-вывод (`>>` и `<<`), создание своих функций и их вызов.
Можно вызывать функции из C.

### Пример

Всю грамматику можно посмотреть [здесь](src/main/antlr/top/sandwwraith/mt/lab3v7/RatNums.g4).

Исходный файл `example.rn`:

```
fun sqr(a) {
    ret a * a
}

fun sqr_sum(a b) {
    x = a * a + b * b
    ret x
}

main {
    >> x y
    sqr_sum(x y) -> z
    sqr(z) -> z
    << z
}
```

После этого нужно собрать проект и запустить транслятор:

```bash
../gradlew lab3v7:generateGrammarSource installDist
./run.sh example.rn e.out
```

Можно открыть `example.c` и посмотреть результат работы транслятора.
Функция `sqr_sum`, например, будет выглядеть так:

```c
void sqr_sum(mpq_t a, mpq_t b, mpq_t _out_param) {
    mpq_t x;
    mpq_init(x);
    mpq_t _tmp_var0;
    mpq_init(_tmp_var0);
    mpq_mul(_tmp_var0, a, a);
    mpq_t _tmp_var1;
    mpq_init(_tmp_var1);
    mpq_mul(_tmp_var1, b, b);
    mpq_t _tmp_var2;
    mpq_init(_tmp_var2);
    mpq_add(_tmp_var2, _tmp_var0, _tmp_var1);
    mpq_set(x, _tmp_var2);
    mpq_set(_out_param, x);
    mpq_clears(_tmp_var1, _tmp_var0, _tmp_var2, x, 0);
}
```

Более громоздко, не правда ли?

Если запустить `./e.out` и ввести 3 и 4, то получим 625, как и ожидалось.