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

package v2.controllers.validators

import cats.data.Validated
import cats.implicits._
import common.errors.{CustomerRefFormatError, EventFormatError}
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.{ResolveInteger, ResolveParsedNumber, ResolveStringPattern}
import shared.models.errors.MtdError
import v2.models.request.amendInsurancePolicies.{
  AmendCommonInsurancePoliciesItem,
  AmendForeignPoliciesItem,
  AmendInsurancePoliciesRequestData,
  AmendVoidedIsaPoliciesItem
}

object AmendInsurancePoliciesRulesValidator extends RulesValidator[AmendInsurancePoliciesRequestData] {

  private val resolveNonNegativeDecimalNumber        = ResolveParsedNumber()
  private val resolveNonNegativeMinimumIntegerNumber = ResolveInteger(0, 99)

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
        .getOrElse(valid),
      voidedIsa
        .map(_.zipWithIndex.traverse_ { case (voidedIsa, index) =>
          validateVoidedIsa(voidedIsa, index)
        })
        .getOrElse(valid),
      foreign
        .map(_.zipWithIndex.traverse_ { case (foreign, index) =>
          validateForeign(foreign, index)
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

  private def validateVoidedIsa(voidedIsa: AmendVoidedIsaPoliciesItem, arrayIndex: Int): Validated[Seq[MtdError], Unit] = {
    import voidedIsa._

    val validatedCustomerRef = customerReference match {
      case Some(value) =>
        val resolveCustomerRef = new ResolveStringPattern(regex, CustomerRefFormatError.withPath(s"/voidedIsa/$arrayIndex/customerReference"))
        resolveCustomerRef(value)
      case None => valid
    }

    val validatedEvent = event match {
      case Some(value) =>
        val resolveEvent = new ResolveStringPattern(regex, EventFormatError.withPath(s"/voidedIsa/$arrayIndex/event"))
        resolveEvent(value)
      case None => valid
    }

    val validatedMandatoryDecimalNumber = resolveNonNegativeDecimalNumber(gainAmount, s"/voidedIsa/$arrayIndex/gainAmount")

    val validatedOptionalDecimalNumber = resolveNonNegativeDecimalNumber(taxPaidAmount, s"/voidedIsa/$arrayIndex/taxPaidAmount")

    val validatedOptionalIntegerNumbers = List(
      (yearsHeld, s"/voidedIsa/$arrayIndex/yearsHeld"),
      (yearsHeldSinceLastGain, s"/voidedIsa/$arrayIndex/yearsHeldSinceLastGain")
    ).traverse_ { case (value, path) =>
      resolveNonNegativeMinimumIntegerNumber(value, path)
    }

    combine(validatedCustomerRef, validatedEvent, validatedMandatoryDecimalNumber, validatedOptionalDecimalNumber, validatedOptionalIntegerNumbers)

  }

  private def validateForeign(foreign: AmendForeignPoliciesItem, arrayIndex: Int): Validated[Seq[MtdError], Unit] = {
    import foreign._

    val validatedCustomerRef = customerReference match {
      case Some(value) =>
        val resolveCustomerRef = new ResolveStringPattern(regex, CustomerRefFormatError.withPath(s"/foreign/$arrayIndex/customerReference"))
        resolveCustomerRef(value)
      case None => valid
    }

    val validatedMandatoryDecimalNumber = resolveNonNegativeDecimalNumber(gainAmount, s"/foreign/$arrayIndex/gainAmount")

    val validatedOptionalDecimalNumber = resolveNonNegativeDecimalNumber(taxPaidAmount, s"/foreign/$arrayIndex/taxPaidAmount")

    val validatedOptionalIntegerNumber =
      resolveNonNegativeMinimumIntegerNumber(yearsHeld, s"/foreign/$arrayIndex/yearsHeld")

    combine(validatedCustomerRef, validatedMandatoryDecimalNumber, validatedOptionalDecimalNumber, validatedOptionalIntegerNumber)

  }

}
