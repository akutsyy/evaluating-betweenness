import json

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns

import constants
import data_parsing



# Define the palette as a list to specify exact values
palette = sns.color_palette("bright",len(constants.alg_names))


data = data_parsing.readJSONToPandasAvg()
for col in data.columns:
    print(col)

sns.set_theme(style="ticks")
g = sns.relplot(
    data=data,
    x="time", y="percent",
    hue="algs",hue_order=constants.alg_names,
    col="graphs",col_order=constants.shortGraphNames,
    kind="scatter" , palette=palette,
    height=5, facet_kws=dict(sharex=False),
)

g.set(xscale="log")
#g.set(yscale="log")

#g.set(xlim=10**-2)
#g.set(ylim=10**-2)



plt.show()