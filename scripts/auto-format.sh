#!/bin/bash

for i in `find src/main/java/com/micutio/jynk/ -name "*.java" -type f`; do
    echo "formatting" "$i"
    clang-format -i "$i"
done