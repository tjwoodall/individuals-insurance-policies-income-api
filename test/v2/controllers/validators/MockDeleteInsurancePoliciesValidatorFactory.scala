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
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v2.models.request.deleteInsurancePolicies.DeleteInsurancePoliciesRequestData

trait MockDeleteInsurancePoliciesValidatorFactory extends MockFactory {

  val mockDeleteInsurancePoliciesValidatorFactory: DeleteInsurancePoliciesValidatorFactory =
    mock[DeleteInsurancePoliciesValidatorFactory]

  object MockedDeleteInsurancePoliciesValidatorFactory {

    def validator(): CallHandler[Validator[DeleteInsurancePoliciesRequestData]] =
      (mockDeleteInsurancePoliciesValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }

  def willUseValidator(use: Validator[DeleteInsurancePoliciesRequestData]): CallHandler[Validator[DeleteInsurancePoliciesRequestData]] = {
    MockedDeleteInsurancePoliciesValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: DeleteInsurancePoliciesRequestData): Validator[DeleteInsurancePoliciesRequestData] =
    new Validator[DeleteInsurancePoliciesRequestData] {
      def validate: Validated[Seq[MtdError], DeleteInsurancePoliciesRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[DeleteInsurancePoliciesRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[DeleteInsurancePoliciesRequestData] =
    new Validator[DeleteInsurancePoliciesRequestData] {
      def validate: Validated[Seq[MtdError], DeleteInsurancePoliciesRequestData] = Invalid(result)
    }

}
