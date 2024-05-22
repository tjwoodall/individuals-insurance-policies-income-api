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

package v1.controllers.validators

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.{ResolveParsedNumber, ResolveStringPattern}
import api.models.errors.{CustomerRefFormatError, EventFormatError, MtdError}
import cats.data.Validated
import cats.implicits._
import v1.controllers.validators.resolvers.ResolveIntegerNumber
import v1.models.request.amendInsurancePolicies.{AmendCommonInsurancePoliciesItem, AmendInsurancePoliciesRequestData}

object AmendInsurancePoliciesRulesValidator extends RulesValidator[AmendInsurancePoliciesRequestData] {

  private val resolveNonNegativeDecimalNumber        = ResolveParsedNumber()
  private val resolveNonNegativeMinimumIntegerNumber = ResolveIntegerNumber()

  private val regex = "^[0-9a-zA-Z{À-˿'}\\- _&`():.'^]{1,90}$".r

  def validateBusinessRules(parsed: AmendInsurancePoliciesRequestData): Validated[Seq[MtdError], AmendInsurancePoliciesRequestData] = {
    import parsed.body._

    combine(
      lifeInsurance
        .map(_.zipWithIndex.traverse_ { case (lifeInsurance, index) =>
          validateCommonItem(lifeInsurance, "lifeInsurance", index)
        })
        .getOrElse(valid),
      capitalRedemption
        .map(_.zipWithIndex.traverse_ { case (capitalRedemption, index) =>
          validateCommonItem(capitalRedemption, "capitalRedemption", index)
        })
        .getOrElse(valid),
      lifeAnnuity
        .map(_.zipWithIndex.traverse_ { case (lifeAnnuity, index) =>
          validateCommonItem(lifeAnnuity, "lifeAnnuity", index)
        })
        .getOrElse(valid)
    ).onSuccess(parsed)
  }

  private def validateCommonItem(commonItem: AmendCommonInsurancePoliciesItem, itemName: String, arrayIndex: Int): Validated[Seq[MtdError], Unit] = {
    import commonItem._

    val validatedCustomerRef = customerReference match {
      case Some(value) =>
        val resolveCustomerRef = new ResolveStringPattern(regex, CustomerRefFormatError.withPath(s"/$itemName/$arrayIndex/customerReference"))
        resolveCustomerRef(value)
      case None => valid
    }

    val validatedEvent = event match {
      case Some(value) =>
        val resolveEvent = new ResolveStringPattern(regex, EventFormatError.withPath(s"/$itemName/$arrayIndex/event"))
        resolveEvent(value)
      case None => valid
    }

    val validatedMandatoryDecimalNumber = resolveNonNegativeDecimalNumber(gainAmount, s"/$itemName/$arrayIndex/gainAmount")

    val validatedOptionalDecimalNumber = resolveNonNegativeDecimalNumber(deficiencyRelief, s"/$itemName/$arrayIndex/deficiencyRelief")

    val validatedOptionalIntegerNumbers = List(
      (yearsHeld, s"/$itemName/$arrayIndex/yearsHeld"),
      (yearsHeldSinceLastGain, s"/$itemName/$arrayIndex/yearsHeldSinceLastGain")
    ).traverse_ { case (value, path) =>
      resolveNonNegativeMinimumIntegerNumber(value, path)
    }

    combine(validatedCustomerRef, validatedEvent, validatedMandatoryDecimalNumber, validatedOptionalDecimalNumber, validatedOptionalIntegerNumbers)

  }

}
