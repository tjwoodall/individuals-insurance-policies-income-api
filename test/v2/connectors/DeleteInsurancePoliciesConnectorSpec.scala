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

package v2.connectors

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{InternalError, NinoFormatError}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.models.request.deleteInsurancePolicies.DeleteInsurancePoliciesRequestData

import scala.concurrent.Future

class DeleteInsurancePoliciesConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    protected val connector: DeleteInsurancePoliciesConnector = new DeleteInsurancePoliciesConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val request: DeleteInsurancePoliciesRequestData = DeleteInsurancePoliciesRequestData(Nino(nino), taxYear)

  }

  "Delete" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url"$baseUrl/income-tax/insurance-policies/income/$nino/${taxYear.asMtd}"
        ).returns(Future.successful(outcome))

        await(connector.deleteInsurancePolicies(request)) shouldBe outcome
      }

      "downstream returns a single error" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val outcome = Left(ResponseWrapper(correlationId, NinoFormatError))

        willDelete(
          url"$baseUrl/income-tax/insurance-policies/income/$nino/${taxYear.asMtd}"
        ).returns(Future.successful(outcome))

        await(connector.deleteInsurancePolicies(request)) shouldBe outcome
      }

      "downstream returns multiple errors" in new IfsTest with Test {

        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val outcome = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, InternalError)))

        willDelete(
          url"$baseUrl/income-tax/insurance-policies/income/$nino/${taxYear.asMtd}"
        ).returns(Future.successful(outcome))

        await(connector.deleteInsurancePolicies(request)) shouldBe outcome
      }

    }
    "return the expected response for a TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url"$baseUrl/income-tax/insurance-policies/income/${taxYear.asTysDownstream}/$nino"
        ).returns(Future.successful(outcome))

        await(connector.deleteInsurancePolicies(request)) shouldBe outcome
      }
    }
  }

}
