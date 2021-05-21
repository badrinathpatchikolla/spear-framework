package com.github.edge.roman.spear.connectors

import com.github.edge.roman.spear.commons.SpearCommons
import com.github.edge.roman.spear.{Connector, SpearConnector}
import org.apache.log4j.Logger
import org.apache.spark.sql.DataFrame

abstract class AbstractConnector extends Connector {
  val logger: Logger = Logger.getLogger(this.getClass.getName)
  var df: DataFrame = _
  var verboseLogging: Boolean = false
  var numRows=10

  def setVeboseLogging(enable: Boolean): Unit = {
    this.verboseLogging = enable
  }

  def saveAs(alias: String): Connector = {
    this.df.createOrReplaceTempView(alias)
    logger.info(s"Saving data as temporary table:${alias} ${SpearCommons.SuccessStatus}")
    this
  }

  def cacheData(): Connector = {
    this.df.cache()
    logger.info(s"Cached data in dataframe: ${SpearCommons.SuccessStatus}")
    this
  }

  def repartition(n: Int): Connector = {
    this.df.repartition(n)
    logger.info(s"Repartition data in dataframe: ${SpearCommons.SuccessStatus}")
    this
  }

  def coalesce(n: Int): Connector = {
    this.df.coalesce(n)
    logger.info(s"Coalesce data in dataframe: ${SpearCommons.SuccessStatus}")
    this
  }

  def toDF: DataFrame = this.df

  def stop(): Unit = SpearConnector.spark.stop()

  override def transformSql(sqlText: String): Connector = {
    this.df = this.df.sqlContext.sql(sqlText)
    logger.info(s"Executing tranformation sql: ${sqlText} status :${SpearCommons.SuccessStatus}")
    show()
    this
  }

  def show():Unit= if (this.verboseLogging) this.df.show(this.numRows, false)
}
