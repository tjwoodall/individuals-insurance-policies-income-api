/*
 * Copyright 2024 HM Revenue & Customs
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

package common.connectors

import config.{MockInsuranceAppConfig, MockInsuranceFeatureSwitches}
import shared.connectors.ConnectorSpec

trait InsuranceConnectorSpec extends ConnectorSpec with MockInsuranceFeatureSwitches {

  protected trait InsuranceConnectorTest extends StandardConnectorTest with MockInsuranceAppConfig

  protected trait Api1661Test extends InsuranceConnectorTest {
    val name = "api1661"

    MockedInsuranceAppConfig.api1661DownstreamConfig.anyNumberOfTimes() returns config
  }

}
