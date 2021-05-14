import math

import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns

import constants
import data_parsing

data  = data_parsing.readJSONToPandasFlat()

data = data[data['algs'] == 'Brandes']

data = data.groupby('graphs').agg(mean_time=('time', 'mean'), std_time=('time', 'std'),
                                  mean_edges = ('accesses','mean'),std_edges=('accesses', 'std'))

print(data)

sns.set_theme(font_scale=1.25, style='ticks')  # big

g = sns.lineplot(
    data=data,
    x="accesses", y="time")

#g.set(xscale="log")
#g.set(yscale="log")

plt.show()