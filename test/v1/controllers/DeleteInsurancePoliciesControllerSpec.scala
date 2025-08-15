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

import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v1.controllers.validators.MockDeleteInsurancePoliciesValidatorFactory
import v1.models.request.deleteInsurancePolicies.DeleteInsurancePoliciesRequestData
import v1.services.MockDeleteInsurancePoliciesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteInsurancePoliciesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteInsurancePoliciesService
    with MockDeleteInsurancePoliciesValidatorFactory
    with MockAuditService
    with MockSharedAppConfig {

  val taxYear: String = "2019-20"

  val requestData: DeleteInsurancePoliciesRequestData = DeleteInsurancePoliciesRequestData(
    nino = Nino(validNino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  "DeleteInsurancePoliciesController" should {
    "return a successful response with status 204 (No Content)" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteInsurancePoliciesService
          .deleteInsurancePoliciesService(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))
        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
          "supporting-agents-access-control.enabled" -> false
        )

        MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

        runOkTestWithAudit(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
          "supporting-agents-access-control.enabled" -> false
        )

        MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

        runErrorTestWithAudit(NinoFormatError)
      }

      "service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteInsurancePoliciesService
          .deleteInsurancePoliciesService(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))
        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
          "supporting-agents-access-control.enabled" -> false
        )

        MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: DeleteInsurancePoliciesController = new DeleteInsurancePoliciesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockDeleteInsurancePoliciesValidatorFactory,
      service = mockDeleteInsurancePoliciesService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.delete(validNino, taxYear)(fakeRequest)

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteInsurancePolicies",
        transactionName = "delete-insurance-policies",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = "1.0",
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          requestBody = None,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
