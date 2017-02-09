![Logo](static/logo.png "Trusted Overlord")

## Purpose

Trusted Overlord  is a Java tool aimed to aggregate AWS Trusted Advisor alarms from different accounts
and build a brief report with the results.

## Usage

Build Trusted Overlord using Maven with

```
mvn clean compile package
```

Run Trusted Overlod passing your profile names as arguments.

```
java -jar trustedoverlord-1.0-SNAPSHOT.jar profileName1 profileName2...
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


2017-02-08T10:27:35,262 INFO  [main] c.b.t.TrustedOverlord: ...will now check 1 AWS accounts.

=====================================================================
Checking Health for profile 'profileName1'
=====================================================================
2017-02-08T10:27:36,283 INFO  [main] c.b.t.TrustedOverlord:  # Open Issues: 0
2017-02-08T10:27:36,283 INFO  [main] c.b.t.TrustedOverlord:  # Schedules Changes: 0
2017-02-08T10:27:36,283 INFO  [main] c.b.t.TrustedOverlord:  # Other Notifications: 1
2017-02-08T10:27:36,283 INFO  [main] c.b.t.TrustedOverlord:
2017-02-08T10:27:36,283 INFO  [main] c.b.t.TrustedOverlord:  + Other Notification: AWS_ECS_OPERATIONAL_NOTIFICATION

=====================================================================
Checking Trusted Advisor for profile 'profileName1'
=====================================================================
2017-02-08T10:27:47,737 INFO  [main] c.b.t.TrustedOverlord:  # Errors: 2
2017-02-08T10:27:47,737 INFO  [main] c.b.t.TrustedOverlord:  # Warnings: 14
2017-02-08T10:27:47,737 INFO  [main] c.b.t.TrustedOverlord:
2017-02-08T10:27:47,738 ERROR [main] c.b.t.TrustedOverlord:  + Error: Amazon EBS Snapshots
2017-02-08T10:27:47,738 ERROR [main] c.b.t.TrustedOverlord:  + Error: Amazon EC2 Availability Zone Balance
2017-02-08T10:27:47,738 WARN  [main] c.b.t.TrustedOverlord:  + Warning: Low Utilization Amazon EC2 Instances
2017-02-08T10:27:47,738 WARN  [main] c.b.t.TrustedOverlord:  + Warning: Underutilized Amazon EBS Volumes
2017-02-08T10:27:47,738 WARN  [main] c.b.t.TrustedOverlord:  + Warning: Unassociated Elastic IP Addresses
2017-02-08T10:27:47,738 WARN  [main] c.b.t.TrustedOverlord:  + Warning: MFA on Root Account
2017-02-08T10:27:47,738 WARN  [main] c.b.t.TrustedOverlord:  + Warning: IAM Password Policy
2017-02-08T10:27:47,738 WARN  [main] c.b.t.TrustedOverlord:  + Warning: Load Balancer Optimization
2017-02-08T10:27:47,738 WARN  [main] c.b.t.TrustedOverlord:  + Warning: Service Limits
2017-02-08T10:27:47,738 WARN  [main] c.b.t.TrustedOverlord:  + Warning: Amazon S3 Bucket Logging
2017-02-08T10:27:47,738 WARN  [main] c.b.t.TrustedOverlord:  + Warning: ELB Listener Security
2017-02-08T10:27:47,738 WARN  [main] c.b.t.TrustedOverlord:  + Warning: ELB Security Groups
2017-02-08T10:27:47,738 WARN  [main] c.b.t.TrustedOverlord:  + Warning: ELB Cross-Zone Load Balancing
2017-02-08T10:27:47,738 WARN  [main] c.b.t.TrustedOverlord:  + Warning: ELB Connection Draining
2017-02-08T10:27:47,738 WARN  [main] c.b.t.TrustedOverlord:  + Warning: IAM Access Key Rotation
2017-02-08T10:27:47,738 WARN  [main] c.b.t.TrustedOverlord:  + Warning: Amazon S3 Bucket Versioning

=====================================================================
Checking Health for profile 'profileName2'
=====================================================================
...

```

