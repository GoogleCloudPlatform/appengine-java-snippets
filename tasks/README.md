# Google Cloud Tasks App Engine Queue Samples

Sample command-line program for interacting with the Cloud Tasks API
using App Engine queues.

App Engine queues push tasks to an App Engine HTTP target. This directory
contains both the App Engine app to deploy, as well as the snippets to run
locally to push tasks to it, which could also be called on App Engine.

`CreateHTTPTask.java` constructs a task with an HTTP target and pushes it
to your queue.

`CreateHTTPTask.java` constructs a task with an HTTP target and OIDC token and pushes it
to your queue.

## Initial Setup

 * Set up a Google Cloud Project and enable billing.
 * Enable the
 [Cloud Tasks API](https://console.cloud.google.com/launcher/details/google/cloudtasks.googleapis.com).
 * Download and install the [Cloud SDK](https://cloud.google.com/sdk).
 * Download and install [Maven](http://maven.apache.org/install.html).
 * Set up [Google Application Credentials](https://cloud.google.com/docs/authentication/getting-started).

## Creating a queue

To create a queue using the Cloud SDK, use the following gcloud command:

```
gcloud beta tasks queues create-app-engine-queue my-queue
```

Note: A newly created queue will route to the default App Engine service and
version unless configured to do otherwise.

## Deploying the App Engine app
[Using Maven and the App Engine Plugin](https://cloud.google.com/appengine/docs/flexible/java/using-maven)
& [Maven Plugin Goals and Parameters](https://cloud.google.com/appengine/docs/flexible/java/maven-reference)

```
mvn appengine:deploy
```

## Run the Sample Using the Command Line

Set environment variables:

First, your project ID:

```
export GOOGLE_CLOUD_PROJECT=<YOUR_GOOGLE_CLOUD_PROJECT>
```

Then the queue ID, as specified at queue creation time. Queue IDs already
created can be listed with `gcloud beta tasks queues list`.

```
export QUEUE_ID=my-queue
```

And finally the location ID, which can be discovered with
`gcloud beta tasks queues describe $QUEUE_ID`, with the location embedded in
the "name" value (for instance, if the name is
"projects/my-project/locations/us-central1/queues/my-appengine-queue", then the
location is "us-central1").

```
export LOCATION_ID=<YOUR_ZONE>
```

### Using HTTP Push Queues

Set an environment variable for the endpoint to your task handler. This is an
example url to send requests to the App Engine task handler:
```
export URL=https://${PROJECT_ID}.appspot.com/tasks/create
```

Running the sample will create a task and send the task to the specific URL
endpoint, with a payload specified:

```
mvn exec:java@HttpTask"
```

### Using HTTP Targets with Authentication Headers

In `CreateHttpTaskWithToken.java`, add your service account email in place of
`<SERVICE_ACCOUNT_EMAIL>` to authenticate the OIDC token.

Running the sample with command:
```
mvn exec:java@WithToken"
```
