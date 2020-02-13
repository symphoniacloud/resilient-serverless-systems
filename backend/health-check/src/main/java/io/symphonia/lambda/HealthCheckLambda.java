package io.symphonia.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;

public class HealthCheckLambda {

    public APIGatewayV2ProxyResponseEvent handler(APIGatewayV2ProxyRequestEvent event) {
        var response = new APIGatewayV2ProxyResponseEvent();

        response.setStatusCode(200);
        response.setBody("Ok");
//        response.setStatusCode(500);
//        response.setBody("Internal Server Error");

        return response;
    }

}
