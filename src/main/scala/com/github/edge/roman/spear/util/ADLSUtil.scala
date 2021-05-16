package com.github.edge.roman.spear.util


import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.{CloudBlobClient, CloudBlobContainer}

import java.io.{File, FileInputStream, InputStream}

class ADLSUtil {

  var containerName: String = null
  val defaultEndpointsProtocol: String = "https"
  var container: CloudBlobContainer = null
  var blobClient: CloudBlobClient = null

  def configureClient(configMap: Map[String, String]):Unit = {
    try {
      containerName = configMap("containerName")
      val accountName: String = configMap("accountName")
      val accountKey: String = configMap("accountKey")
      val storageConnectionString = "DefaultEndpointsProtocol=" + defaultEndpointsProtocol + ";" + "AccountName=" + accountName + ";" + "AccountKey=" + accountKey + ";";
     val storageAccount = CloudStorageAccount.parse(storageConnectionString)
      blobClient = storageAccount.createCloudBlobClient();
      container = blobClient.getContainerReference(containerName)
    } catch {
      case exception: Exception => println(exception.printStackTrace())

    }


  }

  def downloadFile(remote: String): InputStream = {
    var stream:InputStream =null
    try {
      val blob = container.getBlockBlobReference(remote)
      blob.getProperties.getLength
      stream = blob.openInputStream()
    } catch {
      case exception: Exception => println(exception.printStackTrace())
    }
    stream
  }

  def uploadFile(remote: String, file: File):Unit = {
    try {
      val blob = container.getBlockBlobReference(remote)
      val fileStream: InputStream = new FileInputStream(file)
      val blobOutputStream = blob.openOutputStream()
      var next = -1
      while ((next = fileStream.read) != -1) {
        blobOutputStream.write(next)
      }
      blobOutputStream.close
      fileStream.close()
    } catch {
      case exception: Exception => println(exception.printStackTrace())
    }


  }


  def uploadFile(remote: String, size: Long, fileStream: InputStream):Unit = {
    try {
      val blob = container.getBlockBlobReference(remote)
      blob.upload(fileStream, size)
    } catch {
      case exception: Exception => println(exception.printStackTrace())
    }
  }

  def getSize(remote: String): Long = {
    var size: Long = 0L
    try {
      val blob = container.getBlockBlobReference(remote)
      size = blob.getProperties.getLength
    } catch {
      case exception: Exception => println(exception.printStackTrace())
    }
    size
  }
}
