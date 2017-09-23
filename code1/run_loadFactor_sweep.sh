export DATA_PATH="/Users/xin/Desktop/11711/assign1_data/"
echo $DATA_PATH

#java -cp assign1.jar:assign1-submit.jar -server -mx2000m edu.berkeley.nlp.assignments.assign1.student.LanguageModelTester2 -path $DATA_PATH -lmType TRIGRAM -noprint -loadFactor 0.9

for loadFactor in 0.3 0.35 0.4 0.45 0.5 0.55 0.6 0.65 0.7 0.75 0.8 0.85 0.9 0.95; do java -cp assign1.jar:assign1-submit.jar -server -mx10500m edu.berkeley.nlp.assignments.assign1.student.LanguageModelTester2 -path $DATA_PATH -lmType TRIGRAM -noprint -loadFactor $loadFactor; done

#for sent in 7189 8986 9073252 7258601 5806881 4645505 3716404 2973123 2378498 1902798 1522239 1217791 974233 779386 623509 498807 399045 319236 255389 204311 163449 130759 104607 83685 66948 53559 42847 34277 27422 21937 17550 14040 11232; do java -cp assign1.jar:assign1-submit.jar -server -mx2500m edu.berkeley.nlp.assignments.assign1.student.LanguageModelTester2 -path $DATA_PATH -lmType TRIGRAM -noprint -calculatePerplexity $sent; done


