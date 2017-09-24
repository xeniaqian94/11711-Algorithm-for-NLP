ant -f build_assign1.xml
export DATA_PATH="/Users/xin/Desktop/11711/assign1_data/"
echo $DATA_PATH

#for loadFactor in 0.3 0.35 0.4 0.45 0.5 0.55 0.6 0.65 0.7 0.75 0.8 0.85 0.9 0.95; do java -cp assign1.jar:assign1-submit.jar -server -mx10500m edu.berkeley.nlp.assignments.assign1.student.LanguageModelTester2 -path $DATA_PATH -lmType TRIGRAM -noprint -loadFactor $loadFactor; done


#java -cp assign1.jar:assign1-submit.jar -server -mx10500m edu.berkeley.nlp.assignments.assign1.student.LanguageModelTester2 -path $DATA_PATH -lmType TRIGRAM -noprint -loadFactor 0.8 -quadraticProbing;

#for loadFactor in 0.3 0.35 0.4 0.45 0.5 0.55 0.6 0.65 0.7 0.75 0.8 0.85 0.9 0.95; do java -cp assign1.jar:assign1-submit.jar -server -mx10500m edu.berkeley.nlp.assignments.assign1.student.LanguageModelTester2 -path $DATA_PATH -lmType TRIGRAM -noprint -loadFactor $loadFactor -quadraticProbing; done

for loadFactor in 0.95; do java -cp assign1.jar:assign1-submit.jar -server -mx10500m edu.berkeley.nlp.assignments.assign1.student.LanguageModelTester2 -path $DATA_PATH -lmType TRIGRAM -noprint -loadFactor $loadFactor -quadraticProbing; done

