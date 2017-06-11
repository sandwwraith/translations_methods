#!/usr/bin/env bash

../gradlew :lab4:generateGrammarSource :lab4:installDist
TARGET="examples/src/main/kotlin/top/sandwwraith/mt/lab4/examples"
echo "Files written to $TARGET"
build/install/lab4/bin/lab4 hello.gram "$TARGET/hello"
build/install/lab4/bin/lab4 expr.gram "$TARGET/expr"
echo "Testing..."
../gradlew -q :lab4:examples:clean :lab4:examples:test