package com.github.edge.roman.spear

import com.github.edge.roman.spear.commons.SpearCommons
import com.github.edge.roman.spear.connectors.AbstractConnector
import com.github.edge.roman.spear.connectors.targetFS.{FiletoFS, JDBCtoFS, StreamtoFS}
import com.github.edge.roman.spear.connectors.targetjdbc.{FiletoJDBC, JDBCtoJDBC, StreamtoJDBC}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object SpearConnector {

  val sparkConf = new SparkConf
  lazy val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

  def createConnector(name: String = SpearCommons.Default): SpearConnector = {
    sparkConf.setAppName(name)
    new SpearConnector
  }

  //companion class spear-connector
  class SpearConnector {
    private var sourceType: String = _
    private var sourceFormat: String = _
    private var destType: String = _
    private var destFormat: String = _

    def source(sourceType: String, sourceFormat: String): SpearConnector = {
      this.sourceType = sourceType
      this.sourceFormat = sourceFormat
      this
    }

    def target(targetType: String, targetFormat: String): SpearConnector = {
      this.destType = targetType
      this.destFormat = targetFormat
      this
    }

    def getConnector: AbstractConnector = {
      (sourceType, destType) match {
        case ("file", "relational") => new FiletoJDBC(sourceFormat, destFormat)
        case ("relational", "relational") => new JDBCtoJDBC(sourceFormat, destFormat)
        case ("stream", "relational") => new StreamtoJDBC(sourceFormat, destFormat)
        case ("file", "FS") => new FiletoFS(sourceFormat, destFormat)
        case ("relational", "FS") => new JDBCtoFS(sourceFormat, destFormat)
        case ("stream", "FS") => new StreamtoFS(sourceFormat, destFormat)
        case ("FS", "FS") => new FiletoFS(sourceFormat, destFormat)
        case (_, _) => throw new Exception(SpearCommons.InvalidParams)
      }
    }
  }

}
