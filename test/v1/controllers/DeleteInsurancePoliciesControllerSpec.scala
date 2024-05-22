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

package v1.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetailOld}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import mocks.MockAppConfig
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.mocks.requestParsers.MockDeleteInsurancePoliciesParser
import v1.mocks.services.MockDeleteInsurancePoliciesService
import v1.models.request.deleteInsurancePolicies.{DeleteInsurancePoliciesRawData, DeleteInsurancePoliciesRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteInsurancePoliciesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteInsurancePoliciesService
    with MockDeleteInsurancePoliciesParser
    with MockAuditService
    with MockAppConfig {

  val taxYear: String = "2019-20"

  val rawData: DeleteInsurancePoliciesRawData = DeleteInsurancePoliciesRawData(
    nino = validNino,
    taxYear = taxYear
  )

  val requestData: DeleteInsurancePoliciesRequest = DeleteInsurancePoliciesRequest(
    nino = Nino(validNino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  "DeleteInsurancePoliciesController" should {
    "return a successful response with status 204 (No Content)" when {
      "happy path" in new Test {
        MockDeleteInsurancePoliciesParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteInsurancePoliciesService
          .deleteInsurancePoliciesService(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockDeleteInsurancePoliciesParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError)
      }

      "service returns an error" in new Test {
        MockDeleteInsurancePoliciesParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteInsurancePoliciesService
          .deleteInsurancePoliciesService(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetailOld] {

    val controller = new DeleteInsurancePoliciesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockDeleteInsurancePoliciesParser,
      service = mockDeleteInsurancePoliciesService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.delete(validNino, taxYear)(fakeRequest)

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetailOld] =
      AuditEvent(
        auditType = "DeleteInsurancePolicies",
        transactionName = "delete-insurance-policies",
        detail = GenericAuditDetailOld(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          request = None,
          `X-CorrelationId` = correlationId,
          response = auditResponse
        )
      )

  }

}
