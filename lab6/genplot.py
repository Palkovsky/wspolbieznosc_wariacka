import sys
import csv
import matplotlib
import matplotlib.pyplot as plt
import pandas as pd

if __name__ == "__main__":
    df = pd.read_csv(sys.stdin)
    df = df.groupby(["method", "id"]).agg(["mean"]).reset_index()
    labels = ["asym", "conductor", "atompick"]

    fig, axs = plt.subplots(3)
    fig.set_figheight(10)
    fig.subplots_adjust(hspace=0.35)

    for y in df.iterrows():
        print(y[1]["method"][0])

    for i in range(len(labels)):
        ax = axs[i]
        label = labels[i]

        ys = [y[1]["time"][0] for y in df.iterrows() if y[1]["method"][0] == label]
        xs = list(range(1, len(ys)+1))

        print(xs)
        print(ys)

        title = {
            "asym": "Asymetrical",
            "conductor" : "With conductor",
            "atompick" : "Two forks pick"
        }[label]
        ax.set_title(title)
        ax.set_xlabel("Philosopher ID")
        ax.set_ylabel("Wait time(ms)")
        ax.set_xticks(xs)
        ax.scatter(xs, ys)

    outfile = "fig.png" if len(sys.argv) < 2 else sys.argv[1]
    fig.savefig(outfile)
