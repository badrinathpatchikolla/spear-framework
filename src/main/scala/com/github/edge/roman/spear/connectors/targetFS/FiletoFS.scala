package com.github.edge.roman.spear.connectors.targetFS

import com.databricks.spark.xml.XmlDataFrameReader
import com.github.edge.roman.spear.{Connector, SpearConnector}
import com.github.edge.roman.spear.commons.{ConnectorCommon, SpearCommons}
import com.github.edge.roman.spear.commons.ConnectorCommon.logger
import com.github.edge.roman.spear.connectors.{AbstractConnector, AbstractTargetFSConnector}
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.types.StructType

class FiletoFS(sourceFormat: String, destFormat: String) extends AbstractTargetFSConnector(sourceFormat, dateFormat) {

  override def source(sourceFilePath: String, params: Map[String, String]): FiletoFS = {
    logger.info(s"Connector to Target: File System with Format: ${destFormat} from Source: ${sourceFilePath} with Format: ${sourceFilePath} started running !!")
    this.df = ConnectorCommon.sourceFile(sourceFormat, sourceFilePath, params)
    logger.info(s"Reading source file: ${sourceFilePath} with format: ${sourceFormat} status:${SpearCommons.SuccessStatus}")
    show()
    this
  }
  def sourceSql(params: Map[String, String], sqlText: String): Connector =throw new NoSuchMethodException(s"method sourceSql is not supported for given sourceType file for connector type FiletoFS" )
}