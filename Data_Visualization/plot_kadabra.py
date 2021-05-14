import matplotlib.pyplot as plt
import numpy as np
import seaborn as sns
import matplotlib.ticker as ticker

import data_parsing

import constants


# Define the palette as a list to specify exact values


def plot_kadabra(graph, x, y, xtext, ytext, loglog, error, loglin=False):

    data  = data_parsing.readJSONToPandasFlat()

    data = data[data['algs'] == 'KADABRA']
    data = data[data['graphs'] == graph]



    data = data.rename(columns={"param1": "Lambda","graphs":'Graph'})
    def numerical(row):
        return float(row['Lambda'])

    data['Lambda'] = data.apply (lambda row: numerical(row), axis=1)

    palette = sns.color_palette("plasma",as_cmap=True)

    plt.tight_layout()
    sns.set_theme(font_scale=1.25, style='ticks')  # big

    g = sns.scatterplot(
        data=data,
        x=x, y=y,
        hue="Lambda",
        palette=palette,
    )

    g.set(xlabel=xtext, ylabel=ytext)

    if loglog:
        g.xaxis.set_major_locator(ticker.MultipleLocator(5))
        g.xaxis.set_major_formatter(ticker.ScalarFormatter())

        g.yaxis.set_major_locator(ticker.MultipleLocator(5))
        g.yaxis.set_major_formatter(ticker.ScalarFormatter())

        g.set(xscale="log")
        g.set(yscale="log")

    if loglin:
        g.xaxis.set_major_locator(ticker.MultipleLocator(5))
        g.xaxis.set_major_formatter(ticker.ScalarFormatter())

        plt.xscale('log')

    if error:
        mergedErrors = data.groupby(['Lambda']).agg(mean_x=(x, 'mean'), mean_y=(y, 'mean'), std_x=(x, 'std'),
                                                    std_y=(y, 'std'))

        combined = data.merge(right=mergedErrors, how='outer', on=['Samples','Accumulation Samples'])
        combined = combined.sort_values(['Lambda','Accumulation Samples'])
        g.errorbar(c='g',x=combined['mean_x'], y=combined['mean_y'], xerr=combined['std_x'], yerr=combined['std_y'], fmt='none', zorder=0)

    plt.savefig("figs/KADABRA"+x + "-" + y + "-" + ('log-log-' if loglog else "") +('log-lin-' if loglin else "") + graph+'.png', bbox_inches="tight")



otherAlgs=  ['BrandesAndPich2007','GeisbergerLinear','GeisbergerBisection','KADABRA']
shortNames = {'BrandesAndPich2007':'BP2007','GeisbergerLinear':'Linear','GeisbergerBisection':'Bisection',"KADABRA":"KADABRA"}
graphs = constants.shortGraphNames
x = 'time'
y = "average"
xtext = 'Computation Time (s)'
ytext = 'Average Normalized Error'
loglog = True
loglin = False
error = False
for g in graphs:
    plot_kadabra(g, x, y, xtext, ytext, loglog,error,loglin=loglin)
    plt.clf()

