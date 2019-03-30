package at.madlmayr;


import at.madlmayr.lambdasample.SameModuleInput;
import cloud.localstack.DockerTestUtils;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Disabled
@TestInstance(PER_CLASS)
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(randomizePorts = false, services = {"s3", "lambda"})
public class TestWithLocalStack {


    private AmazonS3 amazonS3;
    private AWSLambda awsLambda;

    @BeforeAll
    void setUp() {
        amazonS3 = DockerTestUtils.getClientS3();
        awsLambda = DockerTestUtils.getClientLambda();
    }

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


        // RequestHandler under test
        final Class handlerClass = ReadConfig.class;
        final String handlerClassName = handlerClass.getCanonicalName();
        final String lambdaFunctionName = handlerClass.getSimpleName();
        LambdaInvoker invoker = new LambdaInvoker(amazonS3, awsLambda);


        // Create request object
        final SameModuleInput sameModuleInput = new SameModuleInput();
        sameModuleInput.setTestProperty("Testing");

        // Invoke Lambda
        final InvokeResult result = invoker.invokeLambda(sameModuleInput, lambdaFunctionName, handlerClassName);

        // Assert post-conditions
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.SC_OK);


    }


}
