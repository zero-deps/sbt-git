package zero.git

import sys.process._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class VersionSpec extends AnyFreeSpec with Matchers {
  "version" in {
    val expected = "git describe --dirty --always".!!.trim.replaceAll(raw"-(\d+)-g", ".$1.g")
    version(parts=None, dotted=true) shouldBe expected
  }
}
