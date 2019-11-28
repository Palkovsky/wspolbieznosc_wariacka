#!/bin/bash

N=$1

echo 'method,id,time'
node main.js $N asym
node main.js $N cond
node main.js $N atompick
