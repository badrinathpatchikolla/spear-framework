package com.github.edge.roman.spear.connectors

import com.github.edge.roman.spear.Connector
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.types.StructType

import java.util.Properties

trait TargetFSConnector extends Connector {

  //used for jdbc source but not for files .Thus left un-implemented
  override def sourceSql(params: Map[String, String], sqlText: String): Connector = ???

  //used for jdbc source but not for files .Thus left un-implemented
  override def source(sourceObject: String, params: Map[String, String], schema: StructType): Connector = ???

  override def targetJDBC(tableName: String, props: Properties, saveMode: SaveMode): Unit = throw new NoSuchMethodException("method targetJDBC not compatible for given targetType FS")

  //left unimplemented here as it is not used fof FS to FS
  override def targetFS(destinationFilePath: String, saveAsTable: String, saveMode: SaveMode): Unit = ???

  //left unimplemented here as it is not used fof FS to FS
  override def targetFS(destinationPath: String, params: Map[String, String]): Unit = ???

  override def targetSql(sqlText: String, props: Properties, saveMode: SaveMode): Unit = ???

}
