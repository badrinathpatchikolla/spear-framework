package com.github.edge.roman.spear


import com.github.edge.roman.spear.connectors.targetFS.{FiletoFS, JDBCtoFS, StreamtoFS}
import com.github.edge.roman.spear.connectors.targetjdbc.{FiletoJDBC, JDBCtoJDBC, StreamtoJDBC}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object SpearConnector {

  val sparkConf = new SparkConf
  lazy val spark = SparkSession.builder().config(sparkConf).getOrCreate()

  def createConnector(name: String): SpearConnector = {
    sparkConf.setAppName(name)
    new SpearConnector
  }

  //companion class spearconnector
  class SpearConnector {
    private var sourceType: String = null
    private var sourceFormat: String = null
    private var destType: String = null
    private var destFormat: String = null

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

    def getConnector: Connector = {
      (sourceType, destType) match {
        case ("file", "relational") => new FiletoJDBC(sourceFormat, destFormat)
        case ("relational", "relational") => new JDBCtoJDBC(sourceFormat, destFormat)
        case ("file", "FS") => new FiletoFS(sourceFormat, destFormat)
        case ("relational", "FS") => new JDBCtoFS(sourceFormat, destFormat)
        case ("stream", "relational") => new StreamtoJDBC(sourceFormat, destFormat)
        case ("stream", "FS") => new StreamtoFS(sourceFormat, destFormat)
        case (_, _) => throw new Exception()
      }
    }
  }
}
