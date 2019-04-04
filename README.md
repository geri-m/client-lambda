# Client Lambda

## To start off

https://www.baeldung.com/java-aws-lambda

Start a local copy of dynamoDb

```
docker run -p 8000:8000 amazon/dynamodb-local:latest
docker run --attach STDOUT -v ~/.aws/:/root/.aws/:ro --net=host -e AWS_REGION=eu-central-1 --name xray-daemon -p 2000:2000/udp  amazon/aws-xray-daemon:latest -o
```


## Serverless will start

we can use mock to develop and test using http://localstack.cloud. It has Junit5 Integration. Nice.

```
docker run --name localstack -e SERVICES=serverless -p 4574:4574 localstack/localstack
```


```
Starting mock CloudWatch Logs (http port 4586)...
Starting mock IAM (http port 4593)...
Starting mock S3 (http port 4572)...
Starting mock DynamoDB (http port 4569)...
Starting mock SNS (http port 4575)...
Starting mock API Gateway (http port 4567)...
Starting mock Lambda service (http port 4574)...
```

Localstack does not offer a yet a mock for ```AWSLambdaAsyncClientBuilder``` which makes it hard to setup Testing. So
we won't consider localstack for the moment but deal with amazon containers for the next steps

## Testing of Lambda functions

We remove static block for better testability as container reuse might not be given
- https://stackoverflow.com/questions/50347544/aws-lambda-java-static-initialization
- https://aws.amazon.com/de/blogs/compute/container-reuse-in-lambda/ (static init might not give us more performance)


## Input-Output JSON of Lambda

Handling De-Serialization myself
- https://docs.aws.amazon.com/lambda/latest/dg/java-programming-model-req-resp.html

This allows us to better test the functions without AWS involved.

## Using Dynamo DB Mapper

https://docs.aws.amazon.com/de_de/amazondynamodb/latest/developerguide/DynamoDBMapper.html

