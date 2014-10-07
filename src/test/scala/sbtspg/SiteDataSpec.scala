package sbtspg

import com.typesafe.config.ConfigFactory
import org.specs2.{ScalaCheck, Specification}

class SiteDataSpec extends Specification with ScalaCheck with ArbitraryInput { def is = s2"""

  On including another SiteData it must
    include the incoming tags $incomingTags

  On presenting a tag string it must
    be sorted and comma-separated $tagString

"""

  def incomingTags = (arbIdSet, arbOptIdSet){(base, incoming) =>
    val conf = ConfigFactory.parseString(incoming.map{xs => s"""tags = [${xs.mkString(", ")}]\n"""}.getOrElse(""))
    val result = SiteData(base) include conf
    result.tags must beEqualTo(incoming.map(_ ++ base).getOrElse(base))
  }

  def tagString = arbIdSet{tags =>
    if (tags.isEmpty) SiteData(tags).tagString must beNone
    else SiteData(tags).tagString.map(_.split(", ")) must beSome(tags.toArray.sorted)
  }

}
