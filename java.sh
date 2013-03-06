#!/bin/bash
here=$(dirname $0)
CP="$CP:$here/bin"   ## eclipse build dir
CP="$CP:$(echo $here/lib/*.jar | tr ' ' ':')"
java -Xmx2g -XX:ParallelGCThreads=2 -ea -cp "$CP" "$@"
