package com.github.edge.roman.spear.connectors.targetFS

import com.github.edge.roman.spear.{Connector, SpearConnector}
import com.github.edge.roman.spear.connectors.AbstractTargetFSConnector
import org.apache.spark.sql.functions.{date_format, from_json}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SaveMode}
import SpearConnector.spark.implicits._

class StreamtoFS(sourceFormat: String, destFormat: String) extends AbstractTargetFSConnector(sourceFormat, destFormat) {

  override def source(sourceObject: String, params: Map[String, String], schema: StructType): Connector = {
    logger.info(s"Connector to Target: FileSystem with Format: ${destFormat} from Source Stream: ${sourceObject}} started running!!")
    sourceFormat match {
      case "kafka" =>
        val _df = SpearConnector.spark
          .readStream
          .format(sourceFormat)
          .option("subscribe", sourceObject)
          .options(params)
          .load()
          .selectExpr("CAST(value AS STRING)").as[String]
          .select(from_json($"value", schema).as("data"))
          .select("data.*")
        this.df = _df
      case _ =>
        val _df = SpearConnector.spark
          .readStream
          .format(sourceFormat)
          .schema(schema)
          .options(params)
          .load(sourceObject + "/*." + sourceFormat)
        this.df = _df

    }
    this
  }

  override def source(sourceObject: String, params: Map[String, String]): Connector = {
    val _df = SpearConnector.spark
      .readStream
      .format(sourceFormat)
      .options(params)
      .load()
    this.df = _df
    this
  }

  override def targetFS(destinationFilePath: String, tableName: String, saveMode: SaveMode): Unit = {
    this.df.writeStream
      .foreachBatch { (batchDF: DataFrame, _: Long) =>
        if (destinationFilePath.isEmpty) {
          batchDF.write.format(destFormat).mode(saveMode).saveAsTable(tableName)
        } else {
          batchDF.write.format(destFormat).mode(saveMode).option("path", destinationFilePath).saveAsTable(tableName)
        }
        val targetDF = SpearConnector.spark.sql("select * from " + tableName)
        targetDF.show(this.numRows, false)
      }.start()
      .awaitTermination()
  }


  override def targetFS(destinationFilePath: String, saveMode: SaveMode): Unit = {
    this.df.writeStream
      .foreachBatch { (batchDF: DataFrame, _: Long) =>
        if (destinationFilePath.isEmpty) {
          throw new Exception("Empty file path specified:" + destinationFilePath)
        } else {
          batchDF.write.format(destFormat).mode(saveMode).option("path", destinationFilePath).save()
        }
      }.start()
      .awaitTermination()
  }
}
