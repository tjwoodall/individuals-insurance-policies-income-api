Individuals Insurance Policies Income API
=============================

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

The Individuals Insurance Policies Income API allows a developer to:

- Create and Amend Insurance Policies Income
- Retrieve Insurance Policies Income
- Delete Insurance Policies Income

## Requirements

- Scala 3.5.x
- Java 21
- sbt 1.10.x
- [Service Manager V2](https://github.com/hmrc/sm2)

## Development setup

Run the microservice from the console using: `sbt run` (starts on port 7804 by default)

Start the service manager profile: 

```bash
sm2 --start MTDFB_INDIVIDUALS_INSURANCE_POLICIES_INCOME
```

## Run tests

Run unit tests: `sbt test`

Run integration tests: `sbt it/test`

## Viewing Open API Spec (OAS) docs

To view the OpenAPI documentation locally, ensure the API is running.

Start the `api-documentation-frontend` and `api-definition` services using the Service Manager profile:

```bash
sm2 -start DEVHUB_PREVIEW_OPENAPI
```

Then navigate to the preview page:

```text
http://localhost:9680/api-documentation/docs/openapi/preview
```

Enter the specification URL using the appropriate port and API version:

```text
http://localhost:7804/api/conf/2.0/application.yaml
```

## Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog)

## Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

## API Reference / Documentation

Available
at [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/individuals-insurance-policies-income-api)

## License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")