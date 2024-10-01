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

package v1.controllers.validators

import common.errors.{CustomerRefFormatError, EventFormatError}
import config.MockInsuranceAppConfig
import play.api.libs.json.{JsValue, Json}
import shared.config.MockSharedAppConfig
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.utils.UnitSpec
import v1.models.request.amendInsurancePolicies.{AmendInsurancePoliciesRequestBody, AmendInsurancePoliciesRequestData}

class AmendInsurancePoliciesValidatorFactorySpec extends UnitSpec with MockSharedAppConfig with MockInsuranceAppConfig {
  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2020-21"

  private val ZERO_MINIMUM_INCLUSIVE         = "The value must be between 0 and 99999999999.99"
  private val ZERO_MINIMUM_INTEGER_INCLUSIVE = "The value must be between 0 and 99"

  private val validRequestBodyJson: JsValue = Json.parse(
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

  private val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

  private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

  private val nonValidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "lifeInsurance":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": "no",
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val missingMandatoryFieldJson: JsValue = Json.parse(
    """
      |{
      |   "foreign":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidCustomerRefRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "capitalRedemption":[
      |       {
      |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidEventRequestBodyJson: JsValue = Json.parse(
    s"""
       |{
       |   "lifeAnnuity":[
       |       {
       |           "customerReference": "INPOLY123A",
       |           "event": "${"a" * 91} ",
       |           "gainAmount": 2000.99,
       |           "taxPaid": true,
       |           "yearsHeld": 15,
       |           "yearsHeldSinceLastGain": 12,
       |           "deficiencyRelief": 5000.99
       |       }
       |   ]
       |}
    """.stripMargin
  )

  private val invalidLifeInsuranceRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "lifeInsurance":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": -2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidCapitalRedemptionRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "capitalRedemption":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.999
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidLifeAnnuityRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "lifeAnnuity":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.999,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidVoidedIsaRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "voidedIsa":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 300
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidForeignRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreign":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 150
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "lifeInsurance":[
      |       {
      |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.999,
      |           "taxPaid": true,
      |           "yearsHeld": -15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.999
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "This event string is 91 characters long ------------------------------------------------ 91",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "capitalRedemption":[
      |       {
      |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |           "event": "Death of spouse",
      |           "gainAmount": 3000.999,
      |           "taxPaid": true,
      |           "yearsHeld": -15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 120,
      |           "deficiencyRelief": 5000.999
      |       }
      |   ],
      |   "lifeAnnuity":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": -15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.999
      |       },
      |       {
      |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |           "event": "This event string is 91 characters long ------------------------------------------------ 91",
      |           "gainAmount": 5000.99,
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
      |           "yearsHeld": -15,
      |           "yearsHeldSinceLastGain": 120
      |       },
      |       {
      |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |           "event": "Death of spouse",
      |           "gainAmount": 5000.999,
      |           "taxPaidAmount": 5000.999,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12
      |       }
      |   ],
      |   "foreign":[
      |       {
      |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |           "gainAmount": 5000.99,
      |           "taxPaidAmount": 5000.999,
      |           "yearsHeld": 15
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "gainAmount": 2000.999,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": -15
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val parsedValidRequestBody = validRequestBodyJson.as[AmendInsurancePoliciesRequestBody]

  private val validatorFactory = new AmendInsurancePoliciesValidatorFactory(mockInsuranceAppConfig)

  private def validator(nino: String, taxYear: String, body: JsValue) =
    validatorFactory.validator(nino, taxYear, body)

  MockedInsuranceAppConfig.minimumPermittedTaxYear
    .returns(TaxYear.ending(2021))
    .anyNumberOfTimes()

  "validator" should {
    "return the parsed domain object" when {
      "a valid request is supplied" in {
        val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
          validator(validNino, validTaxYear, validRequestBodyJson).validateAndWrapResult()

        result shouldBe Right(
          AmendInsurancePoliciesRequestData(parsedNino, parsedTaxYear, parsedValidRequestBody)
        )
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
          validator("A12344A", validTaxYear, validRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in {
        val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
          validator(validNino, "20178", validRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, TaxYearFormatError)
        )
      }
    }

    "return RuleTaxYearRangeInvalidError error for an invalid tax year range" in {
      val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
        validator(validNino, "2019-23", validRequestBodyJson).validateAndWrapResult()

      result shouldBe Left(
        ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
      )
    }

    "return multiple errors for multiple invalid request parameters" in {
      val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
        validator("A12344A", "2020-22", validRequestBodyJson).validateAndWrapResult()

      result shouldBe Left(
        ErrorWrapper(
          correlationId,
          BadRequestError,
          Some(List(NinoFormatError, RuleTaxYearRangeInvalidError))
        )
      )
    }

    "return RuleTaxYearNotSupportedError error for an unsupported tax year" in {
      val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
        validator(validNino, "2019-20", validRequestBodyJson).validateAndWrapResult()

      result shouldBe Left(
        ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
      )
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in {
        val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
          validator(validNino, validTaxYear, emptyRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError)
        )
      }

      "a non-empty JSON body is submitted without any expected fields" in {
        val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
          validator(validNino, validTaxYear, nonsenseRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError)
        )
      }

      "the submitted request body is not in the correct format" in {
        val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
          validator(validNino, validTaxYear, nonValidRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/lifeInsurance/0/gainAmount"))
        )
      }

      "the submitted request body has missing mandatory fields" in {
        val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
          validator(validNino, validTaxYear, missingMandatoryFieldJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreign/1/gainAmount"))
        )
      }
    }

    "return CustomerRefFormatError error" when {
      "an incorrectly formatted customer reference is submitted" in {
        val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
          validator(validNino, validTaxYear, invalidCustomerRefRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, CustomerRefFormatError.withPath("/capitalRedemption/0/customerReference"))
        )
      }
    }

    "return EventFormatError error" when {
      "an incorrectly formatted event is submitted" in {
        val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
          validator(validNino, validTaxYear, invalidEventRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, EventFormatError.withPath("/lifeAnnuity/0/event"))
        )
      }
    }

    "return ValueFormatError error (single failure)" when {
      "one field fails value validation (life insurance)" in {
        val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
          validator(validNino, validTaxYear, invalidLifeInsuranceRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, ValueFormatError.forPathAndRange("/lifeInsurance/0/gainAmount", "0", "99999999999.99"))
        )
      }

      "one field fails value validation (capital redemption)" in {
        val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
          validator(validNino, validTaxYear, invalidCapitalRedemptionRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange("/capitalRedemption/0/deficiencyRelief", "0", "99999999999.99")
          ))
      }

      "one field fails value validation (life annuity)" in {
        val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
          validator(validNino, validTaxYear, invalidLifeAnnuityRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange("/lifeAnnuity/0/gainAmount", "0", "99999999999.99")
          ))
      }

      "one field fails value validation (voidedIsa)" in {
        val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
          validator(validNino, validTaxYear, invalidVoidedIsaRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange("/voidedIsa/0/yearsHeldSinceLastGain", "0", "99")
          ))
      }

      "one field fails value validation (foreign)" in {
        val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
          validator(validNino, validTaxYear, invalidForeignRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange("/foreign/0/yearsHeld", "0", "99")
          ))
      }
    }

    "return multiple errors" when {
      "multiple fields fail value validation" in {
        val result: Either[ErrorWrapper, AmendInsurancePoliciesRequestData] =
          validator(validNino, validTaxYear, allInvalidValueRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(
              CustomerRefFormatError.withPaths(Seq(
                "/lifeInsurance/0/customerReference",
                "/capitalRedemption/0/customerReference",
                "/lifeAnnuity/1/customerReference",
                "/voidedIsa/1/customerReference",
                "/foreign/0/customerReference"
              )),
              EventFormatError.withPaths(Seq("/lifeInsurance/1/event", "/lifeAnnuity/1/event")),
              ValueFormatError.copy(
                message = ZERO_MINIMUM_INCLUSIVE,
                paths = Some(List(
                  "/lifeInsurance/0/gainAmount",
                  "/lifeInsurance/0/deficiencyRelief",
                  "/capitalRedemption/0/gainAmount",
                  "/capitalRedemption/1/deficiencyRelief",
                  "/lifeAnnuity/0/deficiencyRelief",
                  "/voidedIsa/1/gainAmount",
                  "/voidedIsa/1/taxPaidAmount",
                  "/foreign/0/taxPaidAmount",
                  "/foreign/1/gainAmount"
                ))
              ),
              ValueFormatError.copy(
                message = ZERO_MINIMUM_INTEGER_INCLUSIVE,
                paths = Some(List(
                  "/lifeInsurance/0/yearsHeld",
                  "/capitalRedemption/0/yearsHeld",
                  "/capitalRedemption/1/yearsHeldSinceLastGain",
                  "/lifeAnnuity/0/yearsHeld",
                  "/voidedIsa/0/yearsHeld",
                  "/voidedIsa/0/yearsHeldSinceLastGain",
                  "/foreign/1/yearsHeld"
                ))
              )
            ))
          ))
      }
    }
  }

}
