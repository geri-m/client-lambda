# Client Lambda

## Helpfull Stuff

###

https://www.baeldung.com/java-aws-lambda

Start a local copy of dynamoDb

```
docker run -p 8000:8000 amazon/dynamodb-local
docker run --attach STDOUT -v ~/.aws/:/root/.aws/:ro --net=host -e AWS_REGION=eu-central-1 --name xray-daemon -p 2000:2000/udp  amazon/aws-xray-daemon:latest -o
```