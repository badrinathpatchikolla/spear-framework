package com.github.edge.roman.spear.connectors

import com.github.edge.roman.spear.Connector
import org.apache.spark.sql.SaveMode

import java.util.Properties

trait TargetFSConnector extends Connector {

  //used for jdbc source but not for files .Thus left un-implemented
  override def sourceSql(params: Map[String, String], sqlText: String): Connector = ???

  override def targetJDBC(tableName: String, props: Properties, saveMode: SaveMode): Unit = throw new NoSuchMethodException("method targetJDBC not compatible for given targetType FS")

   override def targetFS(destinationPath: String, params: Map[String, String]): Unit = ???

  override def targetFS(destinationFilePath: String): Unit = ???
}
