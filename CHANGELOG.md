# Changelog

This file records the changes to RandIMI since version 2.0.0 (2023-08-23).

## 2.3.0 (2026-07-31)

### New features

- Added minimization as a randomization algorithm, including configurable
  imbalance functions and parameters. ([#17010](https://bugtracker.uni-muenster.de/issues/17010))
- Added study statistics views for sites and strata, with tables, interactive
  charts, filtering, zooming, stacking, capacity indicators, and image export.
  ([#11684](https://bugtracker.uni-muenster.de/issues/11684))
- Added stable API IDs for studies, sites, study arms, and strata. API IDs are
  used by the API and in URLs, can be displayed in overviews, and can optionally
  be included in exports. ([#15480](https://bugtracker.uni-muenster.de/issues/15480))
- Added study archiving, reactivation, soft deletion, and configurable retention
  periods, including reminder emails and automatic cleanup.
  ([#14677](https://bugtracker.uni-muenster.de/issues/14677))
- Added study-wide site permissions and the `LOCAL_MANAGER` role. Local managers
  can manage invitations and users within their own permissions.
  ([#14678](https://bugtracker.uni-muenster.de/issues/14678),
  [#16221](https://bugtracker.uni-muenster.de/issues/16221))
- Added API v2 resources and endpoints for retrieving study definitions and
  updating subjects. Randomization errors now return details about conflicting
  subjects, and randomization responses include the assigned study arm.
  ([#15747](https://bugtracker.uni-muenster.de/issues/15747),
  [#16718](https://bugtracker.uni-muenster.de/issues/16718))
- Added support for changing subject pseudonyms and for modifying pseudonym
  handling in active studies. ([#16718](https://bugtracker.uni-muenster.de/issues/16718),
  [#17691](https://bugtracker.uni-muenster.de/issues/17691))
- Added subject detail dialogs and deletion/release timestamps.
  ([#15496](https://bugtracker.uni-muenster.de/issues/15496),
  [#11684](https://bugtracker.uni-muenster.de/issues/11684))
- Added selectable CSV delimiters for study exports.
  ([#15783](https://bugtracker.uni-muenster.de/issues/15783))
- Added persistence of table states between page visits.
  ([#16053](https://bugtracker.uni-muenster.de/issues/16053))

### Improvements

- Reworked the study and subject-list interfaces, including subject-list
  overviews, subject details, status displays, sorting, and hidden-subject counts.
  ([#11684](https://bugtracker.uni-muenster.de/issues/11684),
  [#15496](https://bugtracker.uni-muenster.de/issues/15496),
  [#18039](https://bugtracker.uni-muenster.de/issues/18039))
- Display dates according to the selected language.
  ([#17530](https://bugtracker.uni-muenster.de/issues/17530))
- Trim text input consistently in the browser and backend, and strengthened
  validation of site capacities, study capacities, names, and active studies.
  ([#16916](https://bugtracker.uni-muenster.de/issues/16916),
  [#16919](https://bugtracker.uni-muenster.de/issues/16919))
- Improved the study permission model for archived and deleted studies and for
  API access. ([#14677](https://bugtracker.uni-muenster.de/issues/14677),
  [#14678](https://bugtracker.uni-muenster.de/issues/14678),
  [#16221](https://bugtracker.uni-muenster.de/issues/16221))
- Improved audit entries for subject conflicts, study status changes, and
  retention periods, and corrected audit-trail rendering and localization.
  ([#14677](https://bugtracker.uni-muenster.de/issues/14677),
  [#15747](https://bugtracker.uni-muenster.de/issues/15747),
  [#16901](https://bugtracker.uni-muenster.de/issues/16901),
  [#17178](https://bugtracker.uni-muenster.de/issues/17178))
- Updated the interface to a newer Bootstrap release and refreshed tables,
  navigation, tabs, modals, forms, colors, and responsive chart behavior.
  ([#16164](https://bugtracker.uni-muenster.de/issues/16164))
- Improved exports with API IDs, corrected strata and status columns, and fixed
  filtering, ordering, and file-count calculations.
  ([#11684](https://bugtracker.uni-muenster.de/issues/11684),
  [#15480](https://bugtracker.uni-muenster.de/issues/15480))
- Simplified the start page and improved activation redirects and user-facing
  success and error messages. ([#16279](https://bugtracker.uni-muenster.de/issues/16279),
  [#16979](https://bugtracker.uni-muenster.de/issues/16979))

### Fixes

- Fixed statistics for pre-generated, released, deleted, and stratified subjects.
  ([#11684](https://bugtracker.uni-muenster.de/issues/11684))
- Fixed site lookup and randomization using API IDs, including API v1 and v2
  permission checks. ([#15480](https://bugtracker.uni-muenster.de/issues/15480),
  [#16017](https://bugtracker.uni-muenster.de/issues/16017))
- Fixed editing and deleting studies, subjects, permissions, and invitations in
  locked, archived, deleted, and test-mode studies.
  ([#14677](https://bugtracker.uni-muenster.de/issues/14677),
  [#14678](https://bugtracker.uni-muenster.de/issues/14678),
  [#15496](https://bugtracker.uni-muenster.de/issues/15496),
  [#16221](https://bugtracker.uni-muenster.de/issues/16221))
- Fixed pseudonym-conflict handling, stratum ordering, additional table columns,
  CSV export columns, localized messages, and several DataTables rendering issues.
  ([#15747](https://bugtracker.uni-muenster.de/issues/15747),
  [#16718](https://bugtracker.uni-muenster.de/issues/16718),
  [#17530](https://bugtracker.uni-muenster.de/issues/17530))
- Fixed expired password-reset token handling and added automatic token cleanup.

### Operations and development

- Added Docker and Docker Compose configurations for production and development.
  ([#17009](https://bugtracker.uni-muenster.de/issues/17009))
- Added automated test and release pipelines, GitHub release creation, Wiki
  synchronization, and hosted Swagger UI documentation.
  ([#16369](https://bugtracker.uni-muenster.de/issues/16369),
  [#17009](https://bugtracker.uni-muenster.de/issues/17009))
- Updated Spring Boot from 3.3.1 to 3.5.14 and refreshed Maven plugins, Selenium,
  Testcontainers, and other dependencies.
- Expanded controller, migration, service, validation, Selenium, statistics, and
  randomization test coverage; Selenium services now run in test containers.
- Added database migrations for API IDs, retention periods, subject timestamps,
  invitations, password-reset token uniqueness, and minimization.
  ([#14677](https://bugtracker.uni-muenster.de/issues/14677),
  [#15480](https://bugtracker.uni-muenster.de/issues/15480),
  [#16221](https://bugtracker.uni-muenster.de/issues/16221),
  [#17010](https://bugtracker.uni-muenster.de/issues/17010))
- Replaced the Wiki submodule with external documentation and expanded the README
  with installation, Docker, development, and randomization guidance.
  ([#17009](https://bugtracker.uni-muenster.de/issues/17009))

## 2.2.0 (2024-07-30)

New Features:
- Added custom ratios for study arms (#12609)
- Added option to delete users
- Added page to view pending invitation of a user
- Added sidebar for navigation
- Allow stratifying studies by site after activation under certain conditions (#15247)
- Added test mode for studies (#15171)
- Allow the username to be changed (#15369)
- Added export for subject-lists via UI and API (#15434)
- Lock/Unlock activated studies (RANDIMI-15456)

Improvements:
- Improved general UI
- Improved selection of subject lists (#14728)
- Added a button to show the password
- Added an order number to sites (#14685)
- Improved selection of subject lists (#10731)
- Improved audit entries for subjects
- Improved forms: Moved change reason into a popup, show error on top of the page for invalid forms
- Improved handling of access denied
- Improved handling of unexpected exceptions and page not found exceptions
- Changed API users to normal users with an API role (#15368)
- Always display the name of the current user/ study in the head
- Use application/problem+json for error response
- Allow admins to use the API
- Use siteApiId for identifying sites when using API v1
- Improved swagger UI header and authorization
- Added change reason for new sites
- Improved audit entries for actions that change the status of a study
- Improved icons for disabling/enabling users

Fixes:
- Fixed non-admins being able to modify admins
- Fixed overall permissions
- Fixed changing the password does not check for password requirements (#14720)
- Fixed invitation process cannot be skipped after sending an email (#14726)
- Fixed empty enumerated strata not getting removed when submitting the study
- Fixed renaming sites does not update subject lists and site stratum (#15143)
- Fixed site stratum is shown as stratum (#15159)
- Fixed the visible fieldset for strata in studies that are only stratified by site (#15160)
- Fixed the label for audit type filter in audit trail
- Fixed missing audit entries for API get calls (#15260)
- Fixed API users not being able to change their password (#10610)
- Fixed some localization errors
- Fixed audit entries when updating and deleting studies
- Fixed disabled users not being logged out (#12698)
- Fixed NullPointerException if studyStrataParams were missing

Internal:
- Migrated to Spring Boot 3
- Integrated StratumPartSite
- Linked subject list and stratum parts
- Removed stratum code
- Removed fix for duplicate subject indices as it is not necessary anymore
- Automatically forward success and error messages
- Always use MessageService instead of MessageSource
- Updated dependencies

Testing:
- Fixed caching issues in selenium tests
- Migrated to JUnit Jupiter
- Added a test suite that excludes selenium tests
- Repeat tests that sometimes fail (#15392)

Code Quality:
- Moved inputs into fragments
- Moved popups into fragments (#10575)
- Use fragments for dynamic lists

Development:
- Automatically reload message sources in the dev profile
- Fixed profile to skip selenium tests, skips all tests (#12633)
- Added merge description template

## 2.1.1 (2023-12-04)

- Improved performance by lazy loading and caching
- Fixed roles are not disabled for API users when entering /users/editRoles
- Fixed non-admin user manager could grant and revoke admin rights
- Updated Spring boot to 2.7.18

## 2.1.0 (2023-11-27)

- Added feature to pregenerate subject lists on study activation.
- When migrating from version 1 to 2.1.0, existing subject lists will be converted to pregenerated subject lists.

## 2.0.0 (2023-08-23)
- Complete rework of RandIMI
