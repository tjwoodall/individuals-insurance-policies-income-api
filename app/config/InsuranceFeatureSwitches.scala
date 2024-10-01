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

package config

import play.api.Configuration
import shared.config.{FeatureSwitches, SharedAppConfig}

import javax.inject.Inject

case class InsuranceFeatureSwitches(protected val featureSwitchConfig: Configuration) extends FeatureSwitches {

  @Inject
  def this(appConfig: SharedAppConfig) = this(appConfig.featureSwitchConfig)

}

object InsuranceFeatureSwitches {
  def apply(configuration: Configuration): InsuranceFeatureSwitches = new InsuranceFeatureSwitches(configuration)

  def apply()(implicit appConfig: SharedAppConfig): InsuranceFeatureSwitches = new InsuranceFeatureSwitches(appConfig)
}
