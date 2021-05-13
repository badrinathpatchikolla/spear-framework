package com.github.edge.roman.spear.connectors.targetFS
import com.github.edge.roman.spear.Connector
import com.github.edge.roman.spear.connectors.TargetFSConnector
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.types.StructType


class FiletoFS(sourceType: String,destFormat:String) extends TargetFSConnector {

  override def targetFS(target: String, objectName: String, saveMode: SaveMode): Unit = ???

  override def targetFS(destinationFilePath: String, saveMode: SaveMode): Unit = ???

  override def source(sourceObject: String, params: Map[String, String]): Connector = ???

  override def sourceSql(params: Map[String, String], sqlText: String): Connector = ???

  override def transformSql(sqlText: String): Connector = ???

  override def source(sourceObject: String, params: Map[String, String], schema: StructType): Connector = ???
}
