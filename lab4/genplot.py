import sys
import csv
import matplotlib
import matplotlib.pyplot as plt
import numpy as np

def read_csv(path):
    data = []
    with open(path) as csv_file:
        reader = csv.reader(csv_file, delimiter=',')
        next(reader)
        for row in reader:
            data.append((int(row[0]), int(row[1])))
    return data


if __name__ == "__main__":
    data = read_csv(sys.argv[1])
    xs = [x[0] for x in data]
    ys = [x[1]/1.0e9 for x in data]

    fig, ax = plt.subplots()
    # ax.set_xticks(np.arange(len(xs)+1))

    ax.plot(xs, ys, label="Execution Time(s)")
    ax.scatter([2], [ys[1]], label="Physical cores", color="blue")
    ax.scatter([4], [ys[3]], label="Logical cores", color="red")

    ax.legend()


    ax.set(xlabel="Thread Count", ylabel="Time(s)",
    title="Threads/Time plot")
    ax.grid()

    name = "plot.png" if len(sys.argv) <= 2 else sys.argv[2]
    fig.savefig(name)
    plt.show()
