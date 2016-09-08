/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unit.uk.gov.hmrc.openidconnect.userinfo.data

import uk.gov.hmrc.openidconnect.userinfo.data.UserInfoGenerator
import uk.gov.hmrc.openidconnect.userinfo.domain.UserInfo
import org.joda.time.LocalDate
import org.scalatest.prop.PropertyChecks
import uk.gov.hmrc.play.test.UnitSpec

class UserInfoGeneratorSpec extends UnitSpec with PropertyChecks {
  val ninoPattern = "^[A-CEGHJ-NOPR-TW-Z]{2}[0-9]{6}[ABCD\\s]{1}$".r
  val from = new LocalDate(1939, 12, 27)
  val until = new LocalDate(1998, 12, 29)

  "userInfo" should {
    "generate an OpenID Connect compliant UserInfo response" in forAll(UserInfoGenerator.userInfo) { userInfo: UserInfo =>
      assertValid("givenName", UserInfoGenerator.firstNames, userInfo.given_name)
      UserInfoGenerator.middleNames should contain (userInfo.middle_name)
      assertValid("familyName", UserInfoGenerator.lastNames, userInfo.family_name)
      assertValid("givenName", UserInfoGenerator.firstNames, userInfo.given_name)
      userInfo.address shouldBe UserInfoGenerator.address
      assertValidDob(userInfo.birthdate.getOrElse(fail(s"Generated user's dob is not defined")))
      assertValidNino(userInfo.uk_gov_nino.getOrElse(fail(s"Generated user's NINO is not defined")))
    }
  }

  private def assertValid(name: String, expected: List[String], actual: Option[String]): Unit = {
    expected should contain (actual.getOrElse(fail(s"Generated user's $name is not defined")))
  }

  private def assertValidDob(dob: LocalDate): Unit = {

      dob.isAfter(from) && dob.isBefore(until) match {
        case true =>
        case false => fail(s"Generated user's dob: $dob is not within valid range: 1940-01-01 / 1998-12-28")
      }
  }

  private def assertValidNino(nino: String) = {
    ninoPattern.findFirstIn(nino) match {
      case Some(s) =>
      case None => fail(s"Generated invalid user's NINO: $nino")
    }
  }
}



