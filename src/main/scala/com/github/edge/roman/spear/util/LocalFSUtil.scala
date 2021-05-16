package com.github.edge.roman.spear.util

import org.apache.commons.io.{FileUtils, IOUtils}

import java.io._

class LocalFSUtil {


  def downloadFile(remote: String): InputStream = {
    var stream: InputStream = null
    try {
      stream = new FileInputStream(new File(remote))
    } catch {
      case exception: Exception => println(exception.printStackTrace())
    }
    stream
  }

  def uploadFile(remote: String, file: File):Unit = {
    try {
      val outputStream: OutputStream = new FileOutputStream(new File(remote))
      val inputStream: InputStream = new FileInputStream(file)
      IOUtils.copy(inputStream, outputStream)
      outputStream.close()
      inputStream.close()
    } catch {
      case exception: Exception => println(exception.printStackTrace())
    }
  }

  def uploadFile(remote: String, size: Long, fileStream: InputStream):Unit = {
    try {
      val outputStream: OutputStream = new FileOutputStream(new File(remote))
      IOUtils.copy(fileStream, outputStream)
      outputStream.close()
      fileStream.close()
    } catch {
      case exception: Exception => println(exception.printStackTrace())
    }
  }

  def getSize(remote: String): Long = {
    var size: Long = 0L
    try {
      size = FileUtils.getFile(remote).length()
    } catch {
      case exception: Exception => println(exception.printStackTrace())
    }
    size
  }
}
