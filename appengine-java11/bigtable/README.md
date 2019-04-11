Bigtable-hello-j11
=================

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java11/bigtable/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Bigtable Hello World application to Google App Engine Standard for Java 11.

## Setup your Google Cloud Project
- Install the [Google Cloud SDK](https://cloud.google.com/sdk/) and run:
```
   gcloud init
```
If this is your first time creating an App engine application:
```
   gcloud app create
```

## Setup the Sample App

- Copy the sample apps to your local machine:
```
  git clone https://github.com/GoogleCloudPlatform/java-docs-samples
```
- [Create a Cloud Bigtable Instance](https://cloud.google.com/bigtable/docs/creating-instance).

- Update `INSTANCE_ID` value in [Main.java](src/main/java/com/example.bigtable/Main.java).

- Move into the `appengine-java11/bigtable` directory and compile the app:
```
  cd java-docs-samples/appengine-java11/bigtable
  mvn package
```

### Deploy to App Engine Standard for Java 11
```
    mvn  appengine:deploy -Dapp.deploy.projectId=<project-id>
```

### When done

Cloud Bigtable Instances should be [deleted](https://cloud.google.com/bigtable/docs/deleting-instance)
when they are no longer being used as they use significant resources.
