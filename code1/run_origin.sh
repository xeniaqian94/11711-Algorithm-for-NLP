ant -f build_assign1.xml
export DATA_PATH="/Users/xin/Desktop/11711/assign1_data/"
echo $DATA_PATH
java -cp assign1.jar:assign1-submit.jar -server -mx2000m edu.berkeley.nlp.assignments.assign1.LanguageModelTester -path $DATA_PATH -lmType TRIGRAM
java -cp assign1.jar:assign1-submit.jar -server -mx50m edu.berkeley.nlp.assignments.assign1.LanguageModelTester -path $DATA_PATH -lmType TRIGRAM -sanityCheck

