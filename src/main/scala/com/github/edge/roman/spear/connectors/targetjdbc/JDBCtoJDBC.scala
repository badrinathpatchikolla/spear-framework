package com.github.edge.roman.spear.connectors.targetjdbc

import com.github.edge.roman.spear.{Connector, SpearConnector}
import com.github.edge.roman.spear.connectors.TargetJDBCConnector
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.SaveMode
import org.apache.log4j.Logger
import org.apache.spark.sql.types.StructType

import java.util.Properties

class JDBCtoJDBC(sourceFormat: String, destFormat: String) extends TargetJDBCConnector with LazyLogging{


  override def source(sourceObject: String, params: Map[String, String], schema: StructType): Connector = {
    val paramsWithSchema = params+("customSchema" -> schema.toString())
    source(sourceObject,paramsWithSchema)
  }

  override def source(tableName: String, params: Map[String, String]): JDBCtoJDBC = {
    val df = SpearConnector.spark.read.format(sourceFormat).option("dbtable", tableName).options(params).load()
    this.df = df
    this
  }

  override def sourceSql(params: Map[String, String], sqlText: String): JDBCtoJDBC = {
    val _df = SpearConnector.spark.read.format(sourceFormat).option("dbtable", s"($sqlText)temp").options(params).load()
    this.df = _df
    this
  }

  override def transformSql(sqlText: String): JDBCtoJDBC = {
    val _df = this.df.sqlContext.sql(sqlText)
    this.df = _df
    this
  }

  override def targetJDBC(tableName: String, props: Properties, saveMode: SaveMode): Unit = {
    this.df.write.mode(saveMode).jdbc(props.get("url").toString, tableName, props)
    showTargetData(tableName: String, props: Properties)
  }

  def showTargetData(tableName: String, props: Properties): Unit = {
    SpearConnector.spark.read.jdbc(props.get("url").toString, tableName, props).show(10, false)
  }
}
