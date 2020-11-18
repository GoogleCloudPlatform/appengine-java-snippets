# Spark Applications for Cloud Bigtable

## Overview

The project shows how to read data from or write data to [Cloud Bigtable](https://cloud.google.com/bigtable) using [Apache Spark](https://spark.apache.org/) and [Apache HBase™ Spark Connector](https://github.com/apache/hbase-connectors/tree/master/spark).

**Apache Spark** is the execution environment that can distribute and parallelize data processing (loading data from and writing data to various data sources).
Apache Spark provides DataSource API for external systems to plug into as data sources (also known as data providers).

**Apache HBase™ Spark Connector** implements the DataSource API for Apache HBase and allows executing relational queries on data stored in Cloud Bigtable.

**Google Cloud Bigtable** is a fully-managed cloud service for a NoSQL database of petabyte-scale and large analytical and operational workloads.
`bigtable-hbase-2.x-hadoop` provides a bridge from the HBase API to Cloud Bigtable that allows Spark queries to interact with Bigtable using the native Spark API.

**Google Cloud Dataproc** is a fully-managed cloud service for running [Apache Spark](https://spark.apache.org/) applications and [Apache Hadoop](https://hadoop.apache.org/) clusters.

## Tasks

FIXME Remove the section once all tasks done.

- [ ] Avoid specifying dependencies at runtime (remove `--packages` option for `spark-submit`)
- [ ] Make sure README.md is up-to-date before claiming the PR done

## Prerequisites

1. [Google Cloud project](https://console.cloud.google.com/)

1. [Google Cloud SDK](https://cloud.google.com/sdk/) installed.

1. [sbt](https://www.scala-sbt.org/) installed.

1. [Apache Spark](https://spark.apache.org/) installed. Download Spark built for Scala 2.11.

1. A basic familiarity with [Apache Spark](https://spark.apache.org/) and [Scala](https://www.scala-lang.org/).

## Assemble the Examples

Execute the following `sbt` command to assemble the sample applications as a single uber/fat jar (with all of its dependencies and configuration).

```
sbt clean assembly
```

The above command should build `target/scala-2.11/bigtable-spark-samples-assembly-0.1.jar` file.

Set the following environment variable to reference the assembly file.

```
BIGTABLE_SPARK_ASSEMBLY_JAR=target/scala-2.11/bigtable-spark-samples-assembly-0.1.jar
```

## Run Examples with Bigtable Emulator

### Start Bigtable Emulator

```
gcloud beta emulators bigtable start
```

### Configure Environment

Set the following environment variables.

```
SPARK_HOME=your-spark-home
BIGTABLE_SPARK_PROJECT_ID=your-project-id
BIGTABLE_SPARK_INSTANCE_ID=your-instance-id

BIGTABLE_SPARK_WORDCOUNT_TABLE=wordcount
BIGTABLE_SPARK_WORDCOUNT_FILE=src/test/resources/Romeo-and-Juliet-prologue.txt

BIGTABLE_SPARK_COPYTABLE_TABLE=copytable
```

Initialize the environment to point to the Bigtable Emulator.

```
$(gcloud beta emulators bigtable env-init)
```

### Create Tables

Create the tables using `cbt` tool.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  createtable $BIGTABLE_SPARK_WORDCOUNT_TABLE \
  "families=cf"
```

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  createtable $BIGTABLE_SPARK_COPYTABLE_TABLE \
  "families=cf"
```

List tables.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  ls
```

Output should be:
```
copytable
wordcount
```

For more information about using the `cbt` tool, including a list of available commands, see the [cbt Reference](https://cloud.google.com/bigtable/docs/cbt-reference).

### Wordcount

Run [example.Wordcount](src/main/scala/example/Wordcount.scala).

```
$SPARK_HOME/bin/spark-submit \
  --packages org.apache.hbase.connectors.spark:hbase-spark:1.0.0 \
  --class example.Wordcount \
  $BIGTABLE_SPARK_ASSEMBLY_JAR \
  $BIGTABLE_SPARK_PROJECT_ID $BIGTABLE_SPARK_INSTANCE_ID \
  $BIGTABLE_SPARK_WORDCOUNT_TABLE $BIGTABLE_SPARK_WORDCOUNT_FILE
```

### Verify

Count the number of rows in the `BIGTABLE_SPARK_WORDCOUNT_TABLE` table.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  count $BIGTABLE_SPARK_WORDCOUNT_TABLE
```
Output should be:
```
88
```

### CopyTable

Run [example.CopyTable](src/main/scala/example/CopyTable.scala).

```
$SPARK_HOME/bin/spark-submit \
  --packages org.apache.hbase.connectors.spark:hbase-spark:1.0.0 \
  --class example.CopyTable \
  $BIGTABLE_SPARK_ASSEMBLY_JAR \
  $BIGTABLE_SPARK_PROJECT_ID $BIGTABLE_SPARK_INSTANCE_ID \
  $BIGTABLE_SPARK_WORDCOUNT_TABLE $BIGTABLE_SPARK_COPYTABLE_TABLE
```

### Verify

Count the number of rows in the `BIGTABLE_SPARK_COPYTABLE_TABLE` table.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  count $BIGTABLE_SPARK_COPYTABLE_TABLE
```
Output should be:
```
88
```

## Run Wordcount with Cloud Bigtable

### Environment Variables

Set the following environment variables (some are borrowed from [Run Examples with Bigtable Emulator](#run-examples-with-bigtable-emulator)).

```
SPARK_HOME=your-spark-home
BIGTABLE_SPARK_PROJECT_ID=your-project-id
BIGTABLE_SPARK_INSTANCE_ID=your-instance-id

BIGTABLE_SPARK_WORDCOUNT_TABLE=wordcount
BIGTABLE_SPARK_WORDCOUNT_FILE=src/test/resources/Romeo-and-Juliet-prologue.txt
BIGTABLE_SPARK_ASSEMBLY_JAR=target/scala-2.11/bigtable-spark-samples-assembly-0.1.jar
```

### Create Cloud Bigtable Instance

Create a Cloud Bigtable instance using the Google Cloud Console (as described in the [Create a Cloud Bigtable instance](https://cloud.google.com/bigtable/docs/quickstart-cbt#create-instance)) or `gcloud beta bigtable instances`.

```
BIGTABLE_SPARK_CLUSTER_ID=your-bigtable-cluster-id
BIGTABLE_SPARK_CLUSTER_ZONE=your-bigtable-zone-id
BIGTABLE_SPARK_INSTANCE_DISPLAY_NAME=your-bigtable-display-name

gcloud bigtable instances create $BIGTABLE_SPARK_INSTANCE_ID \
  --cluster=$BIGTABLE_SPARK_CLUSTER_ID \
  --cluster-zone=$BIGTABLE_SPARK_CLUSTER_ZONE \
  --display-name=$BIGTABLE_SPARK_INSTANCE_DISPLAY_NAME \
  --instance-type=DEVELOPMENT
```

Check out the available Cloud Bigtable instances and make sure yours is listed.

```
gcloud bigtable instances list
```

### Create Table

Create the table.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  createtable $BIGTABLE_SPARK_WORDCOUNT_TABLE \
  "families=cf"
```

List the available tables.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  ls
```
Output should be:
```
wordcount
```

### Submit Wordcount

```
$SPARK_HOME/bin/spark-submit \
  --packages org.apache.hbase.connectors.spark:hbase-spark:1.0.0 \
  --class example.Wordcount \
  $BIGTABLE_SPARK_ASSEMBLY_JAR \
  $BIGTABLE_SPARK_PROJECT_ID $BIGTABLE_SPARK_INSTANCE_ID \
  $BIGTABLE_SPARK_WORDCOUNT_TABLE $BIGTABLE_SPARK_WORDCOUNT_FILE
```

### Verify

Count the number of rows in the `BIGTABLE_SPARK_WORDCOUNT_TABLE` table. There should be 88 rows.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  count $BIGTABLE_SPARK_WORDCOUNT_TABLE
```
Output should be:
```
88
```

### Delete Cloud Bigtable Instance

Use `cbt listinstances` to list existing Bigtable instances.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  listinstances
```

There should be at least `BIGTABLE_SPARK_INSTANCE_ID` instance. Delete it using `cbt deleteinstance`.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  deleteinstance $BIGTABLE_SPARK_INSTANCE_ID
```

## Run Wordcount with Cloud Dataproc

This section describes how to run [example.Wordcount](src/main/scala/example/Wordcount.scala) with [Google Cloud Dataproc](https://cloud.google.com/dataproc/).

Start afresh and re-create all the resources (a Bigtable instance, tables).

**TIP**: Read [Quickstart using the gcloud command-line tool](https://cloud.google.com/dataproc/docs/quickstarts/quickstart-gcloud) that shows how to use the Google Cloud SDK `gcloud` command-line tool to create a Google Cloud Dataproc cluster and more.

### Configure Environment

```
BIGTABLE_SPARK_PROJECT_ID=your-project-id
BIGTABLE_SPARK_INSTANCE_ID=your-instance-id

BIGTABLE_SPARK_DATAPROC_CLUSTER=your-dataproc-cluster
BIGTABLE_SPARK_DATAPROC_REGION=your-dataproc-region

BIGTABLE_SPARK_CLUSTER_ID=your-bigtable-cluster-id
BIGTABLE_SPARK_CLUSTER_ZONE=your-bigtable-cluster-zone
BIGTABLE_SPARK_INSTANCE_DISPLAY_NAME=your-bigtable-display-name

BIGTABLE_SPARK_WORDCOUNT_TABLE=wordcount
BIGTABLE_SPARK_BUCKET_NAME=gs://[your-bucket-name]
BIGTABLE_SPARK_ASSEMBLY_JAR=target/scala-2.11/bigtable-spark-samples-assembly-0.1.jar
```

**NOTE**: `BIGTABLE_SPARK_DATAPROC_REGION` should point to your region. Read [Available regions and zones](https://cloud.google.com/compute/docs/regions-zones#available) in the official documentation.

### Authenticate

Authenticate to a Google Cloud Platform API using service or user accounts.
Learn about [authenticating to a GCP API](https://cloud.google.com/docs/authentication/) in the Google Cloud documentation.

**NOTE**: In most situations, we recommend [authenticating as a service account](https://cloud.google.com/docs/authentication/production) to a Google Cloud Platform (GCP) API.

```
GOOGLE_APPLICATION_CREDENTIALS=/your/service/account.json
```

### Upload File to Cloud Storage

One notable change (compared to the earlier executions) is that the example uses [Cloud Storage](https://cloud.google.com/storage).

**TIP**: Read [Quickstart: Using the gsutil tool](https://cloud.google.com/storage/docs/quickstart-gsutil) in the official documentation.

1. Create a bucket.

    ```text
    gsutil mb \
      -b on \
      -l $BIGTABLE_SPARK_DATAPROC_REGION \
      -p $BIGTABLE_SPARK_PROJECT_ID \
      $BIGTABLE_SPARK_BUCKET_NAME
    ```

1. Upload an input file into the bucket.

    ```text
    gsutil cp src/test/resources/Romeo-and-Juliet-prologue.txt $BIGTABLE_SPARK_BUCKET_NAME
    ```

    If successful, the command returns:

    ```text
    Copying file://src/test/resources/Romeo-and-Juliet-prologue.txt [Content-Type=text/plain]...
    / [1 files][  629.0 B/  629.0 B]
    Operation completed over 1 objects/629.0 B.
    ```

1. List contents of the bucket.

    ```text
    $ gsutil ls $BIGTABLE_SPARK_BUCKET_NAME
   
    gs://[your-bucket-name]/Romeo-and-Juliet-prologue.txt
    ```

### Create Dataproc Cluster

```
gcloud dataproc clusters create $BIGTABLE_SPARK_DATAPROC_CLUSTER \
  --region=$BIGTABLE_SPARK_DATAPROC_REGION \
  --zone=$BIGTABLE_SPARK_CLUSTER_ZONE \
  --project=$BIGTABLE_SPARK_PROJECT_ID \
  --image-version=1.4
```

Please note that the examples use Dataproc 1.4.

For the list of available Dataproc image versions visit [Dataproc Image version list](https://cloud.google.com/dataproc/docs/concepts/versioning/dataproc-versions).

List the clusters and make sure that `BIGTABLE_SPARK_DATAPROC_CLUSTER` is among them.

```
gcloud dataproc clusters list \
  --region=$BIGTABLE_SPARK_DATAPROC_REGION
```

### Configure Cloud Bigtable

1. Create Cloud Bigtable Instance as described in [Create Cloud Bigtable Instance](#create-cloud-bigtable-instance)

1. Create the table as described in [Create Table](#create-table)

### Submit Wordcount

Submit Wordcount to the Dataproc instance.

```
gcloud dataproc jobs submit spark \
  --cluster=$BIGTABLE_SPARK_DATAPROC_CLUSTER \
  --region=$BIGTABLE_SPARK_DATAPROC_REGION \
  --class=example.Wordcount \
  --jars=$BIGTABLE_SPARK_ASSEMBLY_JAR \
  --properties=spark.jars.packages='org.apache.hbase.connectors.spark:hbase-spark:1.0.0' \
  -- \
  $BIGTABLE_SPARK_PROJECT_ID $BIGTABLE_SPARK_INSTANCE_ID \
  $BIGTABLE_SPARK_WORDCOUNT_TABLE $BIGTABLE_SPARK_BUCKET_NAME/Romeo-and-Juliet-prologue.txt
```

It may take some time to see any progress and may seem to be idle. You may want to use `--verbosity` global option with `debug` to be told about progress earlier.

Eventually, you should see the following messages:

```text
Job [joibId] submitted.
Waiting for job output...
```

### Verify

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  read $BIGTABLE_SPARK_WORDCOUNT_TABLE
```

### Clean Up

Delete the Bigtable instance.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  deleteinstance $BIGTABLE_SPARK_INSTANCE_ID
```

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  listinstances
```

Delete the Dataproc cluster.

```
gcloud dataproc clusters delete $BIGTABLE_SPARK_DATAPROC_CLUSTER \
  --region=$BIGTABLE_SPARK_DATAPROC_REGION \
  --project=$BIGTABLE_SPARK_PROJECT_ID
```

```
gcloud dataproc clusters list \
  --region=$BIGTABLE_SPARK_DATAPROC_REGION
```

Remove the input file in the bucket and the bucket itself.

```
gsutil rm $BIGTABLE_SPARK_BUCKET_NAME/Romeo-and-Juliet-prologue.txt
gsutil rb $BIGTABLE_SPARK_BUCKET_NAME
```

## Running Integration Test

Start afresh and re-create all the resources (a Bigtable instance, the tables).

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  listinstances
```

There should be no Bigtable instances listed. Create one.

```
gcloud bigtable instances create $BIGTABLE_SPARK_INSTANCE_ID \
  --cluster=$BIGTABLE_SPARK_CLUSTER_ID \
  --cluster-zone=$BIGTABLE_SPARK_CLUSTER_ZONE \
  --display-name=$BIGTABLE_SPARK_INSTANCE_DISPLAY_NAME \
  --instance-type=DEVELOPMENT
```

Create the tables.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  createtable $BIGTABLE_SPARK_WORDCOUNT_TABLE \
  "families=cf"
```

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  createtable $BIGTABLE_SPARK_COPYTABLE_TABLE \
  "families=cf"
```

List tables.

```
$ cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  ls
copytable
wordcount
```

### Clean Up

Delete the Bigtable instance.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  deleteinstance $BIGTABLE_SPARK_INSTANCE_ID
```
