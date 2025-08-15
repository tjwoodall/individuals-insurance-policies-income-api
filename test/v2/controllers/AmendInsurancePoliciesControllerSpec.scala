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

package v2.controllers

import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v2.controllers.validators.MockAmendInsurancePoliciesValidatorFactory
import v2.models.request.amendInsurancePolicies.*
import v2.services.MockAmendInsurancePoliciesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendInsurancePoliciesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAmendInsurancePoliciesService
    with MockAuditService
    with MockAmendInsurancePoliciesValidatorFactory
    with MockSharedAppConfig {

  val taxYear = "2019-20"

  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "lifeInsurance":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "capitalRedemption":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "lifeAnnuity":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "voidedIsa":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12
      |       }
      |   ],
      |   "foreign":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15
      |       }
      |   ]
      |}
    """.stripMargin
  )

  val lifeInsurance: Seq[AmendCommonInsurancePoliciesItem] = Seq(
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    ),
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    )
  )

  val capitalRedemption: Seq[AmendCommonInsurancePoliciesItem] = Seq(
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    ),
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    )
  )

  val lifeAnnuity: Seq[AmendCommonInsurancePoliciesItem] = Seq(
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    ),
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    )
  )

  val voidedIsa: Seq[AmendVoidedIsaPoliciesItem] = Seq(
    AmendVoidedIsaPoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12)
    ),
    AmendVoidedIsaPoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12)
    )
  )

  val foreign: Seq[AmendForeignPoliciesItem] = Seq(
    AmendForeignPoliciesItem(
      customerReference = Some("INPOLY123A"),
      gainAmount = 2000.99,
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15)
    ),
    AmendForeignPoliciesItem(
      customerReference = Some("INPOLY123A"),
      gainAmount = 2000.99,
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15)
    )
  )

  val amendInsurancePoliciesRequestBody: AmendInsurancePoliciesRequestBody = AmendInsurancePoliciesRequestBody(
    lifeInsurance = Some(lifeInsurance),
    capitalRedemption = Some(capitalRedemption),
    lifeAnnuity = Some(lifeAnnuity),
    voidedIsa = Some(voidedIsa),
    foreign = Some(foreign)
  )

  val requestData: AmendInsurancePoliciesRequestData = AmendInsurancePoliciesRequestData(
    nino = Nino(validNino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = amendInsurancePoliciesRequestBody
  )

  "AmendInsurancePoliciesController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendInsurancePoliciesService
          .amendInsurancePolicies(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
          "supporting-agents-access-control.enabled" -> false
        )

        MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = None,
          maybeAuditResponseBody = None
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
          "supporting-agents-access-control.enabled" -> false
        )

        MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false
        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendInsurancePoliciesService
          .amendInsurancePolicies(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
          "supporting-agents-access-control.enabled" -> false
        )

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: AmendInsurancePoliciesController = new AmendInsurancePoliciesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockAmendInsurancePoliciesValidatorFactory,
      service = mockAmendInsurancePoliciesService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.amendInsurancePolicies(validNino, taxYear)(fakePostRequest(requestBodyJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendInsurancePolicies",
        transactionName = "create-amend-insurance-policies",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = "2.0",
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          requestBody = requestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
