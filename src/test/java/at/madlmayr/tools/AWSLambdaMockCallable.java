package at.madlmayr.tools;

import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

public class AWSLambdaMockCallable implements Callable<InvokeResult> {

    private final RequestStreamHandler method;
    private final InvokeRequest invokeRequest;

    public AWSLambdaMockCallable(RequestStreamHandler method, InvokeRequest invokeRequest) {
        this.method = method;
        this.invokeRequest = invokeRequest;
    }

    @Override
    public InvokeResult call() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(invokeRequest.getPayload().array());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        method.handleRequest(inputStream, outputStream, null);

        InvokeResult result = new InvokeResult();
        result.setStatusCode(200);
        result.setPayload(ByteBuffer.wrap(outputStream.toByteArray()));
        result.setExecutedVersion("Mocked Version");
        result.setFunctionError("No Error");
        result.setLogResult("No Log Result");
        return result;
    }
}
