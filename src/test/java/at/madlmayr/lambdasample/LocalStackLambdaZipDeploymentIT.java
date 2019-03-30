package at.madlmayr.lambdasample;

import at.madlmayr.LambdaInvoker;
import cloud.localstack.DockerTestUtils;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

// Taken from: https://github.com/ianbrandt/localstack-test/blob/master/localstack-test-handler/src/test/java/com/ianbrandt/test/localstack/LocalStackLambdaZipDeploymentIT.java

@TestInstance(PER_CLASS)
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(randomizePorts = false, services = {"s3", "lambda"})
class LocalStackLambdaZipDeploymentIT {

    // Initialize the Log4j logger.
    private static final Logger LOGGER = LogManager.getLogger(LocalStackLambdaZipDeploymentIT.class);

    private AmazonS3 amazonS3;
    private AWSLambda awsLambda;

    @BeforeAll
    void setUp() {
        amazonS3 = DockerTestUtils.getClientS3();
        awsLambda = DockerTestUtils.getClientLambda();
    }


    @Test
    void testSameModuleInputRequestHandler() throws Exception {

        // RequestHandler under test
        final Class<? extends RequestHandler> handlerClass = SameModuleInputRequestHandler.class;
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

    @Test
    void testOtherModuleInputRequestHandler() throws IOException, ClassNotFoundException {

        // RequestHandler under test
        final Class<? extends RequestHandler> handlerClass = OtherModuleInputRequestHandler.class;
        final String handlerClassName = handlerClass.getCanonicalName();
        final String lambdaFunctionName = handlerClass.getSimpleName();

        LambdaInvoker invoker = new LambdaInvoker(amazonS3, awsLambda);

        // Create request object
        final OtherModuleInput otherModuleInput = new OtherModuleInput();
        otherModuleInput.setOtherTestProperty("Testing");

        // Invoke Lambda
        final InvokeResult result = invoker.invokeLambda(otherModuleInput, lambdaFunctionName, handlerClassName);

        // Assert post-conditions
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }

}