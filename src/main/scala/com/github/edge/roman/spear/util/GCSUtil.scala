package com.github.edge.roman.spear.util

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.ReadChannel
import com.google.cloud.storage.{BlobId, BlobInfo, Storage, StorageOptions}

import java.io.{File, FileInputStream, InputStream}
import java.nio.channels.Channels

class GCSUtil {

  var storage: Storage = null
  var bucket_name: String = null

  def configureClient(configMap: Map[String, String]): Unit = {
    try {
      bucket_name = configMap("bucketName")
      val projectId: String = configMap("projectId")
      val gcsAuthKeyPath: String = configMap("gcsAuthKeyPath")
      storage = StorageOptions.newBuilder()
        .setProjectId(projectId)
        .setCredentials(GoogleCredentials.fromStream(
          new FileInputStream(gcsAuthKeyPath))).build().getService
    } catch {
      case exception: Exception => println(exception.printStackTrace())
    }
  }

  def downloadFile(remote: String): InputStream = {
    var stream: InputStream = null
    try {
      val reader: ReadChannel = storage.reader(bucket_name, remote)
      stream = Channels.newInputStream(reader)
    } catch {
      case exception: Exception => println(exception.printStackTrace())
    }
    stream
  }

  def uploadFile(remote: String, file: File): Unit = {
    try {
      val blobId = BlobId.of(bucket_name, remote)
      val storage = StorageOptions.getDefaultInstance.getService
      storage.createFrom(BlobInfo.newBuilder(blobId).build(), new FileInputStream(file))
    } catch {
      case exception: Exception => println(exception.printStackTrace())
    }
  }


  def uploadFile(remote: String, size: Long, fileStream: InputStream): Unit = {
    try {
      val blobId = BlobId.of(bucket_name, remote)
      val storage = StorageOptions.getDefaultInstance.getService
      storage.createFrom(BlobInfo.newBuilder(blobId).build(), fileStream)
    } catch {
      case exception: Exception => println(exception.printStackTrace())
    }
  }

  def getSize(remote: String): Long = {
    var size: Long = 0L
    try {
      val blobId = BlobId.of(bucket_name, remote)
      size = storage.get(blobId).getSize()
    } catch {
      case exception: Exception => println(exception.printStackTrace())
    }
    size
  }
}
