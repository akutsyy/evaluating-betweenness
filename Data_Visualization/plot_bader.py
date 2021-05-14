import matplotlib.pyplot as plt
import numpy as np
import seaborn as sns
import matplotlib.ticker as ticker

import data_parsing

import constants


def plot_bader_withAlph(graph, y, ytext):

    data  = data_parsing.readJSONToPandasFlat()

    data = data[data['graphs'] == graph]
    data = data[data['algs'] == 'Bader']

    def r(row):
        if row['cutoff']:
            return 'Max Iterations'
        return 'Adaptive'



    data['cutoff'] = data.apply (lambda row: r(row), axis=1)

    data = data.rename(columns={"param1": "pp","param2":"alpha","graphs":'Graph','cutoff':'Termination Reason'})


    def pretty(row):
        return str(100*float(row['pp']))+"%"

    data['pp'] = data.apply (lambda row: pretty(row), axis=1)



    plt.tight_layout()
    sns.set_theme(font_scale=1.25, style='ticks')  # big

    palette = sns.color_palette("Set2",2)

    ratio = 1.5
    print(data['alpha'])


    g = sns.catplot(
        data=data,col='alpha',
        x='pp', y=y,
        hue="Termination Reason",hue_order=['Adaptive','Max Iterations'],
        palette=palette,
        jitter=False,dodge=False,
        order=['0.1%','1.0%','10.0%','20.0%']
    )

    g.set(yscale="log")
    g.set_axis_labels("v Selected From Top x %",ytext)


    #plt.legend(bbox_to_anchor=(1.05, 1), loc=2, borderaxespad=0.)


    plt.savefig("figs/Bader-" + "-" + y + "-"+"-"+graph+ '.png', bbox_inches="tight")


def plot_bader(graph, y, ytext):

    data  = data_parsing.readJSONToPandasFlat()

    data = data[data['graphs'] == graph]
    data = data[data['algs'] == 'Bader']

    print(graph)
    def r(row):
        if row['cutoff']:
            return 'Max Iterations'
        return 'Adaptive'



    data['cutoff'] = data.apply (lambda row: r(row), axis=1)

    data = data.rename(columns={"param1": "pp","param2":"alpha","graphs":'Graph'})


    def pretty(row):
        return str(100*float(row['pp']))+"%"

    data['pp'] = data.apply (lambda row: pretty(row), axis=1)


    data['Status'] = "Alpha: "+data['alpha']+"\nTerminated: "+data['cutoff']

    statusOrder=  ["Alpha: 2\nTerminated: Max Iterations",
                   "Alpha: 5\nTerminated: Max Iterations",
                   "Alpha: 2\nTerminated: Adaptive",
                   "Alpha: 5\nTerminated: Adaptive"]

    plt.tight_layout()
    sns.set_theme(font_scale=1.25, style='ticks')  # big

    palette = sns.color_palette("Set2",2) + sns.color_palette("crest",2)

    g = sns.stripplot(
        data=data,
        x='pp', y=y,
        hue="Status",hue_order=statusOrder,
        palette=palette,
        #size=20, marker="D", alpha=.25,edgecolor="gray",
        jitter=False,dodge=False,
        order=['0.1%','1.0%','10.0%','20.0%']
    )

    g.yaxis.set_major_locator(ticker.MultipleLocator(5))
    g.yaxis.set_major_formatter(ticker.ScalarFormatter())

    g.set(yscale="log")
    g.set(xlabel="From top x %", ylabel=ytext)

    plt.legend(bbox_to_anchor=(1.05, 1), loc=2, borderaxespad=0.)


    plt.savefig("figs/Bader-" + "-" + y + "-" +graph+ '.png', bbox_inches="tight")


graphs = [
    "slashdot0811",
    "as-caida20071105",
    "ca-astroph",
    "4932-protein",
]
y = "time"
ytext = 'Computation Time (s)'
loglog = False
loglin = False
error = True

for g in graphs:
        plot_bader_withAlph(g, y, ytext)
        plt.show()
        plt.clf()
