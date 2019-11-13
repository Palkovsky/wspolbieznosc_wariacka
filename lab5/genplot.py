import sys
import csv
import matplotlib
import matplotlib.pyplot as plt

def read_csv():
    reader = csv.reader(sys.stdin, delimiter=',')
    next(reader) # skip header
    return [(row[0], int(row[1])) for row in reader]


if __name__ == "__main__":
    data = read_csv()
    sync  = [time for (method, time) in data if method == "sync"]
    async = [time for (method, time) in data if method == "async"]

    sync_avg = sum(sync)/len(sync)
    async_avg = sum(async)/len(async)

    print("Sync Average: {}, Async Average: {}".format(sync_avg, async_avg))

    plot_data = [sync, async]

    fig, ax = plt.subplots()

    # Create the boxplot
    bp = ax.boxplot(plot_data, labels = ["Synchronous", "Asynchronous"])
    ax.set_ylabel("Time[ms]")

    # Save the figure
    name = "fig.png" if len(sys.argv) <= 1 else sys.argv[1]
    fig.savefig(name, bbox_inches="tight")
