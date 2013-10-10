#!/bin/bash
cd $(dirname $0)
set -eux
(cd bin && jar cf ../myutil.jar .)
(cd src && jar uf ../myutil.jar .)
jar uf myutil.jar README.md

