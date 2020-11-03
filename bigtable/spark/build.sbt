/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

name := "bigtable-spark-samples"

version := "0.1"

// Versions to match Dataproc 1.4
// https://cloud.google.com/dataproc/docs/concepts/versioning/dataproc-release-1.4
scalaVersion := "2.11.12"
val sparkVersion = "2.4.6"
val bigtableVersion = "1.16.0"
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
  "org.apache.hbase.connectors.spark" % "hbase-spark" % "1.0.0" % Provided,
  "com.google.cloud.bigtable" % "bigtable-hbase-2.x-hadoop" % bigtableVersion
)

val scalatestVersion = "3.2.0"
libraryDependencies += "org.scalactic" %% "scalactic" % scalatestVersion
libraryDependencies += "org.scalatest" %% "scalatest" % scalatestVersion % "test"
test in assembly := {}

val fixes = Seq(
  // Fix for Exception: Incompatible Jackson 2.9.2
  // Version conflict between HBase and Spark
  // Forcing the version to match Spark
  // FIXME Would that work with dependencyOverrides?
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.10",
  // Required by 'value org.apache.hadoop.hbase.spark.HBaseContext.dstream'
  "org.apache.spark" %% "spark-streaming" % sparkVersion % Provided,
)
libraryDependencies ++= fixes

// Excluding duplicates for the uber-jar
// There are other deps to provide necessary packages
excludeDependencies ++= Seq(
  ExclusionRule(organization = "asm", "asm"),
  ExclusionRule(organization = "commons-beanutils", "commons-beanutils"),
  ExclusionRule(organization = "commons-beanutils", "commons-beanutils-core"),
  ExclusionRule(organization = "org.mortbay.jetty", "servlet-api")
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("mozilla", "public-suffix-list.txt") => MergeStrategy.first
  case PathList("google", xs @ _*) => xs match {
    case ps @ (x :: xs) if ps.last.endsWith(".proto") => MergeStrategy.first
    case _ => MergeStrategy.deduplicate
  }
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
    // FIXME Make sure first is OK (it's worked well so far)
    MergeStrategy.first
}
