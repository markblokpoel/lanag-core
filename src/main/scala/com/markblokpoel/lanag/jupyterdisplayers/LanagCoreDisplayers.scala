package com.markblokpoel.lanag.jupyterdisplayers

import com.markblokpoel.lanag.rsa.Lexicon
import jupyter.Displayers
import scala.collection.JavaConverters._

/**
  * This is a collection of Displayer classes for nice html formatted output in Jupyter notebooks.
  */
object LanagCoreDisplayers {
  Displayers.register(classOf[Lexicon], (lexicon: Lexicon) => {
    import scalatags.Text.all._
    Map(
    "text/html" -> {
      table(cls:="table")(
        lexicon match {
          case Lexicon(vocabularySize, contextSize, data) =>
            Vector(tr(td(), for(i <- 1 to contextSize) yield th("R", sub(i)))).union(
              for(i <- 0 until vocabularySize) yield {
                tr(
                  td(strong("V", sub(i+1))),
                    for(value <- data.slice(i*contextSize, (1+i) * contextSize)) yield td(value)
                )
            })
          case _ => tr(td("Error: Value is not of type Lexicon."))
        }
    ).render
    }
    ).asJava
    })
}
