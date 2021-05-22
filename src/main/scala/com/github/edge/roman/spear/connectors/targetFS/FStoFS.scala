package com.github.edge.roman.spear.connectors.targetFS

import com.github.edge.roman.spear.commons.SpearCommons
import com.github.edge.roman.spear.connectors.AbstractTargetFSConnector
import com.github.edge.roman.spear.util.{ADLSUtil, FTPUtil, GCSUtil, HDFSUtil, LocalFSUtil, S3Util, SMBUtil}
import org.apache.spark.sql.SaveMode

import java.io.InputStream


class FStoFS(sourceFormat: String, destFormat: String) extends AbstractTargetFSConnector(sourceFormat,destFormat) {
  private val ftpUtil: FTPUtil = new FTPUtil
  private val s3Util: S3Util = new S3Util
  private val smbUtil: SMBUtil = new SMBUtil
  private val gcsUtil: GCSUtil = new GCSUtil
  private val adlsUtil: ADLSUtil = new ADLSUtil
  private val hdfsUtil: HDFSUtil = new HDFSUtil
  private val localFSUtil: LocalFSUtil = new LocalFSUtil

  private var inputStream: InputStream = null
  private var size: Long = 0L

  override def source(sourceObject: String, params: Map[String, String]): FStoFS = {
    sourceFormat match {
      case SpearCommons.FTP =>
        ftpUtil.configureClient(params)
        logger.info("FTP Client configured successfully")
        inputStream = ftpUtil.downloadFile(sourceObject)
        size = ftpUtil.getSize(sourceObject)
      case SpearCommons.AWS =>
        s3Util.configureClient(params)
        logger.info("Amazon S3 Client configured successfully")
        inputStream = s3Util.downloadFile(sourceObject)
        size = s3Util.getSize(sourceObject)
      case SpearCommons.SMB =>
        smbUtil.configureClient(params)
        logger.info("SMB Client configured successfully")
        inputStream = smbUtil.downloadFile(sourceObject)
        size = smbUtil.getSize(sourceObject)
      case SpearCommons.GCS =>
        gcsUtil.configureClient(params)
        logger.info("Google Cloud Storage configured successfully")
        inputStream = gcsUtil.downloadFile(sourceObject)
      case SpearCommons.ADLS =>
        adlsUtil.configureClient(params)
        logger.info("Azure Blob Storage configured successfully")
        inputStream = adlsUtil.downloadFile(sourceObject)
      case SpearCommons.HDFS =>
        hdfsUtil.configureClient(params)
        logger.info("Hadoop File System configured successfully")
        inputStream = hdfsUtil.downloadFile(sourceObject)
      case SpearCommons.LOCAL =>
        logger.info("Local File System configured successfully")
        inputStream = localFSUtil.downloadFile(sourceObject)
      case _ =>
        throw new Exception("Invalid source type provided or Not Supported.")
    }
    this
  }

  override def targetFS(destinationPath: String, params: Map[String, String]): Unit = {
    destFormat match {
      case SpearCommons.LOCAL =>
        localFSUtil.uploadFile(destinationPath, size, inputStream)
        logger.info(SpearCommons.FileUploadSuccess)
      case SpearCommons.AWS =>
        s3Util.configureClient(params)
        s3Util.uploadFile(destinationPath, size, inputStream)
        logger.info(SpearCommons.FileUploadSuccess)
      case SpearCommons.GCS =>
        gcsUtil.configureClient(params)
        gcsUtil.uploadFile(destinationPath, size, inputStream)
        logger.info(SpearCommons.FileUploadSuccess)
      case SpearCommons.ADLS =>
        adlsUtil.configureClient(params)
        adlsUtil.uploadFile(destinationPath, size, inputStream)
        logger.info(SpearCommons.FileUploadSuccess)
      case SpearCommons.HDFS =>
        hdfsUtil.configureClient(params)
        hdfsUtil.uploadFile(destinationPath, size, inputStream)
        logger.info(SpearCommons.FileUploadSuccess)
      case _ =>
        throw new Exception("Invalid destination type provided or Not Supported.")
    }
  }

  override def targetFS(destinationPath: String, saveMode: SaveMode): Unit = {
    destFormat match {
      case SpearCommons.LOCAL =>
        localFSUtil.uploadFile(destinationPath, size, inputStream)
        logger.info(SpearCommons.FileUploadSuccess)
      case SpearCommons.AWS =>
        s3Util.uploadFile(destinationPath, size, inputStream)
        logger.info(SpearCommons.FileUploadSuccess)
      case SpearCommons.GCS =>
        gcsUtil.uploadFile(destinationPath, size, inputStream)
        logger.info(SpearCommons.FileUploadSuccess)
      case SpearCommons.ADLS =>
        adlsUtil.uploadFile(destinationPath, size, inputStream)
        logger.info(SpearCommons.FileUploadSuccess)
      case SpearCommons.HDFS =>
        hdfsUtil.uploadFile(destinationPath, size, inputStream)
        logger.info(SpearCommons.FileUploadSuccess)
      case _ =>
        throw new Exception("Invalid destination type provided or Not Supported...")
    }
  }
}
