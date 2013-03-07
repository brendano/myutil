#!/bin/zsh
here=$(dirname $0)
CP=${CP:-""}
CP="$CP:$here/bin"   ## eclipse build dir
CP="$CP:$(echo $here/lib/*.jar | tr ' ' ':')"
for f in xom.jar joda-time.jar jollyday.jar stanford-corenlp-1.3.4-models.jar; do
  CP="$CP:$HOME/sw/nlp/stanford-corenlp-full-2012-11-12/$f"
done
# echo $CP
java -Xmx2g -XX:ParallelGCThreads=1 -ea -cp "$CP" "$@"
