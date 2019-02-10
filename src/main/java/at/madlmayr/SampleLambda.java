package at.madlmayr;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SampleLambda implements RequestHandler<String, String>{

    // Initialize the Log4j logger.
    private static final Logger LOGGER = LogManager.getLogger(SampleLambda.class);

    @Override
    public String handleRequest(String input, Context context) {
        LOGGER.info("input: {}", input);
        return "handleRequest - " + input;
    }


}