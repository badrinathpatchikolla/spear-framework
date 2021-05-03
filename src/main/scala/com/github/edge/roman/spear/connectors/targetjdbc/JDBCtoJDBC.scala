package com.github.edge.roman.spear.connectors.targetjdbc
import com.github.edge.roman.spear.connectors.TargetJDBCConnector
import org.apache.spark.sql.SaveMode

import java.util.Properties

class JDBCtoJDBC(sourceFormat: String, destFormat: String) extends TargetJDBCConnector {
  override def source(tableName: String, params: Map[String, String]): JDBCtoJDBC = {
    val df = this.sparkSession.read.format(sourceFormat).option("dbtable", tableName).options(params).load()
    this.df = df
    df.show(10, false)
    this
  }

  override def sourceSql(params: Map[String, String], sqlText: String): JDBCtoJDBC = {
    logger.info("Executing source sql query: " + sqlText)
    val _df = sparkSession.read.format(sourceFormat).option("dbtable", s"($sqlText)temp").options(params).load()
    this.df = _df
    this
  }

  override def transformSql(sqlText: String): JDBCtoJDBC = {
    logger.info("Data after transformation using the SQL : " + sqlText)
    val _df = this.df.sqlContext.sql(sqlText)
    _df.show(10, false)
    this.df = _df
    this
  }

  override def targetJDBC(tableName: String, props: Properties, saveMode: SaveMode): Unit = {
    logger.info("Writing data to target table: " + tableName)
    this.df.write.mode(saveMode).jdbc(props.get("url").toString, tableName, props)
    showTargetData(tableName: String, props: Properties)
  }

  def showTargetData(tableName: String, props: Properties): Unit = {
    logger.info("Showing data in target table  : " + tableName)
    sparkSession.read.jdbc(props.get("url").toString, tableName, props).show(10, false)
  }
}
