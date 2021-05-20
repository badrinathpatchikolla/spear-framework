package com.github.edge.roman.spear.connectors.targetjdbc

import com.github.edge.roman.spear.{Connector, SpearConnector}
import com.github.edge.roman.spear.connectors.TargetJDBCConnector
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.types.StructType

import java.util.Properties

class JDBCtoJDBC(sourceFormat: String, destFormat: String) extends TargetJDBCConnector {


  override def source(sourceObject: String, params: Map[String, String], schema: StructType): Connector = {
    val paramsWithSchema = params + ("customSchema" -> schema.toString())
    source(sourceObject, paramsWithSchema)
  }

  override def source(tableName: String, params: Map[String, String]): JDBCtoJDBC = {
    sourceFormat match {
      case "soql"|"saql" =>
        throw new Exception(tableName +"object cannot be loaded directly...instead use sourceSql function with soql query")
      case _ =>
        val df = SpearConnector.spark.read.format(sourceFormat).option("dbtable", tableName).options(params).load()
        this.df = df
    }
    this
  }

  override def sourceSql(params: Map[String, String], sqlText: String): JDBCtoJDBC = {
    sourceFormat match {
      case "soql"|"saql" =>
        if(sourceFormat.equals("soql")) {
          SpearConnector.spark.read.format("com.springml.spark.salesforce").option("soql", s"$sqlText").options(params).load()
        }else{
          SpearConnector.spark.read.format("com.springml.spark.salesforce").option("saql", s"$sqlText").options(params).load()
        }
          case _ =>
        val _df = SpearConnector.spark.read.format(sourceFormat).option("dbtable", s"($sqlText)temp").options(params).load()
        this.df = _df
    }
    this
  }

  override def transformSql(sqlText: String): JDBCtoJDBC = {
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
      case "saql" =>
        this.df.write.format("com.springml.spark.salesforce")
          .option("username", props.get("username").toString)
          .option("password", props.get("password").toString)
          .option("datasetName", tableName).save()
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
