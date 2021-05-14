edges = []

with open("4932.protein.links--undirected--weighted.txt", "r") as file:
    with open("output.csv", "w") as output:
        names = {}
        i = 0
        for line in file:
            parts = line.split(" ")
            if not parts[0] in names:
                names[parts[0]] = i
                i += 1
            if not parts[1] in names:
                names[parts[1]] = i
                i += 1
            output.write(str(names[parts[0]]) + "," + str(names[parts[1]]) + "," + str(100/int(parts[2])) + "\n")
            if(i%1000==0):
                print(i)
