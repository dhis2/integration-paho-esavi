# DHIS2 to PAHO ESAVI FHIR Questionnaire Gateway

![Build Status](https://github.com/dhis2/integration-paho-esavi/workflows/CI/badge.svg)

**Requirements**: JDK 17, Maven 3

## Quickstart

Compile the project or download the most recent war file (links under releases).

```shell
$ mvn package
```

This should give you a file inside of `target/` named `integration-paho-esavi.jar`.

Before you start the project you need to set up some basic configuration, this you should put in a file called `application.yml` in your root directory.

```yaml
# Just an example. Please create one and have it alongside your JAR file.
dhis2-to-esavi:
  dhis2:
    base-url: https://example.com/dhis/api
    username: admin
    password: district
    esavi-program-stage-id: lSpdre0srBn
  fhir:
    server-url: https://example.com/fhir # not supported yet
```

After this, you can now run the facade with

```shell
$ java -jar target/integration-paho-esavi.jar
```

## Generate payload

Go to your browser and access the URL `http://localhost:8080/fhir/baseR4/QuestionnaireResponse/{TEI_UID}`. The FHIR payload generated will be returned by the service as HTTP response.

Also, a couple of files will be saved in the filesystem in the folder `output`:
- `dhis2-payload.json`, that contains the json retrieved from the dhis2 instance
- `fhir-payload.json`, that contains the json generated from the service
