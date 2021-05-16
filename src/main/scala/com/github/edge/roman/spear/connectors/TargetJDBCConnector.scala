package com.github.edge.roman.spear.connectors

import com.github.edge.roman.spear.Connector
import org.apache.spark.sql.SaveMode


trait TargetJDBCConnector extends Connector {

  override def sourceSql(params: Map[String, String], sqlText: String): Connector = ???

  override def targetFS(destinationFilePath: String, saveAsTable: String, saveMode: SaveMode): Unit = ???

  override def targetFS(destinationFilePath: String, saveMode: SaveMode): Unit = ???

  def targetFS(destinationFilePath: String): Connector = throw new NoSuchMethodException("method targetFS not compatible for given targetType relational")

  def targetFS(destinationFilePath: String,params: Map[String, String]): Connector = throw new NoSuchMethodException("method targetFS not compatible for given targetType relational")

}
