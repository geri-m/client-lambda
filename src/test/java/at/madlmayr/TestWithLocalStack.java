package at.madlmayr;


import cloud.localstack.DockerTestUtils;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.lambda.model.Runtime;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Disabled
@TestInstance(PER_CLASS)
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(randomizePorts = true, services = {"s3", "lambda"})
public class TestWithLocalStack {


    @Test
    public void testLocalLambdaAPI() {
        AWSLambda lambda = DockerTestUtils.getClientLambda();
        ListFunctionsResult functions = lambda.listFunctions();
        Assertions.assertTrue(functions.getFunctions().isEmpty());
    }

    @Test
    public void testLocalS32API() {
        String bucketName = "test";
        AmazonS3 amazonS3 = DockerTestUtils.getClientS3();
        List functions = amazonS3.listBuckets();
        Assertions.assertTrue(functions.isEmpty());
        Bucket bucket = amazonS3.createBucket(bucketName);
        assertThat(bucket.getName()).isEqualTo(bucketName);
    }

    @Test
    public void testLocalS3API() throws Exception {
        AWSLambda lambda = DockerTestUtils.getClientLambda();
        String functionName = UUID.randomUUID().toString();
        String streamName = UUID.randomUUID().toString();

        // create function
        CreateFunctionRequest request = new CreateFunctionRequest();
        request.setFunctionName(functionName);
        request.setRuntime(Runtime.Java8);
        request.setCode(LocalTestUtil.createFunctionCode(ReadConfig.class));
        request.setHandler(ReadConfig.class.getName());
        lambda.createFunction(request);

        InvokeRequest ir = new InvokeRequest();
        ir.setFunctionName("at.madlmayr.ReadConfig::handleRequest");

        lambda.invoke(ir);

    }


}
