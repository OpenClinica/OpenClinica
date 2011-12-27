--------------------------------------------------------------
OPENCLINICA RELEASE NOTES
OpenClinica Version 3.1.2
Document Version 1.0
--------------------------------------------------------------

--------------------------------------------------------------
Updated: 24-Dec-2011 by Richard Jap rjap@openclinica.com
--------------------------------------------------------------


OpenClinica 3.1.2 is the maintenance release of OpenClinica 3.1. It addresses a large number of issues discovered with 3.1 and 3.1.1. The most notable improvements include:

- Fixes to Extract Data issues tied to extract job completion, data format/accuracy 
- Increased data capture performance (especially for very large CRFs with lots of rules)
- Show/Hide improvements allowing use of simple conditionals with rule-based show/hide on CRF items, and numerous other fixes
- Addition of filter/sort persistence on SDV, so when you leave the page and return your filter sort settings will persist.
- Addition of audit entry upon entry of initial value to an item 

The web application and the web services modules of OpenClinica are in separate war files. 

Please refer to the following URL for more information on OpenClinica 3.1.2:

https://wiki.openclinica.com/doku.php?id=developerwiki:oc312 

--------------------------------------------------------------
CONTENTS OF THIS DOCUMENT
--------------------------------------------------------------
 
I.   OVERALL PRODUCT FEATURES
II.  ISSUES ADDRESSED AND KNOWN ISSUES
III. SOFTWARE DEPENDENCIES AND SYSTEM REQUIREMENTS
IV.  ABOUT OPENCLINICA
V.   GNU LGPL LICENSE

--------------------------------------------------------------
I. OVERALL PRODUCT FEATURES
--------------------------------------------------------------

Overall Product Features

The main functionality includes:
* Submit Data: Allows subject enrollment, data submission and validation for use by clinicians and research associates as well as Query Management and Bulk Data Import.
* Monitor and Manage Data: Enables ongoing data management and monitoring
* Extract Data: Enables data extraction and filtering of datasets for use by investigators and principal investigators.
* Study Build: Facilitates creation and management of studies (protocols), sites, CRFs, users and study event definitions by principal investigators and coordinators.
* Administration: Allows overall system oversight, auditing, configuration, and reporting by administrators.

Some key features of OpenClinica include: 

* Organization of clinical research by study protocol and site, each with its own set of authorized users, subjects, study event definitions, and CRFs. Support for sharing resources across studies in a secure and transparent manner. 
* Dynamic generation of web-based CRFs for electronic data capture via user-defined clinical parameters and validation logic specified in portable Excel templates.
* Management of longitudinal data for complex and recurring patient visits.
* Data import/export tools for migration of clinical datasets in excel spreadsheets, local databases and legacy data formats.
* Extensive interfaces for data query and retrieval, across subjects, time, and clinical parameters, with dataset export in common statistical analysis formats. 
* Compliance with 21 CFR Part 11 and HIPAA privacy and security guidelines including use of study-specific user roles and privileges, SSL encryption, and auditing to monitor access and changes by users.
* A robust and scalable technology infrastructure developed using the Java J2EE framework interoperable with relational databases including PostgreSQL (open source) and Oracle 10G, to support the needs of the clinical research enterprise. 



--------------------------------------------------------------
II. ISSUES ADDRESSED AND KNOWN ISSUES
--------------------------------------------------------------
All issues addressed can be found at https://issuetracker.openclinica.com/changelog_page.php?version_id=54

Known issues can be found at https://wiki.openclinica.com/doku.php?id=ocknownissues:start

--------------------------------------------------------------
III. SOFTWARE DEPENDENCIES AND SYSTEM REQUIREMENTS
--------------------------------------------------------------

Pre-requisites (versions):
    Operating system(s): Windows XP, Windows Server 2003, Redhat Enterprise Linux 4.0+
    Browsers: Internet Explorer 7.0, or 8.0 Mozilla Firefox 3.x or above
    JDK : 1.6.0
    Application server : Tomcat 6.0
    Database server : PostgreSQL 8.4 or Oracle 10g
    OpenClinica version for Upgrades only: OpenClinica 3.0.x

The source code has been removed from the distribution package to make it easier to navigate the file structure.  To access the source code, please visit https://svn.akazaresearch.com.

--------------------------------------------------------------
IV. ABOUT OPENCLINICA
--------------------------------------------------------------

OpenClinica: Open Source Software Platform for Clinical Trials Electronic Data Capture
Professional Open Source Solutions for the Clinical Research Enterprise

OpenClinica is a free, open source clinical trial software platform for Electronic Data Capture (EDC) and clinical data management in clinical research. The software is web-based and designed to support all types of clinical studies in diverse research settings. From the ground up, OpenClinica is built on leading independent standards to achieve high levels of interoperability. Its modular architecture and transparent, collaborative development model offer outstanding flexibility while supporting a robust, enterprise-quality solution.

More about OpenClinica: https://www.OpenClinica.com

Software License

OpenClinica is distributed under the GNU Lesser General Public License (GNU LGPL). For details see: https://www.openclinica.com/gnu-lgpl-open-source-license or LICENSE.txt distributed with this distribution.

Developer and Contact Information
--------------------------------------------------------------
OpenClinica LLC, based in Waltham, MA, provides clinical trials informatics solutions based on OpenClinica, the world's most widely used open source clinical trials software.

OpenClinica LLC
460 Totten Pond Rd, Suite 200
Waltham, MA 02451
phone: 617.621.8585
fax: 617.621.0065
email: contact@openclinica.com

For more about our products and services see:
http://www.OpenClinica.com/


--------------------------------------------------------------
V. GNU LGPL LICENSE
--------------------------------------------------------------

OpenClinica is distributed under the GNU Lesser General Public License (GNU LGPL), 
summarized in the Creative Commons text here:

http://creativecommons.org/licenses/LGPL/2.1/

The GNU Lesser General Public License is a Free Software license. Like any Free Software
license, it grants to you the four following freedoms:

0. The freedom to run the program for any purpose.
1. The freedom to study how the program works and adapt it to your needs.
2. The freedom to redistribute copies so you can help your neighbor.
3. The freedom to improve the program and release your improvements to the public, so 
that the whole community benefits.

You may exercise the freedoms specified here provided that you comply with the express conditions of this license. The LGPL is intended for software libraries, rather than executable programs.

The principal conditions are:

* You must conspicuously and appropriately publish on each copy distributed an appropriate copyright notice and disclaimer of warranty and keep intact all the notices that refer to this License and to the absence of any warranty; and give any other recipients of the Program a copy of the GNU Lesser General Public License along with the Program. Any translation of the GNU Lesser General Public License must be accompanied by the GNU Lesser General Public License.
* If you modify your copy or copies of the library or any portion of it, you may distribute the resulting library provided you do so under the GNU Lesser General Public License. However, programs that link to the library may be licensed under terms of your choice, so long as the library itself can be changed. Any translation of the GNU Lesser General Public License must be accompanied by the GNU Lesser General Public License.
* If you copy or distribute the library, you must accompany it with the complete corresponding machine-readable source code or with a written offer, valid for at least three years, to furnish the complete corresponding machine-readable source code. You need not provide source code to programs which link to the library.

Any of these conditions can be waived if you get permission from the copyright holder.
Your fair use and other rights are in no way affected by the above.

For the full GNU LGPL License text, see LICENSE.txt included in this package.