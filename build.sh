#!/bin/zsh
cd $(dirname $0)
set -eux
rm -rf _build
mkdir -p _build

CP="$(print -l lib/**/*.jar | grep -v 'old/' | tr '\n' :)"
javac -cp "$CP" -d _build $(print -l src/*/**/*.java | 
  grep -v 'tests/' | 
  grep -Pv 'src/(RunSS|GLM).java'
)

(cd _build && jar cf ../myutil.jar .)
# (cd src && jar uf ../myutil.jar .)
ls -l myutil.jar
