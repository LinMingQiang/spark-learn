package com.spark.udf.register

import java.lang.reflect.Method

import com.spark.udf.bean.{MethodInfo, PreCompileInfo, UDFClassInfo}
import com.spark.udf.core.{MethodToScalaFunction, UDFClassLoaderManager}
import com.spark.udf.loader.UrlClassLoader
import org.apache.spark.sql.SparkSession
import org.slf4j.Logger

/**
  *
  * @param hdsfPaths Array("file://data/dd/dd/dd/mobutils-core-v0.1.5.jar")
  * @param udfClassFunc Array(("com.test.util.DateUtils", "*"))
  */
class UrlJarUDFRegister(val hdsfPaths: Array[String],
                        val udfClassFunc: Array[(String, String)])
    extends UDFRegisterTrait {
  var loadClassNames: Set[String] = udfClassFunc.map(_._1).toSet
  val preCompInfos = udfClassFunc.map {
    case (classPath, methodName) =>
      PreCompileInfo(classPath, null, methodName)
  }

  /**
    * 注册class和func。返回一个 Map[className, classInfo]。会做防重复加载
    */
//  override def register()(_log: Logger): Map[String, UDFClassInfo] = {
//    UDFClassLoaderManager.loadJarFromURL(hdsfPaths)
//    UrlClassLoader.classForName(udfClassFunc)
//  }

  /**
    * 将func注册进spark
    */
  override def registerUDF(
      isRegisterUdf: Boolean = true): Map[String, UDFClassInfo] = {
    UDFClassLoaderManager.loadJarFromURL(hdsfPaths)
    UrlClassLoader.getClassInfo(
      preCompInfos,
      if (isRegisterUdf)
        transMethodToScalaFunc(this)
      else transMethodToInfo()
    )
  }

  /**
    * 比较两个是否为同一个
    * @param obj
    * @return
    */
  override def equalsOtherRegister(obj: Any): Boolean = {
    if (obj.isInstanceOf[UrlJarUDFRegister]) {
      val other = obj.asInstanceOf[UrlJarUDFRegister]
      other.hdsfPaths.equals(hdsfPaths) && udfClassFunc.equals(
        other.udfClassFunc)
    } else false
  }
  override def classHashCode(): Int = {
    udfClassFunc.hashCode()
  }

  override def toString: String =
    s"""UrlJarUDFRegister : [${udfClassFunc.mkString(",")}]"""
}

