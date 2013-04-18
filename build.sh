#!/bin/bash
set -eux
(cd $(dirname $0)/bin && jar cf ../myutil.jar .)
