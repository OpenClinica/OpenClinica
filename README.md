# Welcome!

This is a fork of OpenClinica which attempts to refactor the architecture into a multi-tier architecture with Apache Webserver, Tomcat, Amazon RDS, Elasticache (HTTP Session caching) deployment on AWS. Accompanied is a Cloudformation template that will help deploy the refactored stack to AWS.

OpenClinica is an open source software for Electronic Data Capture (EDC) and Clinical Data Management (CDM) used to optimize clinical trial workflow in a smart and secure fashion. Use OpenClinica to:

- Build studies
- Create eCRFs
- Design rules/edit checks
- Schedule patient visits 
- Capture eCRF data from study sites via the web
- Monitor and manage clinical data
- Audit trails and electronic signatures
- Role-based access controls
- Import/Export Data
- Extract data for analysis and reporting
- and much more!

## Jump Start with AWS CloudFormation


<a href="https://console.aws.amazon.com/cloudformation/home?region=us-east-1#/stacks/new?stackName=OpenClinica&templateURL=https://raw.githubusercontent.com/Pradeep39/OpenClinica/master/cloudformation/openclinica-vpc-cfn.yaml"><img src="https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png" alt="Launch OpenClinica Stack" target="_blank"></a>

Clicking the above Launch Stack button deploys a VPC, with 1 public and 1 private subnet for each of the  3 tiers of the OpenClinica Open Source Clinical Trials Management System(CTMS).The pairs of subnet for each of the tiers are spread across two Availability Zones. Besides the VPC and Subnets deployed in multiple AZs, It also deploys an internet gateway, with a default route on the public subnets, a pair of NAT gateways (one in each AZ), and specifies default routes for them in the private subnets to ensure internet connectivy. Additionally, To realize the multi-tier architecture of the application, Apache WebServer, Apache Tomcat, Redis Elasticache cluster for HTTP Session caching and a PostGres RDS database with the default Juno Diabetes Clinical Trials Study will be deployed. To top it off, the template also provisions, Apache-tomcat AJP13 connector, Redis caching session manager dependencies and the static assets(html/css/js) in Apache web tier.

After the stack creation successfully completes, the outputs tab contains the ALB url to launch the OpenClinica Portal. You may use the below demo accounts to login into the Juno Demo Study

- datamanager / password
- crc_A / password
- investigator_A / password

Below is an architecture diagram that illustrates what the cloudformation stack will create. Please note that to realize the below architecture, this fork of OpenClinica has incorporated not so trivial changes to make the application stateless using Redis for HTTP Session caching.

![Imgur](https://raw.githubusercontent.com/Pradeep39/OpenClinica/master/cloudformation/OpenClinica_Multi_Tier_Arch.png)

## Getting Started

- [System requirements](https://docs.openclinica.com/installation/system-requirements)
- [Report an issue](https://jira.openclinica.com/)
- [Release notes](https://docs.openclinica.com/release-notes)
- [Extensions/Contributions](https://community.openclinica.com/extensions)
- [Installation](https://github.com/OpenClinica/OpenClinica/wiki)

## Request a feature

To request a feature please submit a ticket on [Jira](https://jira.openclinica.com/) or start a discussion on the [OpenClinica Forum](http://forums.openclinica.com).

##Screenshots
![Imgur](http://i.imgur.com/ACXj3L7.jpg "Home screen") 
##![Imgur](http://i.imgur.com/DqHQ05Z.jpg "Subject Matrix")



## License

[GNU LGPL license](https://www.openclinica.com/gnu-lgpl-open-source-license)

