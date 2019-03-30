# Client Lambda

## Helpfull Stuff

###

https://www.baeldung.com/java-aws-lambda

Start a local copy of dynamoDb

```
docker run -p 8000:8000 amazon/dynamodb-local
docker run --attach STDOUT -v ~/.aws/:/root/.aws/:ro --net=host -e AWS_REGION=eu-central-1 --name xray-daemon -p 2000:2000/udp  amazon/aws-xray-daemon:latest -o
docker run --name localstack -e SERVICES=serverless -p 4574:4574 localstack/localstack
```


## Serverless will start

```
Starting mock CloudWatch Logs (http port 4586)...
Starting mock IAM (http port 4593)...
Starting mock S3 (http port 4572)...
Starting mock DynamoDB (http port 4569)...
Starting mock SNS (http port 4575)...
Starting mock API Gateway (http port 4567)...
Starting mock Lambda service (http port 4574)...
```