package com.github.edge.roman.spear

import com.github.edge.roman.spear.connectors.targetFS.{FiletoFS, JDBCtoFS}
import com.github.edge.roman.spear.connectors.targetjdbc.{FiletoJDBC, JDBCtoJDBC}

object SpearConnector {

  def init: SpearConnector = new SpearConnector

  class SpearConnector {
    private var sourceType: String = null
    private var sourceFormat: String = null
    private var destType: String = null
    private var destFormat: String = null
    private var connectorName: String = null

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

    def withName(connectorName: String="defaultConnector"): SpearConnector = {
      this.connectorName = connectorName
      this
    }

    def getConnector: Connector = {
      (sourceType, destType) match {
        case ("file", "relational") => new FiletoJDBC(sourceFormat, destFormat).init(connectorName)
        case ("relational", "relational") => new JDBCtoJDBC(sourceFormat, destFormat).init(connectorName)
        case ("file", "FS") => new FiletoFS(sourceFormat, destFormat).init(connectorName)
        case ("relational", "FS") => new JDBCtoFS(sourceFormat, destFormat).init(connectorName)
      }
    }
  }
}
