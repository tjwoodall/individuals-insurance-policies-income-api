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

import cats.data.Validated
import cats.implicits.catsSyntaxValidatedId
import com.typesafe.config.Config
import config.Deprecation.{Deprecated, NotDeprecated}
import play.api.{ConfigLoader, Configuration}
import routing.Version
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.ChronoField
import javax.inject.{Inject, Singleton}

trait AppConfig {

  lazy val ifsDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = ifsBaseUrl, env = ifsEnv, token = ifsToken, environmentHeaders = ifsEnvironmentHeaders)

  lazy val taxYearSpecificIfsDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = tysIfsBaseUrl, env = tysIfsEnv, token = tysIfsToken, environmentHeaders = tysIfsEnvironmentHeaders)

  lazy val api1661DownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = api1661BaseUrl, env = api1661Env, token = api1661Token, environmentHeaders = api1661EnvironmentHeaders)

  def appName: String
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
  def safeEndpointsEnabled(version: String): Boolean

  def featureSwitches: Configuration

  def confidenceLevelConfig: ConfidenceLevelConfig
  def minimumPermittedTaxYear: Int

  def allowRequestCannotBeFulfilledHeader(version: Version): Boolean

  def apiDocumentationUrl: String

  def deprecationFor(version: Version): Validated[String, Deprecation]

  /** Currently only for OAS documentation.
    */
  def apiVersionReleasedInProduction(version: String): Boolean

  def endpointReleasedInProduction(version: String, name: String): Boolean

  /** Defaults to false
    */
  def endpointAllowsSupportingAgents(endpointName: String): Boolean
}

@Singleton
class AppConfigImpl @Inject() (config: ServicesConfig, protected[config] val configuration: Configuration) extends AppConfig {
  def appName: String = config.getString("appName")

  val mtdIdBaseUrl: String = config.baseUrl("mtd-id-lookup")

  // IFS Config
  val ifsBaseUrl: String                         = config.baseUrl("ifs")
  val ifsEnv: String                             = config.getString("microservice.services.ifs.env")
  val ifsToken: String                           = config.getString("microservice.services.ifs.token")
  val ifsEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.ifs.environmentHeaders")

  // Tax Year Specific (TYS) IFS Config
  val tysIfsBaseUrl: String                         = config.baseUrl("tys-ifs")
  val tysIfsEnv: String                             = config.getString("microservice.services.tys-ifs.env")
  val tysIfsToken: String                           = config.getString("microservice.services.tys-ifs.token")
  val tysIfsEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.tys-ifs.environmentHeaders")

  // API1661 Config
  val api1661BaseUrl: String                         = config.baseUrl("api1661")
  val api1661Env: String                             = config.getString("microservice.services.api1661.env")
  val api1661Token: String                           = config.getString("microservice.services.api1661.token")
  val api1661EnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.api1661.environmentHeaders")

  // MTD IF Lookup Config
  val apiGatewayContext: String                    = config.getString("api.gateway.context")
  val confidenceLevelConfig: ConfidenceLevelConfig = configuration.get[ConfidenceLevelConfig](s"api.confidence-level-check")

  def apiStatus(version: Version): String = config.getString(s"api.$version.status")

  def featureSwitches: Configuration = configuration.getOptional[Configuration](s"feature-switch").getOrElse(Configuration.empty)

  def endpointsEnabled(version: Version): Boolean = config.getBoolean(s"api.$version.endpoints.enabled")

  /** Like endpointsEnabled, but will return false if version doesn't exist.
    */
  def safeEndpointsEnabled(version: String): Boolean =
    configuration
      .getOptional[Boolean](s"api.$version.endpoints.enabled")
      .getOrElse(false)

  def apiVersionReleasedInProduction(version: String): Boolean =
    confBoolean(
      path = s"api.$version.endpoints.api-released-in-production",
      defaultValue = false
    )

  def endpointReleasedInProduction(version: String, name: String): Boolean =
    apiVersionReleasedInProduction(version) &&
      confBoolean(
        path = s"api.$version.endpoints.released-in-production.$name",
        defaultValue = true
      )

  /** Can't use config.getConfBool as it's typesafe, and the app-config files use strings.
    */
  private def confBoolean(path: String, defaultValue: Boolean): Boolean =
    if (configuration.underlying.hasPath(path)) config.getBoolean(path) else defaultValue

  def endpointAllowsSupportingAgents(endpointName: String): Boolean =
    supportingAgentEndpoints.getOrElse(endpointName, false)

  private val supportingAgentEndpoints: Map[String, Boolean] =
    configuration
      .getOptional[Map[String, Boolean]]("api.supporting-agent-endpoints")
      .getOrElse(Map.empty)

  val minimumPermittedTaxYear: Int = config.getInt("minimumPermittedTaxYear")

  def allowRequestCannotBeFulfilledHeader(version: Version): Boolean =
    config.getBoolean(s"api.$version.endpoints.allow-request-cannot-be-fulfilled-header")

  def apiDocumentationUrl: String =
    configuration
      .get[Option[String]]("api.documentation-url")
      .getOrElse(s"https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/$appName")

  private val DATE_FORMATTER = new DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 23)
    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 59)
    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 59)
    .toFormatter()

  def deprecationFor(version: Version): Validated[String, Deprecation] = {
    val isApiDeprecated: Boolean = apiStatus(version) == "DEPRECATED"

    val deprecatedOn: Option[LocalDateTime] =
      configuration
        .getOptional[String](s"api.$version.deprecatedOn")
        .map(value => LocalDateTime.parse(value, DATE_FORMATTER))

    val sunsetDate: Option[LocalDateTime] =
      configuration
        .getOptional[String](s"api.$version.sunsetDate")
        .map(value => LocalDateTime.parse(value, DATE_FORMATTER))

    val isSunsetEnabled: Boolean =
      configuration.getOptional[Boolean](s"api.$version.sunsetEnabled").getOrElse(true)

    if (isApiDeprecated) {
      (deprecatedOn, sunsetDate, isSunsetEnabled) match {
        case (Some(dO), Some(sD), true) =>
          if (sD.isAfter(dO))
            Deprecated(dO, Some(sD)).valid
          else
            s"sunsetDate must be later than deprecatedOn date for a deprecated version $version".invalid
        case (Some(dO), None, true) => Deprecated(dO, Some(dO.plusMonths(6).plusDays(1))).valid
        case (Some(dO), _, false)   => Deprecated(dO, None).valid
        case _                      => s"deprecatedOn date is required for a deprecated version $version".invalid
      }

    } else NotDeprecated.valid

  }

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
