package com.github.edge.roman.spear.util

import org.apache.commons.net.ftp._
import java.io.InputStream
import scala.util.Try

class FTPUtil() {

  private val client = new FTPClient

  def login(username: String, password: String): Try[Boolean] = Try {
    client.login(username, password)
  }

  def connect(host: String):Unit = {
    try {
      client.connect(host)
      client.enterLocalPassiveMode()
    } catch {
      case exception: Exception => exception.printStackTrace()
    }

  }

  def configureClient(configMap: Map[String, String]):Unit = {
    try {
      val host: String = configMap("host")
      val userName: String = configMap.getOrElse("user", "anonymous")
      val password: String = configMap.getOrElse("password", "anonymous")
      client.connect(host)
      client.enterLocalPassiveMode()
      client.login(userName, password)
    } catch {
      case exception: Exception => exception.printStackTrace()
    }
  }


  def listFiles(dir: Option[String] = None): List[FTPFile] =
    dir.fold(client.listFiles)(client.listFiles).toList


  def downloadFile(remote: String): InputStream = {
    var stream: InputStream = null
    try {
      stream = client.retrieveFileStream(remote)
      client.completePendingCommand()
      stream
    } catch {
      case exception: Exception => exception.printStackTrace()
    }
    stream
  }

  def uploadFile(remote: String, stream: InputStream): Boolean =
    client.storeFile(remote, stream)

  def getSize(remote: String): Long = {
    var size: Long = 0L
    try {
      val ftpFile = client.mlistFile(remote)
      if (ftpFile != null) {
        size = ftpFile.getSize
      }
    } catch {
      case exception: Exception => exception.printStackTrace()
    }
    size
  }
}