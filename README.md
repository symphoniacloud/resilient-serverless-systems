# resilient-serverless-systems
Demo application for Resilient Serverless Systems talk

This repository contains backend code and a Serverless Application Model (SAM) template for a simple API (HTTP + WebSockets), as well as an Elm-based frontend.

Before deploying:

1. Set up a new hosted zone in Route 53.
1. Change all code references from "api.resilient-demo.symphonia.io" or "api-ws.resilient-demo.symphonia.io" to match your domain name.
1. Change the HostedZoneId in `template.yaml` to refer to your hosted zone.

To deploy the backend:

```
$ cd backend
$ ./bin/deploy.bash <AWS region>
```

Deploy across as many AWS regions as you wish.

After deploying, manually configure DynamoDB Global Tables via the AWS web console or API.

To run the frontend:

```
$ cd frontend
$ elm make src/Main.elm --optimize --output=dist/elm.js
$ python3 -m http.server
```

Navigate to http://localhost:8000
