package com.github.edge.roman.spear.connectors.targetFS
import com.github.edge.roman.spear.Connector
import com.github.edge.roman.spear.connectors.TargetFSConnector
import com.github.edge.roman.spear.util.{ADLSUtil, FTPUtil, GCSUtil, HDFSUtil, LocalFSUtil, S3Util, SMBUtil}
import org.apache.commons.io.IOUtils
import org.apache.log4j.Logger
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.types.StructType

import java.io.{File, FileOutputStream, InputStream}


class FiletoFS(sourceType: String, destFormat: String) extends TargetFSConnector {
  val logger: Logger = Logger.getLogger(this.getClass.getName)

  private val ftpUtil: FTPUtil = new FTPUtil
  private val s3Util: S3Util = new S3Util
  private val smbUtil: SMBUtil = new SMBUtil
  private val gcsUtil: GCSUtil = new GCSUtil
  private val adlsUtil: ADLSUtil = new ADLSUtil
  private val hdfsUtil: HDFSUtil = new HDFSUtil
  private val localFSUtil: LocalFSUtil = new LocalFSUtil

  private var inputStream: InputStream = null
  private var size: Long = 0L

  override def source(sourceObject: String, params: Map[String, String]): FiletoFS = {
    sourceType match {
      case "ftp" =>
        ftpUtil.configureClient(params)
        logger.info("FTP Client configured successfully")
        inputStream = ftpUtil.downloadFile(sourceObject)
        size = ftpUtil.getSize(sourceObject)
      case "aws" =>
        s3Util.configureClient(params)
        logger.info("Amazon S3 Client configured successfully")
        inputStream = s3Util.downloadFile(sourceObject)
        size = s3Util.getSize(sourceObject)
      case "smb" =>
        smbUtil.configureClient(params)
        logger.info("SMB Client configured successfully")
        inputStream = smbUtil.downloadFile(sourceObject)
        size = smbUtil.getSize(sourceObject)
      case "gcs" =>
        gcsUtil.configureClient(params)
        logger.info("Google Cloud Storage configured successfully")
        inputStream = gcsUtil.downloadFile(sourceObject)
      case "adls" =>
        adlsUtil.configureClient(params)
        logger.info("Azure Blob Storage configured successfully")
        inputStream = adlsUtil.downloadFile(sourceObject)
      case "hdfs" =>
        hdfsUtil.configureClient(params)
        logger.info("Hadoop File System configured successfully")
        inputStream = hdfsUtil.downloadFile(sourceObject)
      case "local" =>
        logger.info("Local File System configured successfully")
        inputStream = localFSUtil.downloadFile(sourceObject)
      case _ =>
        throw new Exception("Invalid source type provided or Not Supported.")
    }
    this
  }

  override def targetFS(destinationPath: String, params: Map[String, String]): FiletoFS = {
    destFormat match {
      case "local" =>
        localFSUtil.uploadFile(destinationPath,size,inputStream)
        logger.info("File upload successful")
      case "aws" =>
        s3Util.configureClient(params)
        s3Util.uploadFile(destinationPath, size, inputStream)
        logger.info("File upload successful")
      case "gcs" =>
        gcsUtil.configureClient(params)
        gcsUtil.uploadFile(destinationPath, size, inputStream)
        logger.info("File upload successful")
      case "adls" =>
        adlsUtil.configureClient(params)
        adlsUtil.uploadFile(destinationPath, size, inputStream)
        logger.info("File upload successful")
      case "hdfs" =>
        hdfsUtil.configureClient(params)
        hdfsUtil.uploadFile(destinationPath, size, inputStream)
        logger.info("File upload successful")
      case _ =>
        throw new Exception("Invalid destination type provided or Not Supported.")
    }
    this
  }

  override def targetFS(destinationPath: String): FiletoFS = {
    destFormat match {
      case "local" =>
        localFSUtil.uploadFile(destinationPath, size, inputStream)
        logger.info("File upload successful")
      case "aws" =>
        s3Util.uploadFile(destinationPath, size, inputStream)
        logger.info("File upload successful")
      case "gcs" =>
        gcsUtil.uploadFile(destinationPath, size, inputStream)
        logger.info("File upload successful")
      case "adls" =>
        adlsUtil.uploadFile(destinationPath, size, inputStream)
        logger.info("File upload successful")
      case "hdfs" =>
        hdfsUtil.uploadFile(destinationPath, size, inputStream)
        logger.info("File upload successful")
      case _ =>
        throw new Exception("Invalid destination type provided or Not Supported...")
    }
    this
  }

  override def targetFS(targetType: String, objectName: String, saveMode: SaveMode): Unit = ???

  override def targetFS(destinationFilePath: String, saveMode: SaveMode): Unit = ???

  override def transformSql(sqlText: String): Connector = ???

  override def source(sourceObject: String, params: Map[String, String], schema: StructType): Connector = ???
}
