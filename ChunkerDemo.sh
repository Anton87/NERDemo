# set java classpath
# Try change the CLASSPATH separator symbol from colon ":" to semicolon ";"
# on Windows.
export CLASSPATH="."
export CLASSPATH=${CLASSPATH}:"target/NERDemo-0.0.1-SNAPSHOT.jar"
export CLASSPATH=${CLASSPATH}:"target/dependency/*"

# run the ChunkerDemo app
java -cp $CLASSPATH it.unitn.ainlp.chunker.app.ChunkerDemo ${1:?"srcFile not specified"} ${2:?"destDir not specified"}
