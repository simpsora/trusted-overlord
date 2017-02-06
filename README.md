# Trusted Overlord

## Purpose

Trusted Overlord  is a Java tool aimed to aggregate AWS Trusted Advisor alarms from different accounts
and build a brief report with the results.

## Usage

Build Trusted Overlod using Maven with

```
mvn clean compile assembly:single
```

Run Trusted Overlod passing your profile names as arguments.

```
java -jar trustedoverlord-1.0-SNAPSHOT-jar-with-dependencies.jar profileName1 profileName2...
```

Please note that you must configure permissions using the [standard method](https://aws.amazon.com/blogs/security/a-new-and-standardized-way-to-manage-credentials-in-the-aws-sdks/)

