ant -f build_assign1.xml
#export DATA_PATH="/Users/xin/Desktop/11711/assign1_data_toy/"
export DATA_PATH="/Users/xin/Desktop/11711/assign1_data/"
echo $DATA_PATH
java -cp assign1.jar:assign1-submit.jar -server -mx2500m edu.berkeley.nlp.assignments.assign1.student.LanguageModelTester2 -path $DATA_PATH -lmType TRIGRAM -noprint
