#!/bin/bash
rm -rf csvs
rm -rf plots
mkdir csvs
mkdir plots
for i in 5 10 20 30 40 50
do
    echo "N=$i"
    ./gencsv.sh ${i} > csvs/${i}.csv
    cat csvs/${i}.csv | python3 genplot.py plots/${i}.png
done
