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

package config

import com.typesafe.config.Config
import play.api.{ConfigLoader, Configuration}
import routing.Version
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

trait AppConfig {

  lazy val ifsDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = ifsBaseUrl, env = ifsEnv, token = ifsToken, environmentHeaders = ifsEnvironmentHeaders)

  lazy val taxYearSpecificIfsDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = tysIfsBaseUrl, env = tysIfsEnv, token = tysIfsToken, environmentHeaders = tysIfsEnvironmentHeaders)

  lazy val api1661DownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = api1661BaseUrl, env = api1661Env, token = api1661Token, environmentHeaders = api1661EnvironmentHeaders)

  // IFS Config
  def ifsBaseUrl: String

  def ifsEnv: String

  def ifsToken: String

  def ifsEnvironmentHeaders: Option[Seq[String]]

  // Tax Year Specific (TYS) IFS Config
  def tysIfsBaseUrl: String

  def tysIfsEnv: String

  def tysIfsToken: String

  def tysIfsEnvironmentHeaders: Option[Seq[String]]

  // Api1661 Config
  def api1661BaseUrl: String

  def api1661Env: String

  def api1661Token: String

  def api1661EnvironmentHeaders: Option[Seq[String]]

  // MTD IF Lookup Config
  def mtdIdBaseUrl: String

  def apiGatewayContext: String

  def apiStatus(version: Version): String

  def endpointsEnabled(version: Version): Boolean

  def featureSwitches: Configuration

  def confidenceLevelConfig: ConfidenceLevelConfig
  def minimumPermittedTaxYear: Int
}

@Singleton
class AppConfigImpl @Inject() (config: ServicesConfig, configuration: Configuration) extends AppConfig {

  val mtdIdBaseUrl: String = config.baseUrl("mtd-id-lookup")

  // IFS Config
  val ifsBaseUrl: String = config.baseUrl("ifs")
  val ifsEnv: String = config.getString("microservice.services.ifs.env")
  val ifsToken: String = config.getString("microservice.services.ifs.token")
  val ifsEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.ifs.environmentHeaders")

  // Tax Year Specific (TYS) IFS Config
  val tysIfsBaseUrl: String = config.baseUrl("tys-ifs")
  val tysIfsEnv: String = config.getString("microservice.services.tys-ifs.env")
  val tysIfsToken: String = config.getString("microservice.services.tys-ifs.token")
  val tysIfsEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.tys-ifs.environmentHeaders")

  // API1661 Config
  val api1661BaseUrl: String = config.baseUrl("api1661")
  val api1661Env: String = config.getString("microservice.services.api1661.env")
  val api1661Token: String = config.getString("microservice.services.api1661.token")
  val api1661EnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.api1661.environmentHeaders")

  // MTD IF Lookup Config
  val apiGatewayContext: String = config.getString("api.gateway.context")
  val confidenceLevelConfig: ConfidenceLevelConfig = configuration.get[ConfidenceLevelConfig](s"api.confidence-level-check")

  def apiStatus(version: Version): String = config.getString(s"api.$version.status")

  def featureSwitches: Configuration = configuration.getOptional[Configuration](s"feature-switch").getOrElse(Configuration.empty)

  def endpointsEnabled(version: Version): Boolean = config.getBoolean(s"api.$version.endpoints.enabled")

  val minimumPermittedTaxYear: Int = config.getInt("minimumPermittedTaxYear")
}

case class ConfidenceLevelConfig(confidenceLevel: ConfidenceLevel, definitionEnabled: Boolean, authValidationEnabled: Boolean)

object ConfidenceLevelConfig {

  implicit val configLoader: ConfigLoader[ConfidenceLevelConfig] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)
    ConfidenceLevelConfig(
      confidenceLevel = ConfidenceLevel.fromInt(config.getInt("confidence-level")).getOrElse(ConfidenceLevel.L200),
      definitionEnabled = config.getBoolean("definition.enabled"),
      authValidationEnabled = config.getBoolean("auth-validation.enabled")
    )
  }

}
