# Client Lambda

## To start off

https://www.baeldung.com/java-aws-lambda

Start a local copy of dynamoDb => we use DynamoDbLocal

```
docker run -p 8000:8000 amazon/dynamodb-local:latest
docker run --attach STDOUT -v ~/.aws/:/root/.aws/:ro --net=host -e AWS_REGION=eu-central-1 --name xray-daemon -p 2000:2000/udp  amazon/aws-xray-daemon:latest -o
```

## Serverless & Localstack

Localstack does not offer a yet a mock for ```AWSLambdaAsyncClientBuilder``` which makes it hard to setup Testing. So
we won't consider localstack for the moment but deal with amazon containers for the next steps

## Testing of Lambda functions

We remove ```static``` blocks for better testability, as container reuse might not be given
- https://stackoverflow.com/questions/50347544/aws-lambda-java-static-initialization
- https://aws.amazon.com/de/blogs/compute/container-reuse-in-lambda/ (static init might not give us more performance)


## Input-Output JSON of Lambda

Handling De-Serialization myself
- https://docs.aws.amazon.com/lambda/latest/dg/java-programming-model-req-resp.html

This allows us to better test the functions without AWS involved.

## Using Dynamo DB Mapper

https://docs.aws.amazon.com/de_de/amazondynamodb/latest/developerguide/DynamoDBMapper.html

## For Local Testing we us
- DynamoDb Local:
  - https://docs.aws.amazon.com/de_de/amazondynamodb/latest/developerguide/DynamoDBLocal.Maven.html
  - https://www.baeldung.com/dynamodb-local-integration-tests
- WireMock:
  - http://wiremock.org/docs/getting-started/