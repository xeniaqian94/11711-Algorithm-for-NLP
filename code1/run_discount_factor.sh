export DATA_PATH="/Users/xin/Desktop/11711/assign1_data/"
echo $DATA_PATH

for discountFactor in 0.25 0.3 0.35 0.4 0.45 0.5 0.55 0.6 0.65 0.7 0.75 0.8 0.85 0.9 0.95; do java -cp assign1.jar:assign1-submit.jar -server -mx10500m edu.berkeley.nlp.assignments.assign1.student.LanguageModelTester2 -path $DATA_PATH -lmType TRIGRAM -noprint -discountFactor $discountFactor; done



