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

package api.controllers.requestParsers.validators

import api.models.errors.MtdError

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.ChronoField

package object validations {

  val NoValidationErrors: List[MtdError] = List()

  private val datePattern           = "yyyy-MM-dd"
  val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern(datePattern)

  def checkAmountScale(amount: BigDecimal, maxScale: Int): Boolean                              = !(amount.scale > maxScale)
  def checkAmountRange(amount: BigDecimal, minValue: BigDecimal, maxValue: BigDecimal): Boolean = !(amount > maxValue || amount < minValue)

  val yearFormat: DateTimeFormatter =
    new DateTimeFormatterBuilder()
      .appendPattern("yyyy")
      .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
      .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
      .toFormatter()

}
