/*
 * Copyright 2025 HM Revenue & Customs
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

package shared.definition

import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import shared.definition.APIStatus.BETA
import shared.routing.Version1
import shared.utils.UnitSpec

class ApiVersionSpec extends UnitSpec {

  val json: JsValue = Json.parse("""
      |{
      |"version": "1.0",
      |"status": "BETA",
      |"endpointsEnabled": true
      |}""".stripMargin)

  val model: APIVersion = APIVersion(Version1, BETA, true)

  "reads" should {
    "read JSON to a model" in {
      json.as[APIVersion] shouldBe model
    }
  }

  "writes" should {
    "write a model to JSON" in {
      Json.toJson(model) shouldBe json
    }
  }

  "error when JSON is invalid" in {
    JsObject.empty.validate[APIVersion] shouldBe a[JsError]
  }

}
