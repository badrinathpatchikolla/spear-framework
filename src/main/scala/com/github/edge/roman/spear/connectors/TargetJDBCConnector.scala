package com.github.edge.roman.spear.connectors

import com.github.edge.roman.spear.Connector
import org.apache.spark.sql.SaveMode


trait TargetJDBCConnector extends Connector {
  //sourceSql is only supported for databases where sql queries can be written/Thus it is left un-implemented here
  override def sourceSql(params: Map[String, String], sqlText: String): Connector = ???

  //Target FS not supported for targetType jdbc
  override def targetFS(destinationFilePath: String, saveAsTable: String, saveMode: SaveMode): Unit = throw new NoSuchMethodException("method targetFS() not supported for given targetType 'relational'")

  override def targetFS(destinationFilePath: String, saveMode: SaveMode): Unit = throw new NoSuchMethodException("method targetFS() not supported for given targetType 'relational'")

  override def targetFS(destinationFilePath: String,params: Map[String, String]): Unit = throw new NoSuchMethodException("method targetFS() not compatible for given targetType 'relational'")
}
