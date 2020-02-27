| Test | Result |
| ---- | ------ |
| **T001** | **authentication with username and password** |
| T001-01: login accepted for valid account (username + password) | passed |
| T001-02: login denied for invalid account (valid username and invalid password) | passed |
| T001-03: login denied for invalid account (invalid username and valid password) | passed |
| T001-04: login denied for invalid account (invalid username and invalid password) | passed |
| T001-05: login denied for locked account (valid username and password) | passed |
| T001-06: change password required at first login (either new account or after password reset) | passed |
| T001-07: account lockout after failed attempts | passed |
| **T002** | **authorization with user roles** |
| T002-01: study level - header shows the correct user role | passed |
| T002-02: site level - header shows the correct user role | passed |
| T002-03: study level - main menu entries by role | passed |
| T002-04: study level - tasks menu entries by role | passed |
| T002-05: site level - main menu entries by role | passed |
| T002-06: site level - tasks menu entries by role | passed |
| **T003** | **account management** |
| T003-01: create user | passed |
| T003-02: reset password (show User Password to Admin) | passed |
| T003-03: remove user | passed |
| T003-04: restore a user | passed |
| T003-05: unlock a user | passed |
| T003-06: set the role of a user | passed |
| T003-07: audit user activity | passed |
| **T010** | **study and site user management** |
| T010-01: assign a user to a study with a role | passed |
| T010-02: search a user in the list of study users | passed |
| T010-03: change the study role of a user | passed |
| T010-04: remove the study role of a user | passed |
| T010-05: restore the study role of a user | passed |
| T010-06: assign a user to a site with a role | passed |
| T010-07: search a user in the list of site users | passed |
| T010-08: change the site role of a user | passed |
| T010-09: remove the site role of a user | passed |
| T010-10: restore the site role of a user | passed |
| **T014** | **data entry** |
|	T014-01: initial data entry	| passed |
| T014-02: mark crf complete	| passed |
| T014-03: administrative editing	| passed |
| **T031** | **study audit log** |
| T031-01: filtering subjects in the study log |	failed for filtering by Date of Birth, otherwise passed |
| T031-02: sorting subjects in the study log | failed for sorting by Status, otherwise passed |
| T031-03: paging through subjects in the study log	| passed |
| T031-04: changes to subjects	| passed |
| T031-05: changes to events	| passed |
| T031-06: changes to event crf's	| passed |
| T031-07: changes to item data	| passed |
