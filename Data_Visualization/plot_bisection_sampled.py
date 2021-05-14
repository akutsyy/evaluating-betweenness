import matplotlib.pyplot as plt
import numpy as np
import seaborn as sns
import matplotlib.ticker as ticker

import data_parsing

import constants


# Define the palette as a list to specify exact values


def plot_bisec(graph, x, y, xtext, ytext, loglog, error, loglin=False, ):

    data  = data_parsing.readJSONToPandasFlat()

    data = data[data['algs'] == 'GeisbergerBisectionSampling']
    data = data[data['graphs'] == graph]



    palette = sns.color_palette("plasma",len(constants.samples_order))

    data = data.rename(columns={"param1": "Samples","graphs":'Graph','param2':'Accumulation Samples'})
    mergedErrors = data.groupby(['Samples','Accumulation Samples']).agg(mean_x=(x, 'mean'), mean_y=(y, 'mean'), std_x=(x, 'std'), std_y=(y, 'std'))
    mergedErrors['std_x'] = mergedErrors['std_x']*2
    mergedErrors['std_y'] = mergedErrors['std_y']*2

    data = data.groupby(['Samples','Accumulation Samples']).mean()

    plt.tight_layout()
    sns.set_theme(font_scale=1.25, style='ticks')  # big

    g = sns.lineplot(
        data=data,
        x=x, y=y,
        #hue="Accumulation Samples",hue_order=bsamples,
        hue="Samples",hue_order=constants.samples_order[::-1],
        palette=palette,
    )

    g.set(xlabel=xtext, ylabel=ytext)

    #g.set(xlim=10**-2)
    #g.set(ylim=10**-2)

    if loglog:
        g.xaxis.set_major_locator(ticker.MultipleLocator(5))
        g.xaxis.set_major_formatter(ticker.ScalarFormatter())

        g.yaxis.set_major_locator(ticker.MultipleLocator(5))
        g.yaxis.set_major_formatter(ticker.ScalarFormatter())

        g.set(xscale="log")
        g.set(yscale="log")

    if loglin:
        plt.xscale('log')

    if error:
        combined = data.merge(right=mergedErrors, how='outer', on=['Samples','Accumulation Samples'])
        combined = combined.sort_values(['Samples','Accumulation Samples'])
        g.errorbar(c='g',x=combined['mean_x'], y=combined['mean_y'], xerr=combined['std_x'], yerr=combined['std_y'], fmt='none', zorder=0)

    plt.savefig("Bisection Sampling_"+x + "_" + y + "_" + ('log-log_' if loglog else "") +('log-lin_' if loglin else "") + '.png', bbox_inches="tight")

def plot_bisecErr(otherAlgs,shortnames,graph, x, y, xtext, ytext, loglog, loglin=False, ):

    data  = data_parsing.readJSONToPandasFlat()

    # Filter out unwanted algs
    for a in constants.alg_names:
        if a not in otherAlgs and a != 'GeisbergerBisectionSampling':
            data = data[data['algs'] != a]

    data = data[data['graphs'] == graph]


    def r(row):
        if row['algs'] == 'GeisbergerBisectionSampling':
            return 'Sampling: '+str(row['param2'])
        return shortnames[row['algs']]

    data['Algorithm'] = data.apply (lambda row: r(row), axis=1)

    data = data.rename(columns={"param1": "Samples","graphs":'Graph'})
    mergedErrors = data.groupby(['Samples','Algorithm']).agg(mean_x=(x, 'mean'), mean_y=(y, 'mean'))
    combined = data.merge(right=mergedErrors, how='outer', on=['Samples', 'Algorithm'])
    combined = combined.sort_values(['Samples', 'Algorithm'])


    plt.tight_layout()
    sns.set_theme(font_scale=1.25, style='ticks')  # big

    bsamples = ['1','2','4','8','16']
    order = [shortnames[a] for a in otherAlgs] + ['Sampling: '+ n for n in bsamples]
    palette = sns.color_palette("hls",len(shortnames)+1)[:-1]+sns.color_palette("plasma",len(bsamples))

    g = sns.lineplot(
        data=combined,
        x='mean_x', y=y,
        hue="Algorithm",hue_order=order,
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
        plt.xscale('log')

    # Put the legend out of the figure
    plt.legend(bbox_to_anchor=(1.05, 1), loc=2, borderaxespad=0.)

    plt.savefig("figs/Bisection-Sampling-"+x + "-" + y + "-" + ('log-log-' if loglog else "") +('log-lin-' if loglin else "") +graph+ '.png', bbox_inches="tight")



otherAlgs=  ['BrandesAndPich2007','GeisbergerLinear','GeisbergerBisection']
shortNames = {'BrandesAndPich2007':'BP2007','GeisbergerLinear':'Linear','GeisbergerBisection':'Bisection'}
graphs = ['com-amazon','slashdot0811','ca-astroph']
x = 'time'
y = "max"
xtext = 'Computation Time (s)'
ytext = 'Maximum Normalized Error'
loglog = True
loglin = False
error = True

#plot_bisecErr(otherAlgs,shortNames,'ca-astroph', x, y, xtext, ytext, loglog,loglin=loglin)
#plt.show()
def plotall():
    for graph in graphs:
        for i,y in enumerate(constants.metrics):
            ytext = constants.mainseriesNames[i]
            loglog = constants.mainseriesLogLog[i]
            loglin = not loglog
            plot_bisecErr(otherAlgs,shortNames,graph, x, y, xtext, ytext, loglog,loglin=loglin)
            #plt.show()
            plt.clf()
plotall()
