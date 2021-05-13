package com.github.edge.roman.spear.util

import org.stringtemplate.v4.STGroupFile

import java.util
import scala.collection.JavaConversions

object MappingResolverUtil extends App {

  val map = Map("k1" -> "v1", "k2" -> "v2")
  val stg = new STGroupFile("C:\\workarea\\gitprojects\\spear-framework\\src\\main\\resources\\test.stg", '$', '$')
  val st = stg.getInstanceOf("test")
  st.add("data", JavaConversions.mapAsJavaMap(map))
  println(st.render())
}
