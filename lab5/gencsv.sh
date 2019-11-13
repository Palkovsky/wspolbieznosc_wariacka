#!/bin/bash

echo 'method, time'

for i in {1..100}
do
    php zad2.js -ssync | node
    php zad2.js -sasync | node
done
