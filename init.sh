#!/bin/env sh
echo "- exporting SERVICEUSER_USERNAME"
export SERVICEUSER_USERNAME=$(cat /secret/serviceuser/username)

echo "- exporting SERVICEUSER_PASSWORD"
export SERVICEUSER_PASSWORD=$(cat /secret/serviceuser/password)
