package sbtspg

import org.specs2.{Specification, ScalaCheck}

import scala.collection.Set

class SiteDataSpec extends Specification with ScalaCheck with ArbitraryInput { def is = s2"""

  On including another SiteData
    it should include the incoming tags $incomingTags

  On presenting a tag string
    it should be sorted and comma-separated $tagString

"""

  def incomingTags = (arbIdSet, arbOptIdSet){(base, incoming) =>
    val incomingMeta = incoming.map{xs =>
      if (xs.isEmpty) Map.empty[String, String]
      else Map("tags" -> xs.mkString(","))
    }.getOrElse(Map.empty)
    val result = SiteData(base) include MarkupWithMeta(incomingMeta, null, null)
    result.tags must beEqualTo(incoming.map(_ ++ base).getOrElse(base))
  }

  def tagString = arbIdSet{tags =>
    if (tags.isEmpty) SiteData(tags).tagString must beEqualTo("")
    else SiteData(tags).tagString.split(", ") must beEqualTo(tags.toArray.sorted)
  }

}
