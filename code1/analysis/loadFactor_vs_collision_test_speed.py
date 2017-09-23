import sys
import re
import matplotlib.pyplot as plt


# python loadFactor_vs_collision_test_speed.py ../loadFactor_log ../plot/loadFactor_vs_collision_test_speed

f=open(sys.argv[1],"r")
loadFactor=[]
testTime=[]
ratio_bigram=[]
ratio_trigram=[]

prev_line=""

for line in f.readlines():

	if (re.match(r"^load factor ([\.0-9]+)",line)):

		loadFactor+=[float(re.match(r"^load factor ([\.0-9]+)",line).group(1))]
	if (re.match(r"^Decoding took ([\.0-9]+)s",line)):  # Building took 160.537s
		testTime+=[float(re.match(r"^Decoding took ([\.0-9]+)s",line).group(1))]
		# class edu.berkeley.nlp.assignments.assign1.student.longIntOpenHashMapBigram num_collision 266264368 num_access 332076077 ratio 0.8018173739145925
	if (re.match(r".*longIntOpenHashMapBigram .*ratio ([\.0-9]+)",line)):
		ratio_bigram+=[float(re.match(r".*longIntOpenHashMapBigram.*ratio ([\.0-9]+)",line).group(1))]
	if (re.match(r".*longIntOpenHashMap .*ratio ([\.0-9]+)",line)):
		ratio_trigram+=[float(re.match(r".*longIntOpenHashMap .*ratio ([\.0-9]+)",line).group(1))]

print len(loadFactor),len(testTime),len(ratio_bigram),len(ratio_trigram)
assert len(loadFactor)==len(testTime)==len(ratio_bigram)==len(ratio_trigram)


testTime=[x for _,x in sorted(zip(loadFactor,testTime))]
ratio_bigram=[x for _,x in sorted(zip(loadFactor,ratio_bigram))]
ratio_trigram=[x for _,x in sorted(zip(loadFactor,ratio_trigram))]

fig, ax1 = plt.subplots()
ax1.plot(loadFactor, testTime, 'b.-')
ax1.set_xlabel('Load Factor')
# Make the y-axis label, ticks and tick labels match the line color.
ax1.set_ylabel('LM Decode Time (s)',color='b')
ax1.tick_params('y', colors='b')



ax2 = ax1.twinx()

ax2.plot(loadFactor,ratio_bigram, 'r^--',label="Bigram")
ax2.plot(loadFactor,ratio_trigram,'m*-',label="Trigram")
ax2.plot(loadFactor,loadFactor,"g-")
ax2.set_ylabel('Ratio = # collision / # access',color='r')
ax2.tick_params('y', colors='r')
ax1.grid()
ax2.grid()
handles, labels = ax2.get_legend_handles_labels()
ax2.legend(handles, labels)

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



