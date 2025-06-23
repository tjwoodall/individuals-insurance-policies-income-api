/*
 * Copyright 2023 HM Revenue & Customs
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

package v1.connectors

import common.connectors.InsuranceConnectorSpec
import config.MockInsuranceAppConfig
import shared.models.domain.{Nino, TaxYear, Timestamp}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v1.models.request.retrieveInsurancePolicies.RetrieveInsurancePoliciesRequestData
import v1.models.response.retrieveInsurancePolicies.RetrieveInsurancePoliciesResponse

import scala.concurrent.Future

class RetrieveInsurancePoliciesConnectorSpec extends InsuranceConnectorSpec {

  "RetrieveInsurancePoliciesConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new Api1661Test with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")
        val outcome          = Right(ResponseWrapper(correlationId, response))

        willGet(
          url"$baseUrl/income-tax/insurance-policies/income/$nino/2019-20"
        ).returns(Future.successful(outcome))

        await(connector.retrieveInsurancePolicies(request)) shouldBe outcome
      }
    }

    "return the expected response for a TYS request" when {
      "a valid request is made" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")
        val outcome          = Right(ResponseWrapper(correlationId, response))

        willGet(
          url"$baseUrl/income-tax/insurance-policies/income/23-24/$nino"
        ).returns(Future.successful(outcome))

        await(connector.retrieveInsurancePolicies(request)) shouldBe outcome
      }
    }
  }

  trait Test extends MockInsuranceAppConfig {
    _: ConnectorTest =>

    def taxYear: TaxYear

    protected val nino: String = "AA111111A"

    protected val request: RetrieveInsurancePoliciesRequestData =
      RetrieveInsurancePoliciesRequestData(
        nino = Nino(nino),
        taxYear = taxYear
      )

    val response: RetrieveInsurancePoliciesResponse = RetrieveInsurancePoliciesResponse(
      submittedOn = Timestamp("2020-07-06T09:37:17.000Z"),
      lifeInsurance = None,
      capitalRedemption = None,
      lifeAnnuity = None,
      voidedIsa = None,
      foreign = None
    )

    val connector: RetrieveInsurancePoliciesConnector = new RetrieveInsurancePoliciesConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig,
      insuranceConfig = mockInsuranceAppConfig
    )

  }

}
