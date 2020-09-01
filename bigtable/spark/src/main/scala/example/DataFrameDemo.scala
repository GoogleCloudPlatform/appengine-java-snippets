package example

import org.apache.hadoop.hbase.spark.datasources.{HBaseSparkConf, HBaseTableCatalog}

object DataFrameDemo extends App {

  val appName = this.getClass.getSimpleName.replace("$", "")
  println(s"$appName Spark application is starting up...")

  import scopt.OParser
  val builder = OParser.builder[CmdOpts]
  val parser = {
    import builder._
    OParser.sequence(
      programName(appName),
      head(appName, "1.0"),
      opt[String]('p', "project")
        .required()
        .valueName("<projectId>")
        .action { case (x, c) => c.copy(projectId = x) }
        .text("projectId is required")
    )
  }
  val cmdOpts = OParser.parse(parser, args, CmdOpts()).getOrElse {
    sys.exit(-1)
  }

  println(s"cmdOpts: $cmdOpts")

  val projectId = cmdOpts.projectId
  val instanceId = cmdOpts.instanceId
  val table = cmdOpts.table
  val numRecords = cmdOpts.numRecords

  import org.apache.spark.sql.SparkSession
  val spark = SparkSession.builder().master("local[*]").getOrCreate()
  println(s"Spark version: ${spark.version}")

  val catalog =
    s"""{
       |"table":{"namespace":"default", "name":"$table", "tableCoder":"PrimitiveType"},
       |"rowkey":"key",
       |"columns":{
       |"col0":{"cf":"rowkey", "col":"key", "type":"string"},
       |"col1":{"cf":"cf1", "col":"col1", "type":"boolean"},
       |"col2":{"cf":"cf2", "col":"col2", "type":"double"},
       |"col3":{"cf":"cf3", "col":"col3", "type":"int"}
       |}
       |}""".stripMargin

  // FIXME Explain the options
  // FIXME Where to find more options supported? Any docs?
  val opts = Map(
    HBaseTableCatalog.tableCatalog -> catalog,
    // FIXME Why is this 5?
    HBaseTableCatalog.newTable -> "5",
    HBaseSparkConf.USE_HBASECONTEXT -> "false") // accepts xml configs only

  // TODO Use a command-line option to switch between command line params and xml

  // Hack to specify HBase properties on command line
  // BEGIN
  // import org.apache.hadoop.hbase.HBaseConfiguration
  // val conf = HBaseConfiguration.create()
  // conf.set("google.bigtable.project.id", projectId)
  // conf.set("google.bigtable.instance.id", instanceId)
  import com.google.cloud.bigtable.hbase.BigtableConfiguration
  val conf = BigtableConfiguration.configure(projectId, instanceId)

  import org.apache.hadoop.hbase.spark.HBaseContext
  val hbaseContext = new HBaseContext(spark.sparkContext, conf)
  val opts_nouse = opts.filterNot { case (k, _) => k == HBaseSparkConf.USE_HBASECONTEXT }
  // END

  println(s"Writing $numRecords records to $table")
  import spark.implicits._
  (0 until numRecords)
    .map(BigtableRecord.apply)
    .toDF
    .write
    .format("org.apache.hadoop.hbase.spark")
    .options(opts_nouse)
    .save
  println(s"Writing to $table...DONE")

  println(s"Loading $table")
  spark
    .read
    .format("org.apache.hadoop.hbase.spark")
    .options(opts_nouse)
    .load
    .show(truncate = false)
  println(s"Loading $table...DONE")

}

case class BigtableRecord(
                           col0: String,
                           col1: Boolean,
                           col2: Double,
                           col3: Int)

object BigtableRecord {
  def apply(i: Int): BigtableRecord = {
    val s = s"""row${"%03d".format(i)}"""
    BigtableRecord(s,
      i % 2 == 0,
      i.toDouble,
      i)
  }
}

case class CmdOpts(
  projectId: String = "",
  instanceId: String = "",
  table: String = "dataframe-demo",
  numRecords: Int = 10)
