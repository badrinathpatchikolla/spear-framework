package com.github.edge.roman.spear.connectors.targetjdbc

import com.github.edge.roman.spear.{Connector, SpearConnector}
import com.github.edge.roman.spear.connectors.TargetJDBCConnector
import com.mongodb.spark.config.ReadConfig
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.types.StructType
import com.mongodb.spark.sql.SparkSessionFunctions

import java.util.Properties

class NoSQLtoJDBC(sourceFormat: String, destFormat: String) extends TargetJDBCConnector {
  override def source(sourceObject: String, params: Map[String, String]): Connector = {
    val _df = SparkSessionFunctions(this.df.sparkSession).loadFromMongoDB(ReadConfig(Map("uri" -> params.get("uri").toString.concat(s".$sourceObject"))))
    this.df = _df
    this
  }

  override def source(sourceObject: String, params: Map[String, String], schema: StructType): Connector = {
    val _params = Map("uri" -> params.get("uri").toString.concat(s".$sourceObject"))
    val paramsWithSchema = _params + ("customSchema" -> schema.toString())
    source(sourceObject, paramsWithSchema)
  }

  override def transformSql(sqlText: String): Connector = {
    val _df = this.df.sqlContext.sql(sqlText)
    this.df = _df
    this
  }

  override def targetJDBC(tableName: String, props: Properties, saveMode: SaveMode): Unit = {
    destFormat match {
      case "soql" =>
        this.df.write.format("com.springml.spark.salesforce")
          .option("username", props.get("username").toString)
          .option("password", props.get("password").toString)
          .option("sfObject", tableName).save()
      case _ =>
        this.df.write.mode(saveMode).jdbc(props.get("url").toString, tableName, props)
        showTargetData(tableName: String, props: Properties)
    }
  }

  def showTargetData(tableName: String, props: Properties): Unit = {
    SpearConnector.spark.read.jdbc(props.get("url").toString, tableName, props).show(10, false)
  }

  override def targetSql(sqlText: String, props: Properties, saveMode: SaveMode): Unit = {
    this.df.createOrReplaceTempView("TEMP")
    this.df.write.mode(saveMode)
      .jdbc(props.get("url").toString, "TEMP", props)
  }
}
