#!/bin/bash

set -e

REGION=$1

[ -z ${REGION} ] \
    && echo "REGION argument not provided" \
    && exit 1

mvn clean package -T 1C

SAM_BUCKET=$(aws cloudformation describe-stack-resources \
    --region ${REGION} \
    --stack-name sam-artifacts \
    --query 'StackResources[?LogicalResourceId==`Bucket`].PhysicalResourceId' \
    --output text)

[ -z ${SAM_BUCKET} ] \
    && echo "SAM S3 bucket not found" \
    && exit 1

mkdir -p target

aws cloudformation package \
    --s3-bucket ${SAM_BUCKET} \
    --template-file template.yaml \
    --output-template-file target/template-packaged.yaml

aws cloudformation deploy \
    --region ${REGION} \
    --capabilities CAPABILITY_IAM \
    --template-file target/template-packaged.yaml \
    --stack-name resilient-backend
