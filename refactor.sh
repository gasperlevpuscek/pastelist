#!/bin/bash
for file in $(find app/src/main/java -name "*.java"); do
    sed -i 's/this\.//g' $file
    sed -i 's/final //g' $file
done
