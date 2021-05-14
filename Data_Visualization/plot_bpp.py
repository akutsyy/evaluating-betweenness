import math

import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from matplotlib.lines import Line2D
import matplotlib.ticker as ticker

import constants
import data_parsing


def plot_bpp_withparts(graph):

    data  = data_parsing.readJSONToPandasFlat()


    # Filter out unwanted algs
    for a in constants.alg_names:
        if a not in ['BrandesPP','BrandesSubset']:
            data = data[data['algs'] != a]

    # Filter out unwanted algs
    data = data[data['graphs'] == graph]

    portions = ['Partitioning','Constructing Skeleton','Running Brandes','Calculating Distances','Centralities Algorithm']

    def select(row,i):
        if row['cutoff'] == 'none':
            return row['time']/(float(row['param1'])*float(row['nodes']))
        split = (float(row['cutoff'].split(',')[i].strip(" ").strip("[]"))/(float(row['param1'])*float(row['nodes'])) + (sum([row[portions[x]] for x in range(i-1)]) if i>0 else 0))
        return split


    for i,p in enumerate(portions):
        data[p] = data.apply(lambda row: select(row,i), axis=1)

    data = data.rename(columns={"param1":"Set Percent",'param2':"Number of Partitions","graphs":'Graph','algs':'Algorithm'})

    def comb(row):
        return row['Algorithm']+(" "+row["Number of Partitions"] +" Partitions" if row["Number of Partitions"]!='none' else "")

    data['Parameters'] = data.apply(lambda row: comb(row), axis=1)

    plt.tight_layout()
    sns.set_theme(font_scale=1.25, style='ticks')  # big
    fig, ax = plt.subplots()
    order = ['BrandesSubset','BrandesPP 2 Partitions', 'BrandesPP 8 Partitions', 'BrandesPP 32 Partitions',
             'BrandesPP 128 Partitions', 'BrandesPP 512 Partitions', 'BrandesPP 2048 Partitions']
    portions = ['Partitioning','Constructing Skeleton','Running Brandes','Centralities Algorithm']

    pal = sns.color_palette("plasma",len(portions)*len(order))

    for i,p in enumerate(portions[::-1]):
        ax = sns.barplot(
            data=data,order = ['0.001','0.01','0.05','0.1'],ci=('sd' if i==0 else None),
            x='Set Percent', y=p,hue='Parameters',hue_order=order,palette=pal[i::len(portions)],ax=ax,saturation=1
        )

    custom_lines = [Line2D([0], [0], color=pal[i*len(portions)], lw=4) for i in range(len(order))]
    ax.legend(custom_lines, order,bbox_to_anchor=(1.05, 1), loc=2, borderaxespad=0.)


    ax.set(xlabel="Target Set Portion", ylabel="Computation Time Per Node in Target Set (s)")

    #plt.yscale('log')


    # Put the legend out of the figure
    #plt.legend(bbox_to_anchor=(1.05, 1), loc=2, borderaxespad=0.)

    plt.savefig("figs/bpp-withparts"+graph+'.png', bbox_inches="tight")




def plot_bpp(graph):

    data  = data_parsing.readJSONToPandasFlat()


    # Filter out unwanted algs
    for a in constants.alg_names:
        if a not in ['BrandesPP','BrandesSubset']:
            data = data[data['algs'] != a]

    # Filter out unwanted algs
    data = data[data['graphs'] == graph]

    def make_scaled(row):
        return row['accesses']/(float(row['param1'])*float(row['nodes']))

    data['accesses'] = data.apply(lambda row: make_scaled(row), axis=1)

    data = data.rename(columns={"param1":"Set Percent",'param2':"Number of Partitions","graphs":'Graph','algs':'Algorithm'})

    def comb(row):
        return row['Algorithm']+(" "+row["Number of Partitions"] +" Partitions" if row["Number of Partitions"]!='none' else "")

    data['Parameters'] = data.apply(lambda row: comb(row), axis=1)

    plt.tight_layout()
    sns.set_theme(font_scale=1.25, style='ticks')  # big
    order = ['BrandesSubset','BrandesPP 2 Partitions', 'BrandesPP 8 Partitions', 'BrandesPP 32 Partitions',
             'BrandesPP 128 Partitions', 'BrandesPP 512 Partitions', 'BrandesPP 2048 Partitions']
    pal = [sns.color_palette("rainbow",len(order))[-1]]+sns.color_palette("rainbow",len(order))[:-1]
    g = sns.barplot(
        data=data,order = ['0.001','0.01','0.05','0.1'],
        x='Set Percent', y='accesses',hue='Parameters',hue_order=order,palette=pal,#,ci=None
    )
    plt.yscale('log')
    g.set(xlabel="Target Set Percentage", ylabel="Edge Traversals per Node in Target Set ")
    plt.legend(bbox_to_anchor=(1.05, 1), loc=2, borderaxespad=0.)




    # Put the legend out of the figure
    #plt.legend(bbox_to_anchor=(1.05, 1), loc=2, borderaxespad=0.)

    plt.savefig("figs/bpp-log"+graph+'.png', bbox_inches="tight")

graphs = ['as-caida20071105','ca-astroph','4932-protein']
for g in graphs:
    plot_bpp_withparts(g)
    #plt.show()
    plt.clf()