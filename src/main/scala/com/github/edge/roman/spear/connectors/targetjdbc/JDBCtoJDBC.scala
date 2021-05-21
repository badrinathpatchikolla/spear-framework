package com.github.edge.roman.spear.connectors.targetjdbc

import com.github.edge.roman.spear.commons.SpearCommons
import com.github.edge.roman.spear.{Connector, SpearConnector}
import com.github.edge.roman.spear.connectors.{AbstractConnector, TargetJDBCConnector}
import org.apache.spark.sql.{DataFrame, SaveMode}
import org.apache.spark.sql.types.StructType

import java.util.Properties

class JDBCtoJDBC(sourceFormat: String, destFormat: String) extends AbstractConnector with TargetJDBCConnector {


  override def source(sourceObject: String, params: Map[String, String], schema: StructType): Connector = {
    val paramsWithSchema = params + ("customSchema" -> schema.toString())
    source(sourceObject, paramsWithSchema)
  }

  override def source(tableName: String, params: Map[String, String]): JDBCtoJDBC = {
    sourceFormat match {
      case "soql" | "saql" =>
        throw new NoSuchMethodException(s"Salesforce object ${tableName} cannot be loaded directly.Instead use sourceSql function with soql/saql query to load the data")
      case _ =>
        val df = SpearConnector.spark.read.format(sourceFormat).option("dbtable", tableName).options(params).load()
        this.df = df
    }
    logger.info(s"Reading source table: ${tableName} with format: ${sourceFormat} status:${SpearCommons.SuccessStatus}")
    show()
    this
  }

  override def sourceSql(params: Map[String, String], sqlText: String): JDBCtoJDBC = {
    sourceFormat match {
      case "soql" | "saql" =>
        var _df: DataFrame = null
        if (sourceFormat.equals("soql")) {
          _df = SpearConnector.spark.read.format(SpearCommons.SalesforceFormat).option("soql", s"$sqlText").options(params).load()
        } else {
          _df=SpearConnector.spark.read.format(SpearCommons.SalesforceFormat).option("saql", s"$sqlText").options(params).load()
        }
        this.df=_df
      case _ =>
        val _df = SpearConnector.spark.read.format(sourceFormat).option("dbtable", s"($sqlText)temp").options(params).load()
        this.df = _df
    }
    logger.info(s"Executing source query: ${sqlText} with format: ${sourceFormat} status:${SpearCommons.SuccessStatus}")
    show()
    this
  }

  override def targetJDBC(tableName: String, props: Properties, saveMode: SaveMode): Unit = {
    destFormat match {
      case "soql" =>
        this.df.write.format(SpearCommons.SalesforceFormat)
          .option("username", props.get("username").toString)
          .option("password", props.get("password").toString)
          .option("sfObject", tableName).save()
      case "saql" =>
        this.df.write.format(SpearCommons.SalesforceFormat)
          .option("username", props.get("username").toString)
          .option("password", props.get("password").toString)
          .option("datasetName", tableName).save()
      case _ =>
        this.df.write.mode(saveMode).jdbc(props.get("url").toString, tableName, props)
    }
    logger.info(s"Write data to table/object ${tableName} completed with status:${SpearCommons.SuccessStatus} ")
    show()
  }


  override def targetSql(sqlText: String, props: Properties, saveMode: SaveMode): Unit = {
    this.df.createOrReplaceTempView("TEMP")
    this.df.write.mode(saveMode)
      .jdbc(props.get("url").toString, "TEMP", props)
  }
}
