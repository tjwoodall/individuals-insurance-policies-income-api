Insurance Policies Income API
=============================
The Insurance Policies Income API allows a developer to:

- Create and Amend Insurance Policies Income
- Retrieve Insurance Policies Income
- Delete Insurance Policies Income

## Requirements

- Scala 2.13.x
- Java 11
- sbt 1.7.x
- [Service Manager V2](https://github.com/hmrc/sm2)

## Running the micro-service

Run from the console using: `sbt run` (starts on port 7804 by default)

Start the service manager profile: `sm2 --start MTDFB_INDIVIDUALS_INSURANCE_POLICIES_INCOME`

## Running tests

```
sbt test
sbt it:test
```

## Viewing Open API Spec (OAS) docs

To view documentation locally ensure the API is running, and run api-documentation-frontend:
`./run_local_with_dependencies.sh`
Then go to http://localhost:9680/api-documentation/docs/openapi/preview and use this port and version:
`http://localhost:7804/api/conf/1.0/application.yaml`

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