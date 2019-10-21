import csv
import sys
import matplotlib as mpl
mpl.use("agg")
import matplotlib.pyplot as plt
import numpy as np

def read_csv(path):
    consumers = []
    producers = []

    with open(path) as csv_file:
        reader = csv.reader(csv_file, delimiter=',')
        next(reader)
        for row in reader:
            val = (int(row[1]), int(row[2]))
            if row[0].lower() == 'p':
                producers.append(val)
            else:
                consumers.append(val)

    consumers.sort(key=lambda x: x[0])
    producers.sort(key=lambda x: x[0])

    return (consumers, producers)

def buckets(data, start=0, end=10000, step=1000):
    buckets = [[]]
    labels = ["{}-{}".format(start, start+step)]
    for (x, y) in data:
        if x > end:
            break
        if x - start > step:
            start += step
            buckets.append([])
            labels.append("{}-{}".format(start, start+step))
        buckets[-1].append(y)
    return (buckets, labels)

def main():
    if len(sys.argv) < 2:
        print("Usage: {} paths".format(sys.argv[0]))
        sys.exit(1)

    for csv_path in sys.argv[1:]:
        out_name = csv_path.split("/")[-1].split(".")[0] + ".png"
        (cons, prod) = read_csv(csv_path)
        step = 1000 if "10k_" in csv_path else 10000
        cons_data, labels = buckets(cons, start=0, end=step*10, step=step)
        prod_data, _ = buckets(prod, start=0, end=step*10, step=step)

        (m, pc, alg, rand) = csv_path.split("/")[-1].split("_")
        title = ""
        title += "M: " + m + " | "
        title += "Producers/Consumers: " + pc + " | "
        title += "Algorithm: " + alg.upper() + " | "
        title += "Rand: " + rand.split(".")[0].upper()

        fig, axs = plt.subplots(2)
        fig.set_figheight(12)
        fig.set_figwidth(14)
        fig.suptitle(title, fontsize=24)
        ax1, ax2 = axs[0], axs[1]

        pos = [np.median(x) for x in cons_data]
        ax1.boxplot(cons_data)
        ax1.set_title("Consumers")
        ax1.set(xlabel="n", ylabel="time[ns]")
        ax1.set_xticklabels(labels)

        ax2.boxplot(prod_data)
        ax2.set_title("Producers")
        ax2.set(xlabel="n", ylabel="time[ns]")
        ax2.set_xticklabels(labels)

        fig.savefig(out_name)

if __name__ == "__main__":
    main()
