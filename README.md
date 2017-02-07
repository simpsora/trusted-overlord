![Logo](static/logo.png "Logo")
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

## Output

```
 _____              _           _   _____                _           _
|_   _|            | |         | | |  _  |              | |         | |
  | |_ __ _   _ ___| |_ ___  __| | | | | |_   _____ _ __| | ___   __| |
  | | '__| | | / __| __/ _ \/ _` | | | | \ \ / / _ \ '__| |/ _ \ / _` |
  | | |  | |_| \__ \ ||  __/ (_| | \ \_/ /\ V /  __/ |  | | (_) | (_| |
  \_/_|   \__,_|___/\__\___|\__,_|  \___/  \_/ \___|_|  |_|\___/ \__,_|

...will now check 12 AWS accounts.

=====================================================================
Checking Trusted Advisor for profile profileName1
=====================================================================

 # Errors: 2
 # Warnings: 14

 + Error: Amazon EBS Snapshots
 + Error: Amazon EC2 Availability Zone Balance
 + Warning: Low Utilization Amazon EC2 Instances
 + Warning: Underutilized Amazon EBS Volumes
 + Warning: Unassociated Elastic IP Addresses
 + Warning: MFA on Root Account
 + Warning: IAM Password Policy
 + Warning: Load Balancer Optimization
 + Warning: Service Limits
 + Warning: Amazon S3 Bucket Logging
 + Warning: ELB Listener Security
 + Warning: ELB Security Groups
 + Warning: ELB Cross-Zone Load Balancing
 + Warning: ELB Connection Draining
 + Warning: IAM Access Key Rotation
 + Warning: Amazon S3 Bucket Versioning

=====================================================================
Checking Trusted Advisor for profile profileName2
=====================================================================
...

```

