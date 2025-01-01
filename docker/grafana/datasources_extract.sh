#!/bin/bash

mkdir -p datasources && curl -s "http://localhost:3000/api/datasources"  -u admin:admin | jq -c -M '.[]' | split -l 1 - datasources/