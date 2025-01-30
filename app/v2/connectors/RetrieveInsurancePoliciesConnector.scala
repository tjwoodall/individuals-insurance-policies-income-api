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

import config.InsuranceAppConfig
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.TaxYearSpecificIfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamStrategy, DownstreamUri}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.request.retrieveInsurancePolicies.RetrieveInsurancePoliciesRequestData
import v2.models.response.retrieveInsurancePolicies.RetrieveInsurancePoliciesResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveInsurancePoliciesConnector @Inject()(
                                                    val http: HttpClient,
                                                    val appConfig: SharedAppConfig,
                                                    insuranceConfig: InsuranceAppConfig
                                                  ) extends BaseDownstreamConnector {

  def retrieveInsurancePolicies(request: RetrieveInsurancePoliciesRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveInsurancePoliciesResponse]] = {

    import request._

    val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[RetrieveInsurancePoliciesResponse](
        s"income-tax/insurance-policies/income/${taxYear.asTysDownstream}/${nino.value}"
      )
    } else {
      DownstreamUri[RetrieveInsurancePoliciesResponse](
        s"income-tax/insurance-policies/income/${nino.value}/${taxYear.asMtd}",
        DownstreamStrategy.standardStrategy(insuranceConfig.api1661DownstreamConfig)
      )

    }

    get(uri = downstreamUri)

  }

}
