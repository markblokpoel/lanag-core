package com.markblokpoel.lanag.util

import com.typesafe.config.{Config, ConfigException}


case class ConfigWrapper(conf: Config) {

  def getOrElse[T](path: String, fallback: T): T = try {
    fallback match {
      case _: Int => conf.getInt(path).asInstanceOf[T]
      case _: Double => conf.getDouble(path).asInstanceOf[T]
      case _: String => conf.getString(path).asInstanceOf[T]
      case _: Boolean => conf.getBoolean(path).asInstanceOf[T]
      case _: Long => conf.getLong(path).asInstanceOf[T]
    }
  } catch {
    case _: ConfigException.Missing =>
      println(s"Missing configuration value at $path using fallback value $fallback")
      fallback
    case _: ConfigException.WrongType =>
      println(s"Wrong configuration value type at $path using fallback value $fallback")
      fallback
  }

}
