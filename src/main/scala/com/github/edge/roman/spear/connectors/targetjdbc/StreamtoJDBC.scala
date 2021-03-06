package com.github.edge.roman.spear.connectors.targetjdbc

import com.github.edge.roman.spear.{Connector, SpearConnector}
import com.github.edge.roman.spear.connectors.AbstractTargetJDBCConnector
import org.apache.spark.sql.functions.from_json
import org.apache.spark.sql.{DataFrame, SaveMode}
import org.apache.spark.sql.types.StructType
import SpearConnector.spark.implicits._

import java.util.Properties

class StreamtoJDBC(sourceFormat: String, destFormat: String) extends AbstractTargetJDBCConnector(sourceFormat, destFormat) {

  override def source(sourceObject: String, params: Map[String, String], schema: StructType): Connector = {
    logger.info(s"Connector to Target: JDBC with Format: ${destFormat} from Source Stream:${sourceObject} started running!!")
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
      .load(sourceObject)
    this.df = _df
    this
  }


  override def targetJDBC(tableName: String, props: Properties, saveMode: SaveMode): Unit = {
    this.df.writeStream
      .foreachBatch { (batchDF: DataFrame, _: Long) =>
        batchDF.write
          .mode(saveMode)
          .jdbc(props.get("url").toString, tableName, props)
      }.start()
      .awaitTermination()
  }

  override def targetSql(sqlText: String, props: Properties, saveMode: SaveMode): Unit = {
    this.df.writeStream
      .foreachBatch { (batchDF: DataFrame, _: Long) =>
        batchDF.sqlContext.sql(sqlText)
      }.start()
      .awaitTermination()
  }
}
