# resilient-serverless-systems
Demo application for Resilient Serverless Systems talk

This repository contains backend code and a Serverless Application Model (SAM) template for a simple API (HTTP + WebSockets), as well as an Elm-based frontend.

Before deploying:

1. Set up a new hosted zone in Route 53.
1. Change all code references from "api.resilient-demo.symphonia.io" or "api-ws.resilient-demo.symphonia.io" to match your domain name.
1. Change the HostedZoneId in `template.yaml` to refer to your hosted zone.

To deploy the backend, run `./bin/deploy.bash <region>` across one or more AWS regions.

After deploying:

1. Manually configure DynamoDB Global Tables via the AWS web console.
2. Manually add ALIAS records to the Route 53 hosted zone for each region, for the WebSocket API. The Route 53 alias target value should be the "Target Domain Name" from the appropriate entry in the API Gateway console's "Custom Domain Names" section. Same for the "Alias Hosted Zone ID" value.

To run the frontend:

```
$ cd www
$ npm install
$ npm run dev
```

Navigate to http://localhost:3000