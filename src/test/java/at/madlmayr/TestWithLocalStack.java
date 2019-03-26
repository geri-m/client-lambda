package at.madlmayr;


import cloud.localstack.TestUtils;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.lambda.model.Runtime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

@Disabled
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(randomizePorts = false, services = {"serverless"})
public class TestWithLocalStack {


    @Test
    public void testLocalLambdaAPI() {
        AWSLambda lambda = TestUtils.getClientLambda();
        ListFunctionsResult functions = lambda.listFunctions();
        Assertions.assertTrue(functions.getFunctions().isEmpty());
    }

    @Test
    public void testLocalS3API() throws Exception {
        AWSLambda lambda = TestUtils.getClientLambda();
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
