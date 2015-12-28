#!/bin/bash

if [[ -e node_modules ]]; then
    rm -rf node_modules
fi

if [[ -z $SYNC ]]; then
    node $1
else
    nodemon --exec 'cp package.json /data && cd /data && npm install' \
            --watch package.json
    nodemon $1 --watch . --watch /data/node_modules
fi
