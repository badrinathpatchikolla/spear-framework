package com.github.edge.roman.spear.connectors

import com.github.edge.roman.spear.Connector
import org.apache.spark.sql.SaveMode

import java.util.Properties

trait TargetFSConnector extends Connector {

  override def sourceSql(params: Map[String, String], sqlText: String): Connector = ???

  override def targetJDBC(tableName: String, props: Properties, saveMode: SaveMode): Unit = ???

   override def targetFS(destinationPath: String, params: Map[String, String]): Connector = ???

}
