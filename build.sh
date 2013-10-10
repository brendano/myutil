#!/bin/bash
set -eux
(cd $(dirname $0)/bin && jar cf ../myutil.jar .)
(cd $(dirname $0)/src && jar uf ../myutil.jar .)
jar uf myutil.jar README.md

