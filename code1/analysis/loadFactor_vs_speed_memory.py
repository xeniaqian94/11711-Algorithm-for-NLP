import sys
import re
import matplotlib.pyplot as plt


# python loadFactor_vs_speed_memory.py ../loadFactor_log ../plot/loadFactor_log_vs_speed_memory

f=open(sys.argv[1],"r")
loadFactor=[]
buildTime=[]
memory=[]

prev_line=""

for line in f.readlines():

	if (re.match(r"^load factor ([\.0-9]+)",line)):

		loadFactor+=[float(re.match(r"^load factor ([\.0-9]+)",line).group(1))]
	if (re.match(r"^Building took ([\.0-9]+)s",line)):  # Building took 160.537s
		buildTime+=[float(re.match(r"^Building took ([\.0-9]+)s",line).group(1))]
	if (re.match(r"Memory usage is ([\.0-9MG]+)",line)) and "Spot checks completed" not in prev_line:
		memory_string=re.match(r"Memory usage is ([\.0-9MG]+)",line).group(1)
		if "M" in memory_string:
			memory+=[float(memory_string[:-1])]
		elif "G" in memory_string:
			memory+=[float(memory_string[:-1])*1024]
	prev_line=line

print len(loadFactor),len(buildTime),len(memory)
assert len(loadFactor)==len(buildTime)==len(memory)

buildTime=[x for _,x in sorted(zip(loadFactor,buildTime))]
memory=[x for _,x in sorted(zip(loadFactor,memory))]

print loadFactor,buildTime,memory

fig, ax1 = plt.subplots()
ax1.plot(loadFactor, buildTime, 'b.-')
ax1.set_xlabel('Load Factor')
# Make the y-axis label, ticks and tick labels match the line color.
ax1.set_ylabel('LM Build Time (s)',color='b')
ax1.tick_params('y', colors='b')


ax2 = ax1.twinx()

ax2.plot(loadFactor,memory, 'r^--')
ax2.set_ylabel('Memory Usage (M)',color='r')
ax2.tick_params('y', colors='r')
ax1.grid()
ax2.grid()

fig.tight_layout()
plt.savefig(sys.argv[2])
plt.show()


# fig=plt.figure()
# ax=fig.add_subplot(111)

# ax.set_xlabel("training size")
# ax.set_ylabel("perplexity")
# ax.grid()
# ax.plot(training_size,perplexity,"b.-")

# plt.savefig(sys.argv[2])
# plt.show()



