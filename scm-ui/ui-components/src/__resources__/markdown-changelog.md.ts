/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
export default `
# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

# Compl_icated_H€aDer

## [2.16.0] - 2021-03-26
### Added
- Metrics for http requests ([#1586](https://github.com/scm-manager/scm-manager/issues/1586))
- Metrics for executor services ([#1586](https://github.com/scm-manager/scm-manager/issues/1586))
- Metrics about logging, file descriptors, process threads and process memory ([#1609](https://github.com/scm-manager/scm-manager/pull/1609))
- Metrics for events ([#1601](https://github.com/scm-manager/scm-manager/pull/1601))
- Authentication and access metrics ([#1595](https://github.com/scm-manager/scm-manager/pull/1595))
- Adds metrics over lifetime duration of working copies ([#1591](https://github.com/scm-manager/scm-manager/pull/1591))
- Collect guava caching statistics as metrics ([#1590](https://github.com/scm-manager/scm-manager/pull/1590))
- Add global flag to enable/disable api keys ([#1606](https://github.com/scm-manager/scm-manager/pull/1606))

### Fixed
- Adjust path and filename validation to prevent path traversal ([#1604](https://github.com/scm-manager/scm-manager/pull/1604))
- Wrong subject context for asynchronous subscriber ([#1601](https://github.com/scm-manager/scm-manager/pull/1601))
- Fix repository creation route from repository namespace overview page ([#1602](https://github.com/scm-manager/scm-manager/pull/1602))
- external nav links now correctly collapse when used in a menu ([#1596](https://github.com/scm-manager/scm-manager/pull/1596))
- Response with exception stack trace for invalid urls ([#1605](https://github.com/scm-manager/scm-manager/pull/1605))
- Do not show repositories on overview for not existing namespace ([#1608](https://github.com/scm-manager/scm-manager/pull/1608))

### Changed
- Show "CUSTOM" name instead empty entry for permission roles ([#1597](https://github.com/scm-manager/scm-manager/pull/1597))
- Improve error messages for invalid media types ([#1607](https://github.com/scm-manager/scm-manager/pull/1607))
- Allow all UTF-8 characters except URL identifiers as user and group names and for namespaces. ([#1600](https://github.com/scm-manager/scm-manager/pull/1600))

## [2.15.1] - 2021-03-17
### Fixed
- Encode revision on extension points to fix breaking change ([#1585](https://github.com/scm-manager/scm-manager/pull/1585))
- Index link collection in repository initialize extensions ([#1594](https://github.com/scm-manager/scm-manager/issues/1588) and [#1587](https://github.com/scm-manager/scm-manager/issues/1594))
- Mercurial encoding configuration per repository ([#1577](https://github.com/scm-manager/scm-manager/issues/1577), [#1583](https://github.com/scm-manager/scm-manager/issues/1583))
- Authentication names in open api spec ([#1582](https://github.com/scm-manager/scm-manager/issues/1582))
- Sometimes no redirect after login ([#1592](https://github.com/scm-manager/scm-manager/pull/1592))
- Navigate after search ([#1589](https://github.com/scm-manager/scm-manager/pull/1589))
- Diff for mercurial and subversion ([#1588](https://github.com/scm-manager/scm-manager/issues/1588) and [#1587](https://github.com/scm-manager/scm-manager/issues/1587))

## [2.15.0] - 2021-03-12
### Added
- Create api for markdown ast plugins ([#1578](https://github.com/scm-manager/scm-manager/pull/1578))
- Partial diff ([#1581](https://github.com/scm-manager/scm-manager/issues/1581))
- Added filepath search ([#1568](https://github.com/scm-manager/scm-manager/issues/1568))
- API for metrics ([#1576](https://github.com/scm-manager/scm-manager/issues/1576))
- Add repository-specific non-fast-forward disallowed option ([#1579](https://github.com/scm-manager/scm-manager/issues/1579))

### Fixed
- Fix wrapping of title and actions in source view ([#1569](https://github.com/scm-manager/scm-manager/issues/1569))
- Split SetupContextListener logic into new Privileged Startup API ([#1573](https://github.com/scm-manager/scm-manager/pull/1573))
- Mark configuration files in debian package ([#1574](https://github.com/scm-manager/scm-manager/issues/1574))

## [2.14.1] - 2021-03-03
### Fixed
- Prevent breadcrumb overflow and shrink large elements ([#1563](https://github.com/scm-manager/scm-manager/pull/1563))
- Clarify that FileUpload component does not upload directly ([#1566](https://github.com/scm-manager/scm-manager/pull/1566))
- Prevent xss from stored markdown ([#1566](https://github.com/scm-manager/scm-manager/pull/1566))
- Fix endless loading spinner for sources of empty repositories ([#1565](https://github.com/scm-manager/scm-manager/issues/1565))
- Fix missing permalink button to markdown headings ([#1564](https://github.com/scm-manager/scm-manager/pull/1564))
- Fix redirect after logout if is set

## [2.14.0] - 2021-03-01
### Added
- Repository data can be migrated independently to enable the import of dumps from older versions ([#1526](https://github.com/scm-manager/scm-manager/pull/1526))
- XML attribute in root element of config entry stores ([#1545](https://github.com/scm-manager/scm-manager/pull/1545))
- Add option to encrypt repository exports with a password and decrypt them on repository import ([#1533](https://github.com/scm-manager/scm-manager/pull/1533))
- Make repository export asynchronous. ([#1533](https://github.com/scm-manager/scm-manager/pull/1533))
- Lock repository to "read-only" access during export ([#1519](https://github.com/scm-manager/scm-manager/pull/1519))
- Warn user to not leave page during repository import ([#1536](https://github.com/scm-manager/scm-manager/pull/1536))
- Import repository permissions from repository archive ([#1520](https://github.com/scm-manager/scm-manager/pull/1520))
- Added import protocols ([#1558](https://github.com/scm-manager/scm-manager/pull/1558))

### Fixed
- Loading of cache configuration from plugins ([#1540](https://github.com/scm-manager/scm-manager/pull/1540))
- Missing error message for wrong password ([#1527](https://github.com/scm-manager/scm-manager/pull/1527))
- Sporadic error in reading git pack files ([#1518](https://github.com/scm-manager/scm-manager/issues/1518))
- Fix permission check for branch deletion ([#1515](https://github.com/scm-manager/scm-manager/pull/1515))
- Fix broken mercurial http post args configuration ([#1532](https://github.com/scm-manager/scm-manager/issues/1532))
- Do not resolve external groups for system accounts ([#1541](https://github.com/scm-manager/scm-manager/pull/1541))
- Wrong redirect on paginated overviews  ([#1535](https://github.com/scm-manager/scm-manager/pull/1535))

### Changed
- Config entry stores are handled explicitly in exports ([#1545](https://github.com/scm-manager/scm-manager/pull/1545))
- Allow usage of cache as shiro authentication and authorization cache ([#1540](https://github.com/scm-manager/scm-manager/pull/1540))
- Implement new changelog process ([#1517](https://github.com/scm-manager/scm-manager/issues/1517))
- Fire post receive repository hook event after the repository import has been finished. ([#1544](https://github.com/scm-manager/scm-manager/pull/1544))
- improve frontend performance with stale while revalidate pattern ([#1555](https://github.com/scm-manager/scm-manager/pull/1555))
- Change the order of files inside the repository archive ([#1538](https://github.com/scm-manager/scm-manager/pull/1538))

## [2.13.0] - 2021-01-29
### Added
- Repository export for Subversion ([#1488](https://github.com/scm-manager/scm-manager/pull/1488))
- Provide more options for Helm chart ([#1485](https://github.com/scm-manager/scm-manager/pull/1485))
- Option to create a permanent link to a source file ([#1489](https://github.com/scm-manager/scm-manager/pull/1489))
- Markdown codeblock renderer extension point ([#1492](https://github.com/scm-manager/scm-manager/pull/1492))
- Java version added to plugin center url ([#1494](https://github.com/scm-manager/scm-manager/pull/1494))
- Font ttf-dejavu included oci image ([#1498](https://github.com/scm-manager/scm-manager/issues/1498))
- Repository import and export with metadata for Subversion ([#1501](https://github.com/scm-manager/scm-manager/pull/1501))
- API for store rename/delete in update steps ([#1505](https://github.com/scm-manager/scm-manager/pull/1505))
- Import and export for Git via dump file ([#1507](https://github.com/scm-manager/scm-manager/pull/1507))
- Import and export for Mercurial via dump file ([#1511](https://github.com/scm-manager/scm-manager/pull/1511))

### Changed
- Directory name for git LFS files ([#1504](https://github.com/scm-manager/scm-manager/pull/1504))
- Temporary data for repositories is kept in the repository directory, not in a global directory ([#1510](https://github.com/scm-manager/scm-manager/pull/1510))
- Migrate integration tests to bdd ([#1497](https://github.com/scm-manager/scm-manager/pull/1497))
- Layout of proxy settings ([#1502](https://github.com/scm-manager/scm-manager/pull/1502))
- Apply test ids to production builds for usage in e2e tests ([#1499](https://github.com/scm-manager/scm-manager/pull/1499))
- Bump google guava version to 30.1-jre
- Refactor table component so that it can be styled by styled-components ([#1503](https://github.com/scm-manager/scm-manager/pull/1503))
- Enrich styleguide with new features, rules and changes ([#1506](https://github.com/scm-manager/scm-manager/pull/1506))

### Fixed
- Add explicit provider setup for bouncy castle ([#1500](https://github.com/scm-manager/scm-manager/pull/1500))
- Repository contact information is editable ([#1508](https://github.com/scm-manager/scm-manager/pull/1508))
- Usage of custom realm description for scm protocols ([#1512](https://github.com/scm-manager/scm-manager/pull/1512))

## [2.12.0] - 2020-12-17
### Added
- Add repository import via dump file for Subversion ([#1471](https://github.com/scm-manager/scm-manager/pull/1471))
- Add support for permalinks to lines in source code view ([#1472](https://github.com/scm-manager/scm-manager/pull/1472))
- Add "archive" flag for repositories to make them immutable ([#1477](https://github.com/scm-manager/scm-manager/pull/1477))

### Changed
- Implement mercurial cgi protocol as extension ([#1458](https://github.com/scm-manager/scm-manager/pull/1458))

### Fixed
- Add "Api Key" page link to sub-navigation of "User" and "Me" sections ([#1464](https://github.com/scm-manager/scm-manager/pull/1464))
- Empty page on repository namespace filter ([#1476](https://github.com/scm-manager/scm-manager/pull/1476))
- Usage of namespace filter and search action together on repository overview ([#1476](https://github.com/scm-manager/scm-manager/pull/1476))
- Fix tooltip arrow height in firefox ([#1479](https://github.com/scm-manager/scm-manager/pull/1479))
- Accidentally blocked requests with non ascii characters ([#1480](https://github.com/scm-manager/scm-manager/issues/1480) and [#1469](https://github.com/scm-manager/scm-manager/issues/1469))

## [2.11.1] - 2020-12-07
### Fixed
- Initialization of new git repository with master set as default branch ([#1467](https://github.com/scm-manager/scm-manager/issues/1467) and [#1470](https://github.com/scm-manager/scm-manager/pull/1470))

## [2.11.0] - 2020-12-04
### Added
- Add tooltips to short links on repository overview ([#1441](https://github.com/scm-manager/scm-manager/pull/1441))
- Show the date of the last commit for branches in the frontend ([#1439](https://github.com/scm-manager/scm-manager/pull/1439))
- Unify and add description to key view across user settings ([#1440](https://github.com/scm-manager/scm-manager/pull/1440))
- Healthcheck for docker image ([#1428](https://github.com/scm-manager/scm-manager/issues/1428) and [#1454](https://github.com/scm-manager/scm-manager/issues/1454))
- Tags can now be added and deleted through the ui ([#1456](https://github.com/scm-manager/scm-manager/pull/1456))
- The ui now displays tag signatures ([#1456](https://github.com/scm-manager/scm-manager/pull/1456))
- Repository import via URL for git ([#1460](https://github.com/scm-manager/scm-manager/pull/1460))
- Repository import via URL for hg ([#1463](https://github.com/scm-manager/scm-manager/pull/1463))

### Changed
- Send mercurial hook callbacks over separate tcp socket instead of http ([#1416](https://github.com/scm-manager/scm-manager/pull/1416))

### Fixed
- Language detection of files with interpreter parameters e.g.: \`#!/usr/bin/make -f\` ([#1450](https://github.com/scm-manager/scm-manager/issues/1450))
- Unexpected mercurial server pool stop ([#1446](https://github.com/scm-manager/scm-manager/issues/1446) and [#1457](https://github.com/scm-manager/scm-manager/issues/1457))

## [2.10.1] - 2020-11-24
### Fixed
- Improved logging of failures during plugin installation ([#1442](https://github.com/scm-manager/scm-manager/pull/1442))
- Do not throw exception when plugin file does not exist on cancelled installation ([#1442](https://github.com/scm-manager/scm-manager/pull/1442))

## [2.10.0] - 2020-11-20
### Added
- Delete branches directly in the UI ([#1422](https://github.com/scm-manager/scm-manager/pull/1422))
- Lookup command which provides further repository information ([#1415](https://github.com/scm-manager/scm-manager/pull/1415))
- Include messages from scm protocol in modification or merge errors ([#1420](https://github.com/scm-manager/scm-manager/pull/1420))
- Enhance trace api to accepted status codes ([#1430](https://github.com/scm-manager/scm-manager/pull/1430))
- Add examples to core resources to simplify usage of rest api ([#1434](https://github.com/scm-manager/scm-manager/pull/1434))

### Fixed
- Missing close of hg diff command ([#1417](https://github.com/scm-manager/scm-manager/pull/1417))
- Error on repository initialization with least-privilege user ([#1414](https://github.com/scm-manager/scm-manager/pull/1414))
- Adhere to git quiet flag ([#1421](https://github.com/scm-manager/scm-manager/pull/1421))
- Resolve svn binary diffs properly [#1427](https://github.com/scm-manager/scm-manager/pull/1427)

## [2.9.1] - 2020-11-11
### Fixed
- German translation for repositories view

## [2.9.0] - 2020-11-06
### Added
- Tracing api ([#1393](https://github.com/scm-manager/scm-manager/pull/#1393))
- Automatic user converter for external users ([#1380](https://github.com/scm-manager/scm-manager/pull/1380))
- Create _authenticated group on setup ([#1396](https://github.com/scm-manager/scm-manager/pull/1396))
- The name of the initial git branch can be configured and is set to \`main\` by default ([#1399](https://github.com/scm-manager/scm-manager/pull/1399))

### Fixed
- Internal server error for git sub modules without tree object ([#1397](https://github.com/scm-manager/scm-manager/pull/1397))
- Do not expose subversion commit with id 0 ([#1395](https://github.com/scm-manager/scm-manager/pull/1395))
- Cloning of Mercurial repositories with api keys ([#1407](https://github.com/scm-manager/scm-manager/pull/1407))
- Disable cloning repositories via ssh for anonymous users ([#1403](https://github.com/scm-manager/scm-manager/pull/1403))
- Support anonymous file download through rest api for non-browser clients (e.g. curl or postman) when anonymous mode is set to protocol-only ([#1402](https://github.com/scm-manager/scm-manager/pull/1402))
- SVN diff with property changes ([#1400](https://github.com/scm-manager/scm-manager/pull/1400))
- Branches link in repository overview ([#1404](https://github.com/scm-manager/scm-manager/pull/1404))

## [2.8.0] - 2020-10-27
### Added
- Generation of email addresses for users, where none is configured ([#1370](https://github.com/scm-manager/scm-manager/pull/1370))
- Source code fullscreen view ([#1376](https://github.com/scm-manager/scm-manager/pull/1376))
- Plugins can now expose ui components to be shared with other plugins ([#1382](https://github.com/scm-manager/scm-manager/pull/1382))

### Changed
- Reduce logging of ApiTokenRealm ([#1385](https://github.com/scm-manager/scm-manager/pull/1385))
- Centralise syntax highlighting ([#1382](https://github.com/scm-manager/scm-manager/pull/1382))

### Fixed
- Handling of snapshot plugin dependencies ([#1384](https://github.com/scm-manager/scm-manager/pull/1384))
- SyntaxHighlighting for GoLang ([#1386](https://github.com/scm-manager/scm-manager/pull/1386))
- Privilege escalation for api keys ([#1388](https://github.com/scm-manager/scm-manager/pull/1388))

## [2.6.3] - 2020-10-16
### Fixed
- Missing default permission to manage public gpg keys ([#1377](https://github.com/scm-manager/scm-manager/pull/1377))

## [2.7.1] - 2020-10-14
### Fixed
- Null Pointer Exception on anonymous migration with deleted repositories ([#1371](https://github.com/scm-manager/scm-manager/pull/1371))
- Null Pointer Exception on parsing SVN properties ([#1373](https://github.com/scm-manager/scm-manager/pull/1373))

### Changed
- Reduced logging for invalid JWT or api keys ([#1374](https://github.com/scm-manager/scm-manager/pull/1374))

## [2.7.0] - 2020-10-12
### Added
- Users can create API keys with limited permissions ([#1359](https://github.com/scm-manager/scm-manager/pull/1359))

## [2.6.2] - 2020-10-09
### Added
- Introduce api for handling token validation failed exception ([#1362](https://github.com/scm-manager/scm-manager/pull/1362))

### Fixed
- Align actionbar item horizontal and enforce correct margin between them ([#1358](https://github.com/scm-manager/scm-manager/pull/1358))
- Fix recursive browse command for git ([#1361](https://github.com/scm-manager/scm-manager/pull/1361))
- SubRepository support ([#1357](https://github.com/scm-manager/scm-manager/pull/1357))

## [2.6.1] - 2020-09-30
### Fixed
- Not found error when using browse command in empty hg repository ([#1355](https://github.com/scm-manager/scm-manager/pull/1355))

## [2.6.0] - 2020-09-25
### Added
- Add support for pr merge with prior rebase ([#1332](https://github.com/scm-manager/scm-manager/pull/1332))
- Tags overview for repository ([#1331](https://github.com/scm-manager/scm-manager/pull/1331))
- Permissions can be specified for namespaces ([#1335](https://github.com/scm-manager/scm-manager/pull/1335))
- Show update info on admin information page ([#1342](https://github.com/scm-manager/scm-manager/pull/1342))

### Changed
- Rework modal to use react portal ([#1349](https://github.com/scm-manager/scm-manager/pull/1349))

### Fixed
- Missing synchronization during repository creation ([#1328](https://github.com/scm-manager/scm-manager/pull/1328))
- Missing BranchCreatedEvent for mercurial ([#1334](https://github.com/scm-manager/scm-manager/pull/1334))
- Branch not found right after creation ([#1334](https://github.com/scm-manager/scm-manager/pull/1334))
- Overflow for too long branch names ([#1339](https://github.com/scm-manager/scm-manager/pull/1339))
- Set default branch in branch selector if nothing is selected ([#1338](https://github.com/scm-manager/scm-manager/pull/1338))
- Handling of branch with slashes in source view ([#1340](https://github.com/scm-manager/scm-manager/pull/1340))
- Detect not existing paths correctly in Mercurial ([#1343](https://github.com/scm-manager/scm-manager/pull/1343))
- Return correct revisions for tags in hooks for git repositories ([#1344](https://github.com/scm-manager/scm-manager/pull/1344))
- Add option for concrete commit message in merges without templating ([#1351](https://github.com/scm-manager/scm-manager/pull/1351))

## [2.5.0] - 2020-09-10
### Added
- Tags now have date information attached ([#1305](https://github.com/scm-manager/scm-manager/pull/1305))
- Add support for scroll anchors in url hash of diff page ([#1304](https://github.com/scm-manager/scm-manager/pull/1304))
- Documentation regarding data and plugin migration from v1 to v2 ([#1321](https://github.com/scm-manager/scm-manager/pull/1321))
- Add RepositoryCreationDto with creation context and extension-point for repository initialization ([#1324](https://github.com/scm-manager/scm-manager/pull/1324))
- UI filter and rest endpoints for namespaces ([#1323](https://github.com/scm-manager/scm-manager/pull/1323))

### Fixed
- Redirection to requested page after login in anonymous mode
- Update filter state on property change ([#1327](https://github.com/scm-manager/scm-manager/pull/1327))
- Diff view for svn now handles whitespaces in filenames properly ([1325](https://github.com/scm-manager/scm-manager/pull/1325))
- Validate new namespace on repository rename ([#1322](https://github.com/scm-manager/scm-manager/pull/1322))

## [2.4.1] - 2020-09-01
### Added
- Add "sonia.scm.restart-migration.wait" to set wait in milliseconds before restarting scm-server after migration ([#1308](https://github.com/scm-manager/scm-manager/pull/1308))

### Fixed
- Fix detection of markdown files for files having content does not start with '#' ([#1306](https://github.com/scm-manager/scm-manager/pull/1306))
- Fix broken markdown rendering ([#1303](https://github.com/scm-manager/scm-manager/pull/1303))
- JWT token timeout is now handled properly ([#1297](https://github.com/scm-manager/scm-manager/pull/1297))
- Fix text-overflow in danger zone ([#1298](https://github.com/scm-manager/scm-manager/pull/1298))
- Fix plugin installation error if previously a plugin was installed with the same dependency which is still pending. ([#1300](https://github.com/scm-manager/scm-manager/pull/1300))
- Fix layout overflow on changesets with multiple tags ([#1314](https://github.com/scm-manager/scm-manager/pull/1314))
- Make checkbox accessible from keyboard ([#1309](https://github.com/scm-manager/scm-manager/pull/1309))
- Fix logging of large stacktrace for unknown language ([#1313](https://github.com/scm-manager/scm-manager/pull/1313))
- Fix incorrect word breaking behaviour in markdown ([#1317](https://github.com/scm-manager/scm-manager/pull/1317))
- Remove obsolete revision encoding on sources ([#1315](https://github.com/scm-manager/scm-manager/pull/1315))
- Map generic JaxRS 'web application exceptions' to appropriate response instead of "internal server error" ([#1318](https://github.com/scm-manager/scm-manager/pull/1312))

## [2.4.0] - 2020-08-14
### Added
- Introduced merge detection for receive hooks ([#1278](https://github.com/scm-manager/scm-manager/pull/1278))
- Anonymous mode for the web ui ([#1284](https://github.com/scm-manager/scm-manager/pull/1284))
- Add link to source file in diff sections ([#1267](https://github.com/scm-manager/scm-manager/pull/1267))
- Check versions of plugin dependencies on plugin installation ([#1283](https://github.com/scm-manager/scm-manager/pull/1283))
- Sign PR merges and commits performed through ui with generated private key ([#1285](https://github.com/scm-manager/scm-manager/pull/1285))
- Add generic popover component to ui-components ([#1285](https://github.com/scm-manager/scm-manager/pull/1285))
- Show changeset signatures in ui and add public keys ([#1273](https://github.com/scm-manager/scm-manager/pull/1273))

### Fixed
- Repository names may not end with ".git" ([#1277](https://github.com/scm-manager/scm-manager/pull/1277))
- Add preselected value to options in dropdown component if missing ([#1287](https://github.com/scm-manager/scm-manager/pull/1287))
- Show error message if plugin loading failed ([#1289](https://github.com/scm-manager/scm-manager/pull/1289))
- Fix timing problem with anchor links for markdown view ([#1290](https://github.com/scm-manager/scm-manager/pull/1290))

## [2.3.1] - 2020-08-04
### Added
- New api to resolve SCM-Manager root url ([#1276](https://github.com/scm-manager/scm-manager/pull/1276))

### Changed
- Help tooltips are now multiline by default ([#1271](https://github.com/scm-manager/scm-manager/pull/1271))

### Fixed
- Fixed unnecessary horizontal scrollbar in modal dialogs ([#1271](https://github.com/scm-manager/scm-manager/pull/1271))
- Avoid stacktrace logging when protocol url is accessed outside of request scope ([#1276](https://github.com/scm-manager/scm-manager/pull/1276))

## [2.3.0] - 2020-07-23
### Added
- Add branch link provider to access branch links in plugins ([#1243](https://github.com/scm-manager/scm-manager/pull/1243))
- Add key value input field component ([#1246](https://github.com/scm-manager/scm-manager/pull/1246))
- Update installed optional plugin dependencies upon plugin upgrade ([#1260](https://github.com/scm-manager/scm-manager/pull/1260))

### Changed
- Adding start delay to liveness and readiness probes in helm chart template
- Init svn repositories with trunk folder ([#1259](https://github.com/scm-manager/scm-manager/pull/1259))
- Show line numbers in source code view by default ([#1265](https://github.com/scm-manager/scm-manager/pull/1265))

### Fixed
- Fixed file extension detection with new spotter version
- Fixed wrong cache directory location ([#1236](https://github.com/scm-manager/scm-manager/issues/1236) and [#1242](https://github.com/scm-manager/scm-manager/issues/1242))
- Fixed error in update step ([#1237](https://github.com/scm-manager/scm-manager/issues/1237) and [#1244](https://github.com/scm-manager/scm-manager/issues/1244))
- Fix incorrect trimming of whitespaces in helm chart templates
- Fixed error on empty diff expand response ([#1247](https://github.com/scm-manager/scm-manager/pull/1247))
- Ignore ports on proxy exclusions ([#1256](https://github.com/scm-manager/scm-manager/pull/1256))
- Invalidate branches cache synchronously on create new branch ([#1261](https://github.com/scm-manager/scm-manager/pull/1261))

## [2.2.0] - 2020-07-03
### Added
- Rename repository name (and namespace if permitted) ([#1218](https://github.com/scm-manager/scm-manager/pull/1218))
- Enrich commit mentions in markdown viewer by internal links  ([#1210](https://github.com/scm-manager/scm-manager/pull/1210))
- New extension point \`changeset.description.tokens\` to "enrich" commit messages ([#1231](https://github.com/scm-manager/scm-manager/pull/1231))
- Restart service after rpm or deb package upgrade

### Changed
- Checkboxes can now be 'indeterminate' ([#1215](https://github.com/scm-manager/scm-manager/pull/1215))
- The old frontend extension point \`changeset.description\` is deprecated and should be replaced with \`changeset.description.tokens\` ([#1231](https://github.com/scm-manager/scm-manager/pull/1231))
- Required plugins will be updated, too, when a plugin is updated ([#1233](https://github.com/scm-manager/scm-manager/pull/1233))

### Fixed
- Fixed installation of debian packages on distros without preinstalled \`at\` ([#1216](https://github.com/scm-manager/scm-manager/issues/1216) and [#1217](https://github.com/scm-manager/scm-manager/pull/1217))
- Fixed restart with deb or rpm installation ([#1222](https://github.com/scm-manager/scm-manager/issues/1222) and [#1227](https://github.com/scm-manager/scm-manager/pull/1227))
- Fixed broken migration with empty security.xml ([#1219](https://github.com/scm-manager/scm-manager/issues/1219) and [#1221](https://github.com/scm-manager/scm-manager/pull/1221))
- Added missing architecture to debian installation documentation ([#1230](https://github.com/scm-manager/scm-manager/pull/1230))
- Mercurial on Python 3 ([#1232](https://github.com/scm-manager/scm-manager/pull/1232))
- Fixed wrong package information for deb and rpm packages ([#1229](https://github.com/scm-manager/scm-manager/pull/1229))
- Fixed missing content type on migration wizard ([#1234](https://github.com/scm-manager/scm-manager/pull/1234))

## [2.1.1] - 2020-06-23
### Fixed
- Wait until recommended java installation is available for deb packages ([#1209](https://github.com/scm-manager/scm-manager/pull/1209))
- Do not force java home of recommended java dependency for rpm and deb packages ([#1195](https://github.com/scm-manager/scm-manager/issues/1195) and [#1208](https://github.com/scm-manager/scm-manager/pull/1208))
- Migration of non-bare repositories ([#1213](https://github.com/scm-manager/scm-manager/pull/1213))

## [2.1.0] - 2020-06-18
### Added
- Option to configure jvm parameter of docker container with env JAVA_OPTS or with arguments ([#1175](https://github.com/scm-manager/scm-manager/pull/1175))
- Added links in diff views to expand the gaps between "hunks" ([#1178](https://github.com/scm-manager/scm-manager/pull/1178))
- Show commit contributors in table on changeset details view ([#1169](https://github.com/scm-manager/scm-manager/pull/1169))
- Show changeset parents on changeset details view ([#1189](https://github.com/scm-manager/scm-manager/pull/1189))
- Annotate view to display commit metadata for each line of a file ([#1196](https://github.com/scm-manager/scm-manager/pull/1196))

### Fixed
- Avoid caching of detected browser language ([#1176](https://github.com/scm-manager/scm-manager/pull/1176))
- Fixes configuration of jetty listener address with system property \`jetty.host\` ([#1173](https://github.com/scm-manager/scm-manager/pull/1173), [#1174](https://github.com/scm-manager/scm-manager/pull/1174))
- Fixes loading plugin bundles with context path \`/\` ([#1182](https://github.com/scm-manager/scm-manager/pull/1182/files), [#1181](https://github.com/scm-manager/scm-manager/issues/1181))
- Sets the new plugin center URL once ([#1184](https://github.com/scm-manager/scm-manager/pull/1184))
- Diffs with CR characters are parsed correctly ([#1185](https://github.com/scm-manager/scm-manager/pull/1185))
- Close file lists in migration ([#1191](https://github.com/scm-manager/scm-manager/pull/1191))
- Use command in javahg.py from registrar (Upgrade to newer javahg version)  ([#1192](https://github.com/scm-manager/scm-manager/pull/1192))
- Fixed wrong e-tag format ([sdorra/web-resource #1](https://github.com/sdorra/web-resources/pull/1))
- Fixed refetching loop for non existing changesets ([#1203](https://github.com/scm-manager/scm-manager/pull/1203))
- Fixed active state of sub navigation items, which are using activeWhenMatch ([#1199](https://github.com/scm-manager/scm-manager/pull/1199))
- Handles repositories in custom directories correctly in migration from 1.x ([#1201](https://github.com/scm-manager/scm-manager/pull/1201))
- Usage of short git commit ids in changeset urls ([#1200](https://github.com/scm-manager/scm-manager/pull/1200))
- Fixes linebreaks in multiline tooltip ([#1207](https://github.com/scm-manager/scm-manager/pull/1207))

## [2.0.0] - 2020-06-04
### Added
- Detect renamed files in git and hg diffs ([#1157](https://github.com/scm-manager/scm-manager/pull/1157))
- ClassLoader and Adapter parameters to typed store apis ([#1111](https://github.com/scm-manager/scm-manager/pull/1111))
- Native packaging for Debian, Red Hat, Windows, Unix, Docker and Kubernetes ([#1165](https://github.com/scm-manager/scm-manager/pull/1165))
- Cache for working directories ([#1166](https://github.com/scm-manager/scm-manager/pull/1166))

### Fixed
- Correctly resolve Links in markdown files ([#1152](https://github.com/scm-manager/scm-manager/pull/1152))
- Missing copy on write in the data store ([#1155](https://github.com/scm-manager/scm-manager/pull/1155))
- Resolved conflicting dependencies for scm-webapp ([#1159](https://github.com/scm-manager/scm-manager/pull/1159))

## [2.0.0-rc8] - 2020-05-08
### Added
- Add iconStyle + onClick option and story shot for icon component ([#1100](https://github.com/scm-manager/scm-manager/pull/1100))
- Making WebElements (Servlet or Filter) optional by using the \`@Requires\` annotation ([#1101](https://github.com/scm-manager/scm-manager/pull/1101))
- Add class to manually validate rest data transfer objects with jakarta validation annotations ([#1114](https://github.com/scm-manager/scm-manager/pull/1114))
- Missing stories for ui-components ([#1140](https://github.com/scm-manager/scm-manager/pull/1140))

### Changed
- Removed the \`requires\` attribute on the \`@Extension\` annotation and instead create a new \`@Requires\` annotation ([#1097](https://github.com/scm-manager/scm-manager/pull/1097))
- Update guide to prevent common pitfalls in ui development ([#1107](https://github.com/scm-manager/scm-manager/pull/1107))
- Use os specific locations for scm home directory ([#1109](https://github.com/scm-manager/scm-manager/pull/1109))
- Use Library/Logs/SCM-Manager on OSX for logging ([#1109](https://github.com/scm-manager/scm-manager/pull/1109))
- Cleanup outdated jaxb annotation in scm-core ([#1136](https://github.com/scm-manager/scm-manager/pull/1136))

### Fixed
- Protocol URI for git commands under windows ([#1108](https://github.com/scm-manager/scm-manager/pull/1108))
- Fix usage of invalid cipher algorithm on newer java versions ([#1110](https://github.com/scm-manager/scm-manager/issues/1110),[#1112](https://github.com/scm-manager/scm-manager/pull/1112))
- Handle obscure line breaks in diff viewer ([#1129](https://github.com/scm-manager/scm-manager/pull/1129))
- Validate subversion client checksum ([#1113](https://github.com/scm-manager/scm-manager/issues/1113))
- Fix plugin manage permission ([#1135](https://github.com/scm-manager/scm-manager/pull/1135))

## [2.0.0-rc7] - 2020-04-09
### Added
- Fire various plugin events ([#1088](https://github.com/scm-manager/scm-manager/pull/1088))
- Display version for plugins ([#1089](https://github.com/scm-manager/scm-manager/pull/1089))

### Changed
- Simplified collapse state management of the secondary navigation ([#1086](https://github.com/scm-manager/scm-manager/pull/1086))
- Ensure same monospace font-family throughout whole SCM-Manager ([#1091](https://github.com/scm-manager/scm-manager/pull/1091))

### Fixed
- Authentication for write requests for repositories with anonymous read access ([#108](https://github.com/scm-manager/scm-manager/pull/1081))
- Submodules in git do no longer lead to a server error in the browser command ([#1093](https://github.com/scm-manager/scm-manager/pull/1093))

## [2.0.0-rc6] - 2020-03-26
### Added
- Extension point to add links to the repository cards from plug ins ([#1041](https://github.com/scm-manager/scm-manager/pull/1041))
- Libc based restart strategy for posix operating systems ([#1079](https://github.com/scm-manager/scm-manager/pull/1079))
- Simple restart strategy with System.exit ([#1079](https://github.com/scm-manager/scm-manager/pull/1079))
- Notification if restart is not supported on the underlying platform ([#1079](https://github.com/scm-manager/scm-manager/pull/1079))
- Extension point before title in repository cards ([#1080](https://github.com/scm-manager/scm-manager/pull/1080))
- Extension point after title on repository detail page ([#1080](https://github.com/scm-manager/scm-manager/pull/1080))

### Changed
- Update resteasy to version 4.5.2.Final
- Update shiro to version 1.5.2
- Use browser built-in EventSource for apiClient subscriptions
- Changeover to MIT license ([#1066](https://github.com/scm-manager/scm-manager/pull/1066))

### Removed
- EventSource Polyfill
- ClassLoader based restart logic ([#1079](https://github.com/scm-manager/scm-manager/pull/1079))

### Fixed
- Build on windows ([#1048](https://github.com/scm-manager/scm-manager/issues/1048), [#1049](https://github.com/scm-manager/scm-manager/issues/1049), [#1056](https://github.com/scm-manager/scm-manager/pull/1056))
- Show specific notification for plugin actions on plugin administration ([#1057](https://github.com/scm-manager/scm-manager/pull/1057))
- Invalid markdown could make parts of the page inaccessible ([#1077](https://github.com/scm-manager/scm-manager/pull/1077))

## [2.0.0-rc5] - 2020-03-12
### Added
- Added footer extension points for links and avatar
- Create OpenAPI specification during build
- Extension point entries with supplied extensionName are sorted ascending
- Possibility to configure git core config entries for jgit like core.trustfolderstat and core.supportsatomicfilecreation
- Babel-plugin-styled-components for persistent generated classnames
- By default, only 100 files will be listed in source view in one request

### Changed
- New footer design
- Update jgit to version 5.6.1.202002131546-r-scm1
- Update svnkit to version 1.10.1-scm1
- Secondary navigation collapsable

### Fixed
- Modification for mercurial repositories with enabled XSRF protection
- Does not throw NullPointerException when merge fails without normal merge conflicts
- Keep file attributes on modification
- Drop Down Component works again with translations

### Removed
- Enunciate rest documentation
- Obsolete fields in data transfer objects

## [2.0.0-rc4] - 2020-02-14
### Added
- Support for Java versions > 8
- Simple ClassLoaderLifeCycle to fix integration tests on Java > 8
- Option to use a function for default collapse state in diffs

### Changed
- Use icon only buttons for diff file controls
- Upgrade [Legman](https://github.com/sdorra/legman) to v1.6.2 in order to fix execution on Java versions > 8
- Upgrade [Lombok](https://projectlombok.org/) to version 1.18.10 in order to fix build on Java versions > 8
- Upgrade [Mockito](https://site.mockito.org/) to version 2.28.2 in order to fix tests on Java versions > 8
- Upgrade smp-maven-plugin to version 1.0.0-rc3

### Fixed
- Committer of new Git commits set to "SCM-Manager <noreply@scm-manager.org>"

## [2.0.0-rc3] - 2020-01-31
### Fixed
- Broken plugin order fixed
- MarkdownViewer in code section renders markdown properly

## [2.0.0-rc2] - 2020-01-29
### Added
- Set individual page title
- Copy on write
- A new repository can be initialized with a branch (for git and mercurial) and custom files (README.md on default)
- Plugins are validated directly after download
- Code highlighting in diffs
- Switch between rendered version and source view for Markdown files

### Changed
- Stop fetching commits when it takes too long
- Unification of source and commits become "code"

### Fixed
- Classloader leak which caused problems when restarting
- Failing git push does not lead to an GitAPIException
- Subversion revision 0 leads to error
- Create mock subject to satisfy legman
- Multiple versions of hibernate-validator caused problems when starting from plugins
- Page title is now set correctly
- Restart after migration

## [2.0.0-rc1] - 2019-12-02
### Added
- Namespace concept and endpoints
- File history
- Global permission concept
- Completely translated into German with all the text and controls of the UI
- Frontend provides further details on corresponding errors
- Repository branch overview, detailed view and create branch functionality
- Search and filter for repos, users and groups
- Repository Permissions roles
- Migration step framework and wizard
- Plugin center integration
- Plugins can be installed (even without restart), updated and uninstalled using the new plugins overview
- Git-LFS support (with SSH authentication)
- Anonymous access via git-clone and API access with anonymous user
- Cache and x-requested-with header to bundle requests
- remove public flag from repository and migrate permissions to anonymous user

[2.0.0-rc1]: https://www.scm-manager.org/download/2.0.0-rc1
[2.0.0-rc2]: https://www.scm-manager.org/download/2.0.0-rc2
[2.0.0-rc3]: https://www.scm-manager.org/download/2.0.0-rc3
[2.0.0-rc4]: https://www.scm-manager.org/download/2.0.0-rc4
[2.0.0-rc5]: https://www.scm-manager.org/download/2.0.0-rc5
[2.0.0-rc6]: https://www.scm-manager.org/download/2.0.0-rc6
[2.0.0-rc7]: https://www.scm-manager.org/download/2.0.0-rc7
[2.0.0-rc8]: https://www.scm-manager.org/download/2.0.0-rc8
[2.0.0]: https://www.scm-manager.org/download/2.0.0
[2.1.0]: https://www.scm-manager.org/download/2.1.0
[2.1.1]: https://www.scm-manager.org/download/2.1.1
[2.2.0]: https://www.scm-manager.org/download/2.2.0
[2.3.0]: https://www.scm-manager.org/download/2.3.0
[2.3.1]: https://www.scm-manager.org/download/2.3.1
[2.4.0]: https://www.scm-manager.org/download/2.4.0
[2.4.1]: https://www.scm-manager.org/download/2.4.1
[2.5.0]: https://www.scm-manager.org/download/2.5.0
[2.6.0]: https://www.scm-manager.org/download/2.6.0
[2.6.1]: https://www.scm-manager.org/download/2.6.1
[2.6.2]: https://www.scm-manager.org/download/2.6.2
[2.6.3]: https://www.scm-manager.org/download/2.6.3
[2.7.0]: https://www.scm-manager.org/download/2.7.0
[2.7.1]: https://www.scm-manager.org/download/2.7.1
[2.8.0]: https://www.scm-manager.org/download/2.8.0
[2.9.0]: https://www.scm-manager.org/download/2.9.0
[2.9.1]: https://www.scm-manager.org/download/2.9.1
[2.10.0]: https://www.scm-manager.org/download/2.10.0
[2.10.1]: https://www.scm-manager.org/download/2.10.1
[2.11.0]: https://www.scm-manager.org/download/2.11.0
[2.11.1]: https://www.scm-manager.org/download/2.11.1
[2.12.0]: https://www.scm-manager.org/download/2.12.0
[2.13.0]: https://www.scm-manager.org/download/2.13.0
[2.14.0]: https://www.scm-manager.org/download/2.14.0
[2.14.1]: https://www.scm-manager.org/download/2.14.1
[2.15.0]: https://www.scm-manager.org/download/2.15.0
[2.15.1]: https://www.scm-manager.org/download/2.15.1
[2.16.0]: https://www.scm-manager.org/download/2.16.0
`;
