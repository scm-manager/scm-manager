
# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.0.2] - 2024-02-07
### Fixed
- Added configuration option for idle timeout

## [3.0.1] - 2024-02-02
### Fixed
- Enabling forward headers and log appender with environment variable

## [3.0.0] - 2024-01-29
### Added
- Added e2e tests for branch creation

### Fixed
- Race condition sometimes breaking mouse interaction in global search
- URI encode branch of images in markdown files
- Plugin overview header layout not responsive
- Non-functional annotate view for binary files
- Mail validation in front- and backend unified
- Do not mount extensions which are already disabled via configuration

### Changed
- Save collapse status of secondary navigation
- Jetty and many more Libs upgraded (BREAKING)
- Javax to Jakarta Migration (BREAKING)
- SCM Configuration redesigned (BREAKING)

## [2.48.3] - 2023-12-08
### Fixed
- Removed function `toSpliced` due to missing browser support

## [2.48.2] - 2023-12-08
### Fixed
- Race condition sometimes breaking mouse interaction in global search

## [2.48.1] - 2023-11-22
### Fixed
- Window event listener on navbar

## [2.48.0] - 2023-11-16
### Added
- Internal API to modify repository storage locations
- Invalidation of caches and search index
- Radiobutton components are now available in the ui-forms module
- Modifications in hook provider
- Protocol priority order by user preferences

### Fixed
- Display error when the description of a repository is not stored
- Tabs overlapping other elements
- Show real image diff between ancestor and current branch head
- Links to not-existing users in groups
- Remove bottleneck by switching to assisted injection for command
- The useLocalStorage hook in @scm-manager/ui-api now correctly causes a re-render on write
- Set repository importer as owner on metadata import
- Unnecessary scrollbar showing up in markdown views with code tags for certain resolutions
- Use a random time for plugin center updates
- Ahead and behind tag of a branch to screen reader
- Source view with submodules without http or https URL
- Some chip inputs not correctly passing the ref
- Broken global search shortcut
- Accept file names with semicolons

### Changed
- Break paths in diff
- Remove mail for anonymous user
- Improve global search accessibility

## [2.47.0] - 2023-10-11
### Added
- Atomic design page template simple data pages
- Force option to internal push command api
- Update steps for namespaces
- Fields can now get automatically masked, if the field name contains certain phrases
- Textarea component
- The search page now contains another search bar within, that persists the current query
- New api for interactive card details
- Cardtags can now be colored according to 3 distinct styles

### Fixed
- Ignore non-XML files in data store directories
- Bump JGit to version 6.7.0.202309050840-r to fix CVE-2023-4759
- Exception in SVN repositories due to incorrect git initialization
- Numerous IllegalArgumentExceptions and ConcurrentModificationExceptoins in log file
- Make compare accessible
- Compare target when default branch contains slash
- Disable combobox autocomplete
- Update to svnkit 1.10.10-scm2
- Clean up old installation directories when installing plugins
- Make search accessible
- Catch different exceptions after repositories are deleted

### Changed
- The internal API for content action menus were changed, to handle loading states of extensions
- Use card layout for repository overview
- The checkbox now has a bigger click target
- The chip input api now provides an external add button
- OmniSearchbar now makes use of the Combobox

## [2.46.1] - 2023-09-01
### Fixed
- Loading LFS files for imports and mirrors (from 2.44.3)
- Handling of submodules in imports and mirrors (from 2.44.3)

## [2.46.0] - 2023-08-24
### Added
- Popover component
- User sessions can now be configured to be endless

### Fixed
- A typo on a button for loading more lines in the diff tab has been fixed
- Loadingspinner not showing up
- Set git default branch on first push to not-initialized repository
- Programmatically accessible subheadings in footer
- Throw `NoChangesMadeException` for empty commits in SVN repositories
- Position of the cloudogu platform plugin connection checkmark
- Menu moving the repository sidebar if open
- Deactivate Shiro's new `blockTraversal` check in their `InvalidRequestFilter`
- Marshalling of invalid xml characters

### Changed
- Sort repositories alphanumerically per namespace
- Apply new design to ahead behind tag
- Rework branch overview
- Bump Shiro from 1.10.0 to 1.12.0
- Bump Apache Commons Compress from 1.20 to 1.23.0
- Bump Tika from 1.25 to 1.28.5

### Removed
- Chromatic integration

## [2.45.1] - 2023-07-18
### Added
- Optional caching for stores and data files
- Add variables for liveness probe and readiness probe delay in helm chart
- New accessible Combobox component
- Make file search deactivatable via global config
- Architecture documentation for integration and tech stack

### Fixed
- Re-Release of 2.45.0 due to errors during release
- Internal server error with external groups in permission overview
- Svn external contains sub directory
- Security findings (security headers and jetty server information)
- Broken file action menu keyboard interaction
- Improve general performance
- Copying source code in Firefox incorrectly adds extra line breaks

### Changed
- Optimize ui performance for repository overview
- Enhance extensions name logic by allow bind options
- Replace outdated `Autocomplete` component with new combobox
- Change `myCloudogu` to new brand name `cloudogu platform`
- Use Java 17 for Docker containers

## [2.44.3] - 2023-08-31
### Fixed
- Loading LFS files for imports and mirrors
- Handling of submodules in imports and mirrors

## [2.44.2] - 2023-06-23
### Fixed
- Internal server error with external groups in permission overview

## [2.44.1] - 2023-06-13
### Fixed
- Performance (prevent reading of repositories in namespace mapper)

## [2.44.0] - 2023-06-08
### Added
- New chip input component
- New menu component
- New card list component
- New in-memory implementations of the store api for unit tests using JaxB

### Fixed
- Implement delete for audit log wrapper
- Forms randomly resetting when OpenAPI plugin is installed
- React error in global notifications
- User creation form resetting on re-render
- Broken HG write access when anonymous users have read access
- ConfigurationBinder Navlink does not only match exact routes
- Duplicate contributors for single changeset
- Automatically created gpg keys can now be verified by Github
- Allow passing a ref through FileInput to one of its children

### Changed
- Revamp repository tags overview
- Move form list entry button to left side
- Show empty message for form list table without entries

## [2.43.1] - 2023-05-12
### Fixed
- Configuration of default branch in the git mirror command

## [2.43.0] - 2023-04-12
### Added
- Extension points for bottom of information table
- Display images in diffs
- Optional reset button for forms
- Initial implementation of an audit log API
- Enable developers to manage array properties in forms

### Fixed
- Error in frontend without 'default' branch in hg repositories
- Not found exception with SVN externals
- Fix sticky diffs page position after collapse
- Connect labels with their respective inputs for improved accessibility
- Branch and tag validation regarding special characters
- Concurrent modification exception in JAXB stores

### Changed
- Improve committer accuracy
- Use standard configuration store api for general config

## [2.42.3] - 2023-03-10
### Fixed
- Concurrent modification exception in JAXB stores

## [2.42.2] - 2023-03-02
### Fixed
- Performance issues from 2.42.x introduced by the permission overview
- Resolution of repositories from ssh urls with context paths
- Set the default branch in imported Git repositories correctly to the HEAD of the source repository

## [2.42.1] - 2023-02-16
### Fixed
- Remove sshd dependency that may cause an injection failure on server startup with installed ssh plugin

## [2.42.0] - 2023-02-15
### Added
- Copy button to codeblocks

### Fixed
- Correct z-index equal weighting in diff header
- Fix drop down arrow z index
- Fix tab order for primary navigation
- Long text in table cells breaking layout
- Avatar size must not leave boundaries

### Changed
- Update svnkit to version 1.10.10-scm1

## [2.41.1] - 2023-02-16
### Fixed
- Unconditional force push from editor or merges

## [2.41.0] - 2023-01-18
### Added
- Add abstract configuration adapter to simply creating new global configurations
- Markdown component to render images from repository correctly
- Extension point for page size
- New overlay ui module with tooltip component

### Fixed
- The 'revision to merge' in merge results
- Buttons in diff panel headers stuck together

### Changed
- Default host 'scm-manager.org' replaced with 'example.org' in default mail address
- Make diff header sticky

## [2.40.1] - 2022-12-06
### Fixed
- Preselect namespace in repository create forms

## [2.40.0] - 2022-11-22
### Added
- Keyboard navigation for users, groups, branches, tags, sources, changesets and plugins ([#2153](https://github.com/scm-manager/scm-manager/pull/2153))
- Accessibility settings where you can disable keyboard shortcuts ([#2157](https://github.com/scm-manager/scm-manager/pull/2157))
- Keyboard shortcuts for global and repository-specific navigation ([#2122](https://github.com/scm-manager/scm-manager/pull/2122))
- Git import with lfs support ([#2133](https://github.com/scm-manager/scm-manager/pull/2133))
- Keyboard shortcut for global search ([#2118](https://github.com/scm-manager/scm-manager/pull/2118))
- Feedback links to footer ([#2125](https://github.com/scm-manager/scm-manager/pull/2125))
- Keyboard shortcuts documentation ([#2129](https://github.com/scm-manager/scm-manager/pull/2129))
- Add keyboard navigation to repository overview list ([#2146](https://github.com/scm-manager/scm-manager/pull/2146))

### Fixed
- Keyboard shortcuts should be inactive when modals are open ([#2145](https://github.com/scm-manager/scm-manager/pull/2145))
- Git diff request correctly throws NotFoundException if target revision is non-existent ([#2141](https://github.com/scm-manager/scm-manager/pull/2141))
- Concurrent access during Copy-on-write ([#2143](https://github.com/scm-manager/scm-manager/pull/2143))
- Catch SVN error for cat command on non-file nodes ([#2127](https://github.com/scm-manager/scm-manager/pull/2127))
- Handling of old commits as new ones in new branches ([#2130](https://github.com/scm-manager/scm-manager/pull/2130))
- Plugin installation conflict ([#2138](https://github.com/scm-manager/scm-manager/pull/2138))
- Creating a repository permission without a name breaks the repository ([#2126](https://github.com/scm-manager/scm-manager/pull/2126))
- Clone of git repositories with lfs files via ssh ([#2144](https://github.com/scm-manager/scm-manager/pull/2144))
- Invalidate plugin center cache on global configuration change ([#2147](https://github.com/scm-manager/scm-manager/pull/2147))
- Remove forced word break from markdown view ([#2142](https://github.com/scm-manager/scm-manager/pull/2142))

### Changed
- Upgrade Jackson to 2.13.4
- Fix navbar at the top of the page ([#2128](https://github.com/scm-manager/scm-manager/pull/2128))
- Expose api for declaring keyboard shortcuts ([#2139](https://github.com/scm-manager/scm-manager/pull/2139))
- Provide feedback on plugin center status ([#2147](https://github.com/scm-manager/scm-manager/pull/2147))

## [2.39.1] - 2022-10-12
### Fixed
- Handling of old commits as new ones in new branches ([#2130](https://github.com/scm-manager/scm-manager/pull/2130))

## [2.39.0] - 2022-09-14
### Added
- Reindex mechanism for search ([#2104](https://github.com/scm-manager/scm-manager/pull/2104))
- Changesets command to find all repository changesets ([#2106](https://github.com/scm-manager/scm-manager/pull/2106))
- Implement commit search features for git ([#2111](https://github.com/scm-manager/scm-manager/pull/2111))

### Fixed
- Fix docker healthcheck for custom ports, https and forced base url ([#2110](https://github.com/scm-manager/scm-manager/pull/2110))
- Handle missing indexes with own No Op IndexReader ([#2113](https://github.com/scm-manager/scm-manager/pull/2113))
- Handle missing encoding of square brackets in filenames ([#2117](https://github.com/scm-manager/scm-manager/pull/2117))
- Show warning message for invalid search requests ([#2114](https://github.com/scm-manager/scm-manager/pull/2114))
- Encoding in global search ([#2116](https://github.com/scm-manager/scm-manager/pull/2116))
- Lazy loading in the source view ([#2120](https://github.com/scm-manager/scm-manager/pull/2120))
- Fix broken compare view due to missing export ([#2105](https://github.com/scm-manager/scm-manager/pull/2105))
- Keep search context for search results pagination ([#2115](https://github.com/scm-manager/scm-manager/pull/2115))
- Fixed wrong indentation of extraVolumes and extraVolumeMounts ([#2103](https://github.com/scm-manager/scm-manager/pull/2103))

### Changed
- Reduce the output from the realm trace log ([#2121](https://github.com/scm-manager/scm-manager/pull/2121))
- Enhance search result view by sorting translated categories and disable categories with no search results ([#2107](https://github.com/scm-manager/scm-manager/pull/2107))

## [2.38.1] - 2022-08-09
### Fixed
- Incorrect plugin dependency declaration breaks plugin builds

## [2.38.0] - 2022-08-08
### Added
- Cli commands to modify repository and namespace permissions ([#2090](https://github.com/scm-manager/scm-manager/pull/2090) and [#2093](https://github.com/scm-manager/scm-manager/pull/2093))
- Enable plugin management via CLI ([#2087](https://github.com/scm-manager/scm-manager/pull/2087)) & ([#2094](https://github.com/scm-manager/scm-manager/pull/2094))
- CLI commands for user/group permission management ([#2091](https://github.com/scm-manager/scm-manager/pull/2091))

### Fixed
- Make sure, that passwords are always stored encrypted ([#2085](https://github.com/scm-manager/scm-manager/pull/2085))
- Reduce code smells ([#2089](https://github.com/scm-manager/scm-manager/pull/2089))
- Fix paging for too large page numbers ([#2097](https://github.com/scm-manager/scm-manager/pull/2097))

### Changed
- Extend global search to enable context-sensitive search queries. ([#2102](https://github.com/scm-manager/scm-manager/pull/2102))

## [2.37.2] - 2022-07-13
### Fixed
- Unify password validation ([#2077](https://github.com/scm-manager/scm-manager/pull/2077))
- users cannot be edited ([#2084](https://github.com/scm-manager/scm-manager/pull/2084))
- Plugin wizard for foreign languages ([#2086](https://github.com/scm-manager/scm-manager/pull/2086))

## [2.37.1] - 2022-07-06
### Fixed
- Encrypt passwords stored via cli commands ([#2080](https://github.com/scm-manager/scm-manager/pull/2080))

## [2.37.0] - 2022-06-28
### Added
- Developers can omit the link prop of the CardColumnSmall component ([#2076](https://github.com/scm-manager/scm-manager/pull/2076))
- Mirror LFS files for git ([#2075](https://github.com/scm-manager/scm-manager/pull/2075))

### Fixed
- Validate lfs files after upload and discard corrupt files ([#2068](https://github.com/scm-manager/scm-manager/pull/2068))

### Changed
- clarify options for import with and without metadata ([#2074](https://github.com/scm-manager/scm-manager/pull/2074))

## [2.36.1] - 2022-06-21
### Fixed
- Zip CRC error on startup in Alpine image ([#2071](https://github.com/scm-manager/scm-manager/pull/2071))

## [2.36.0] - 2022-06-13
### Added
- Add api to overwrite content type resolver ([#2051](https://github.com/scm-manager/scm-manager/pull/2051))
- New diff view props for controlling hunk/line interaction ([#2047](https://github.com/scm-manager/scm-manager/pull/2047))

### Fixed
- Missing profile link in footer ([#2057](https://github.com/scm-manager/scm-manager/pull/2057))
- Remove branch validation on overview ([#2064](https://github.com/scm-manager/scm-manager/pull/2064))
- Do not close hg context for diff as string ([#2067](https://github.com/scm-manager/scm-manager/pull/2067))
- Calling search results without parameters ([#2056](https://github.com/scm-manager/scm-manager/pull/2056))
- Add wget to fix docker health check ([#2066](https://github.com/scm-manager/scm-manager/pull/2066))
- Remove underline under icon in buttons with class is-text ([#2065](https://github.com/scm-manager/scm-manager/pull/2065))
- Arrow icon of secondary navigation is inconsistent ([#2060](https://github.com/scm-manager/scm-manager/pull/2060))
- Remove superfluous user theme link properties ([#2058](https://github.com/scm-manager/scm-manager/pull/2058))

### Changed
- Feedback button should not mask warp menu in when SCM-Manager is used as a dogu in Cloudogu EcoSystem ([#2062](https://github.com/scm-manager/scm-manager/pull/2062))

## [2.35.0] - 2022-06-02
### Added
- Initialization step to install pre-defined plugin sets ([#2045](https://github.com/scm-manager/scm-manager/pull/2045))

### Fixed
- Diff command content ([#2036](https://github.com/scm-manager/scm-manager/pull/2036))
- Correct resource for branch leaf ([#2037](https://github.com/scm-manager/scm-manager/pull/2037))
- NPE on synchronous repository export ([#2040](https://github.com/scm-manager/scm-manager/pull/2040))
- character encoding for basic authentication ([#2038](https://github.com/scm-manager/scm-manager/pull/2038))
- Character encoding in diffs ([#2046](https://github.com/scm-manager/scm-manager/pull/2046))
- Resource bundle loading from plugins ([#2041](https://github.com/scm-manager/scm-manager/pull/2041))

### Changed
- Visually separate focused and hovered buttons in confirmation dialogues ([#2050](https://github.com/scm-manager/scm-manager/pull/2050))
- Migrate to Java 11 ([#1982](https://github.com/scm-manager/scm-manager/pull/1982))
- Notify user about results of manually executed health check ([#2044](https://github.com/scm-manager/scm-manager/pull/2044))
- Set order priority for repository information extensions ([#2041](https://github.com/scm-manager/scm-manager/pull/2041))

## [2.34.0] - 2022-05-13
### Added
- Docker images for linux/arm/v7 and linux/arm64 ([#2021](https://github.com/scm-manager/scm-manager/pull/2021))
- Extension point to render file actions in overflow menu ([#2015](https://github.com/scm-manager/scm-manager/pull/2015))

### Fixed
- Dependencies for ui-syntaxhighlighting and ui-text ([#2024](https://github.com/scm-manager/scm-manager/pull/2024))
- Jumping and broken tool tips ([#2026](https://github.com/scm-manager/scm-manager/pull/2026))
- Do not show feedback form for anonymous users
- Require password for internal user creation ([#2027](https://github.com/scm-manager/scm-manager/pull/2027))
- Improve visibility of focused line in source view ([#2031](https://github.com/scm-manager/scm-manager/pull/2031))
- Show confirmation modal before deleting key ([#2028](https://github.com/scm-manager/scm-manager/pull/2028))
- Validate repository types on creation ([#2019](https://github.com/scm-manager/scm-manager/pull/2019))
- Remove card-table action area ([#2016](https://github.com/scm-manager/scm-manager/pull/2016))
- Separate definition of padding to produce valid CSS ([#2023](https://github.com/scm-manager/scm-manager/pull/2023))
- Sort CLI commands alphabetically ([#2020](https://github.com/scm-manager/scm-manager/pull/2020))

## [2.33.0] - 2022-04-29
### Added
- Add cli support for repositories, users and groups ([#1987](https://github.com/scm-manager/scm-manager/pull/1987), [#1993](https://github.com/scm-manager/scm-manager/pull/1993))

### Fixed
- Table in code view breaks at certain screensizes ([#1995](https://github.com/scm-manager/scm-manager/pull/1995))
- Freezing browser if large files with syntax highlighting were displayed ([#2010](https://github.com/scm-manager/scm-manager/pull/2010))
- Multiline highlighting with line numbers ([#2010](https://github.com/scm-manager/scm-manager/pull/2010))
- White space next to button group in code action bar missing at screen size 769px ([#2006](https://github.com/scm-manager/scm-manager/pull/2006))
- Wrong label displayed above the action column in mobile mode for keys ([#1990](https://github.com/scm-manager/scm-manager/pull/1990))
- Do not process index types which no longer exist ([#1985](https://github.com/scm-manager/scm-manager/pull/1985))
- Incorrect log warning "could not set executable flag"
- Correct styling of syntax highlighter .section elements ([#1984](https://github.com/scm-manager/scm-manager/pull/1984))
- Improve organization of variables in scss ([#1976](https://github.com/scm-manager/scm-manager/pull/1976))
- Small tweaks in darkmode ([#1976](https://github.com/scm-manager/scm-manager/pull/1976))
- Escape parenthesis for entity names to fix routing ([#1998](https://github.com/scm-manager/scm-manager/pull/1998))
- Unnecessary re-render of markdown view ([#1999](https://github.com/scm-manager/scm-manager/pull/1999))
- Handling of illegal lfs pointers ([#1994](https://github.com/scm-manager/scm-manager/pull/1994))
- Open file handle prevent deleting a repository on Windows ([#2008](https://github.com/scm-manager/scm-manager/pull/2008))
- Make focused buttons clearly visible ([#2009](https://github.com/scm-manager/scm-manager/pull/2009))
- Label on focused button in high-contrast mode has enough contrast ([#2009](https://github.com/scm-manager/scm-manager/pull/2009))

### Changed
- Skip syntax highlighting on very large files ([#2010](https://github.com/scm-manager/scm-manager/pull/2010))
- Omit default port in protocol urls ([#2014](https://github.com/scm-manager/scm-manager/pull/2014))

## [2.32.2] - 2022-03-23
### Fixed
- Some plugin bundles are executed multiple times ([#1980](https://github.com/scm-manager/scm-manager/pull/1980))
- Remove plugin center login without url ([#1978](https://github.com/scm-manager/scm-manager/pull/1978))
- Do not ignore ui test results on Jenkins ([#1975](https://github.com/scm-manager/scm-manager/pull/1975))
- HG read support for evolve extension
- Hide plugin dependencies on uninstall ([#1977](https://github.com/scm-manager/scm-manager/pull/1977))
- Throw more specific exception if compared revisions are ambiguous ([#1974](https://github.com/scm-manager/scm-manager/pull/1974))
- Branch details invalidation ([#1973](https://github.com/scm-manager/scm-manager/pull/1973))

## [2.32.1] - 2022-03-10
### Fixed
- Create user via UI

## [2.32.0] - 2022-03-10
### Added
- Enhanced contributor api ([#1966](https://github.com/scm-manager/scm-manager/pull/1966))
- Add feedback button and form ([#1967](https://github.com/scm-manager/scm-manager/pull/1967))
- Introduce darkmode theme ([#1969](https://github.com/scm-manager/scm-manager/pull/1969))

### Fixed
- Branch decoding in overview ([#1963](https://github.com/scm-manager/scm-manager/pull/1963))
- Replace escaped characters in search syntax with html-encoded characters to display syntax and examples ([#1963](https://github.com/scm-manager/scm-manager/pull/1972))
- Triangle of notifications area points towards bell ([#1971](https://github.com/scm-manager/scm-manager/pull/1971))
- Do not redirect after simple data updates ([#1965](https://github.com/scm-manager/scm-manager/pull/1965))

### Changed
- Split frontend code by routes ([#1955](https://github.com/scm-manager/scm-manager/pull/1955))
- Enhance ui-elements to be more accessible on smaller screens ([#1950](https://github.com/scm-manager/scm-manager/pull/1950))
- Default theme is selected according to system defaults ([#1969](https://github.com/scm-manager/scm-manager/pull/1969))

## [2.31.1] - 2022-02-21
### Fixed
- Fix ClassNotFoundException on startup ([#1958](https://github.com/scm-manager/scm-manager/issues/1958))

## [2.31.0] - 2022-02-18
### Added
- Add secondary color gradations to styleguide ([#1944](https://github.com/scm-manager/scm-manager/pull/1944))
- Collapses folders in code view which only have a folder as their only child ([#1951](https://github.com/scm-manager/scm-manager/pull/1951))
- Add myCloudogu data processing link ([#1935](https://github.com/scm-manager/scm-manager/pull/1935))

### Fixed
- Adjust reload button on plugin actions ([#1946](https://github.com/scm-manager/scm-manager/pull/1946))
- Improve accessibility ([#1956](https://github.com/scm-manager/scm-manager/pull/1956))
- Do not fail on 502 during restart actions ([#1941](https://github.com/scm-manager/scm-manager/pull/1941))
- NPE for searchable types
- Fix missing css for migration wizard
- Markdown toggle covers source copy button ([#1939](https://github.com/scm-manager/scm-manager/pull/1939))

### Changed
- SCM-Manager icon in the plugin center has a transparent background ([#1936](https://github.com/scm-manager/scm-manager/pull/1936))
- Only display source code copy button on hover ([#1939](https://github.com/scm-manager/scm-manager/pull/1939))
- Fetch plugins without authentication, if prior authentication failed ([#1940](https://github.com/scm-manager/scm-manager/pull/1940))
- Docker base image to fix expat vulnerability ([#1948](https://github.com/scm-manager/scm-manager/pull/1948))
- Link directly to file with file history sources link ([#1945](https://github.com/scm-manager/scm-manager/pull/1945))
- improve tooltip accessibility ([#1954](https://github.com/scm-manager/scm-manager/pull/1954))

## [2.30.1] - 2022-01-26
### Fixed
- Make comparable null safe ([#1933](https://github.com/scm-manager/scm-manager/pull/1933))
- Password field in "Create User" dialog and other ([#1934](https://github.com/scm-manager/scm-manager/pull/1934))

## [2.30.0] - 2022-01-21
### Added
- initial focus, submission on pressing enter and fix trap focus for modals ([#1925](https://github.com/scm-manager/scm-manager/pull/1925))
- Add compare view to see changes between branches, tags and revisions ([#1920](https://github.com/scm-manager/scm-manager/pull/1920))
- Security notifications to inform the running instance about known security issues ([#1924](https://github.com/scm-manager/scm-manager/pull/1924))

### Fixed
- Set HEAD to correct branch in new git repositories ([#1929](https://github.com/scm-manager/scm-manager/pull/1929))
- Fix bugs in unit tests which occur on Windows only ([#1927](https://github.com/scm-manager/scm-manager/pull/1927))
- Encrypt myCloudogu refresh token on file system ([#1923](https://github.com/scm-manager/scm-manager/pull/1923))
- Autocompletion has sorted suggestions ([#1918](https://github.com/scm-manager/scm-manager/pull/1918))

### Changed
- Keeps the selection whether to add a user or a group in the repository permission dialog ([#1919](https://github.com/scm-manager/scm-manager/pull/1919))
- Autocompletion for namespaces ([#1916](https://github.com/scm-manager/scm-manager/pull/1916))

## [2.29.1] - 2022-01-17
### Fixed
- Path traversal vulnerability

## [2.29.0] - 2022-01-07
### Added
- CSS variables for plugins ([#1910](https://github.com/scm-manager/scm-manager/pull/1910))
- Add copy button for markdown code blocks ([#1902](https://github.com/scm-manager/scm-manager/pull/1902))
- Disable repository types via global config ([#1908](https://github.com/scm-manager/scm-manager/pull/1908))

### Fixed
- Minor issues in high contrast mode ([#1910](https://github.com/scm-manager/scm-manager/pull/1910))
- Set line ending used for license files to LF ([#1904](https://github.com/scm-manager/scm-manager/pull/1904))
- Add ability to render storyshots asynchronously ([#1906](https://github.com/scm-manager/scm-manager/pull/1906))

### Changed
- Change mono-font-stack to provide better ux on modern platforms by using later os-fonts. Reduces amount of similar but not equal fonts used.
- Make "not clickable" mode for breadcrumb ([#1907](https://github.com/scm-manager/scm-manager/pull/1907))

## [2.28.0] - 2021-12-22
### Added
- Fix ScmPathInfoStore injection ([#1889](https://github.com/scm-manager/scm-manager/pull/1889))
- Show additional branch details information ([#1888](https://github.com/scm-manager/scm-manager/pull/1888) and [#1893](https://github.com/scm-manager/scm-manager/pull/1893))
- Add move capabilities to modify command ([#1859](https://github.com/scm-manager/scm-manager/pull/1859))
- Adds compatibility for OCP and ICP ([#1870](https://github.com/scm-manager/scm-manager/pull/1870))
- Show additional information on branches overview ([#1876](https://github.com/scm-manager/scm-manager/pull/1876))

### Fixed
- High contrast mode flaws ([#1892](https://github.com/scm-manager/scm-manager/pull/1892))
- Cleanup html errors ([#1869](https://github.com/scm-manager/scm-manager/pull/1869))
- Fix extension point "main.route"
- Closing of repository while getting the latest commit asynchronously ([#1903](https://github.com/scm-manager/scm-manager/pull/1903))
- Search highlighting in jsx ([#1886](https://github.com/scm-manager/scm-manager/pull/1886))
- Syntax highlighting on non highlighted fields ([#1901](https://github.com/scm-manager/scm-manager/pull/1901))
- Ellipsis on new lines in code syntax highlighting ([#1901](https://github.com/scm-manager/scm-manager/pull/1901))
- Ellipsis on content start or end in non code fields ([#1901](https://github.com/scm-manager/scm-manager/pull/1901))
- Closing of file streams ([#1857](https://github.com/scm-manager/scm-manager/pull/1857) and [#1868](https://github.com/scm-manager/scm-manager/pull/1868))
- Exit of retry loop for deletion of files ([#1857](https://github.com/scm-manager/scm-manager/pull/1857)  and [#1868](https://github.com/scm-manager/scm-manager/pull/1868))
- Personal footer links ([#1882](https://github.com/scm-manager/scm-manager/pull/1882))
- Better error descriptions for gpg key import ([#1879](https://github.com/scm-manager/scm-manager/pull/1879))
- Highlight only queried fields ([#1887](https://github.com/scm-manager/scm-manager/pull/1887))
- Overview document title ([#1885](https://github.com/scm-manager/scm-manager/pull/1885))
- Do not display ellipsis if search result matches start or end of content ([#1896](https://github.com/scm-manager/scm-manager/pull/1896))
- Source view for files with colons ([#1881](https://github.com/scm-manager/scm-manager/pull/1881))

### Changed
- Improved quick search experience for screen readers ([#1898](https://github.com/scm-manager/scm-manager/pull/1898))
- Keep whole lines for code highlighting in search ([#1871](https://github.com/scm-manager/scm-manager/pull/1871))
- Use more accurate language detection for syntax highlighting ([#1891](https://github.com/scm-manager/scm-manager/pull/1891))
- Improve headings structure ([#1883](https://github.com/scm-manager/scm-manager/pull/1883))
- Enforce eslint to ensure accessible html ([#1878](https://github.com/scm-manager/scm-manager/pull/1878))

## [2.27.4] - 2021-12-17
### Changed
- Upgrade Logback to v1.2.9

## [2.27.3] - 2021-12-14
### Changed
- Upgrade Logback to v1.2.8 ([#1894](https://github.com/scm-manager/scm-manager/pull/1894))

## [2.27.2] - 2021-11-19
### Fixed
- Repository file search ([#1867](https://github.com/scm-manager/scm-manager/pull/1867))

## [2.27.1] - 2021-11-18
### Fixed
- Sporadic error "Automatic publicPath is not supported in this browser" (updated plugin-scripts to v1.2.2)

## [2.27.0] - 2021-11-16
### Added
- Add contact icon to repository overview ([#1855](https://github.com/scm-manager/scm-manager/pull/1855))
- Support for multipart form data to AdvancedHttpClient ([#1856](https://github.com/scm-manager/scm-manager/pull/1856))
- Lock and unlock command for SVN ([#1847](https://github.com/scm-manager/scm-manager/pull/1847))

### Fixed
- Disabled buttons are rendered as disabled buttons, again ([#1858](https://github.com/scm-manager/scm-manager/pull/1858))
- Color for available plugins, namespace and code header

### Changed
- Improve keyboard access by adding tab stops ([#1831](https://github.com/scm-manager/scm-manager/pull/1831))
- Improve aria lables for better screen reader support ([#1831](https://github.com/scm-manager/scm-manager/pull/1831))

## [2.26.1] - 2021-11-09
### Fixed
- Concurrent modification error in SVN modify command ([#1849](https://github.com/scm-manager/scm-manager/pull/1849))
- Fix endless loop on logout with slow connections

## [2.26.0] - 2021-11-04
### Added
- Viewer for pdf files ([#1843](https://github.com/scm-manager/scm-manager/pull/1843))
- Add alternative text to controls to allow screen readers to read them aloud ([#1840](https://github.com/scm-manager/scm-manager/pull/1840))
- File lock implementation for git (lfs) ([#1838](https://github.com/scm-manager/scm-manager/pull/1838))
- Experimental high contrast mode ([#1845](https://github.com/scm-manager/scm-manager/pull/1845))
- Read all errors with screen readers ([#1839](https://github.com/scm-manager/scm-manager/pull/1839))

### Fixed
- Fix <a> tags without hrefs ([#1841](https://github.com/scm-manager/scm-manager/pull/1841))
- Fix eslint errors and warnings ([#1841](https://github.com/scm-manager/scm-manager/pull/1841))
- Removed NODE_ENV from yarn serve command to be compatible with windows ([#1844](https://github.com/scm-manager/scm-manager/pull/1844))
- Initial mirror with no accepted branch ([#1842](https://github.com/scm-manager/scm-manager/pull/1842))

## [2.25.0] - 2021-10-21
### Added
- Extension points for repository overview ([#1828](https://github.com/scm-manager/scm-manager/pull/1828))
- Binder option to sort by priority ([#1828](https://github.com/scm-manager/scm-manager/pull/1828))

### Fixed
- Refetching of content on switching between source and md view ([#1823](https://github.com/scm-manager/scm-manager/pull/1823))
- Recursive git browse ([#1833](https://github.com/scm-manager/scm-manager/pull/1833))
- remove query keys when deleting individual entities ([#1832](https://github.com/scm-manager/scm-manager/pull/1832))
- Fix link templating for diff links ([#1834](https://github.com/scm-manager/scm-manager/pull/1834))
- Correct import in RepositoryGroupEntry ([#1825](https://github.com/scm-manager/scm-manager/pull/1825))

### Changed
- Always show SCM-Manager footer ([#1826](https://github.com/scm-manager/scm-manager/pull/1826))
- Resolved branch revision in source extension point ([#1803](https://github.com/scm-manager/scm-manager/pull/1803))
- The default branch of a repository cannot be deleted ([#1827](https://github.com/scm-manager/scm-manager/pull/1827))

## [2.24.0] - 2021-10-07
### Added
- Method to delete files recursively in modify command ([#1821](https://github.com/scm-manager/scm-manager/pull/1821))
- Internal api to determine email address for DisplayUser ([#1815](https://github.com/scm-manager/scm-manager/pull/1815))
- Set author for merge as DisplayUser ([#1815](https://github.com/scm-manager/scm-manager/pull/1815))
- Add method to delete whole configuration store ([#1814](https://github.com/scm-manager/scm-manager/pull/1814))
- Move DangerZone styling to ui-components (([#1814](https://github.com/scm-manager/scm-manager/pull/1814)))
- Extension points for source tree ([#1816](https://github.com/scm-manager/scm-manager/pull/1816))

### Fixed
- Bugs in svn and source tree for folders with a % in the name ([#1817](https://github.com/scm-manager/scm-manager/issues/1817) and [#1818](https://github.com/scm-manager/scm-manager/pull/1818))
- Edge cases in mirror command with cached workdirs ([#1812](https://github.com/scm-manager/scm-manager/pull/1812))
- Use correct logger for mercurial internal commands ([#1804](https://github.com/scm-manager/scm-manager/pull/1804))
- Deletion of repositories from search index ([#1813](https://github.com/scm-manager/scm-manager/pull/1813))

### Changed
- Bind mappers automatically to mapper implementations ([#1807](https://github.com/scm-manager/scm-manager/pull/1807))
- Clear external group cache on explicit user logout ([#1819](https://github.com/scm-manager/scm-manager/pull/1819))
- Replace styled-components with bulma helpers ([#1783](https://github.com/scm-manager/scm-manager/pull/1783))

## [2.23.0] - 2021-09-08
### Added
- Embedded repository in search result hit ([#1756](https://github.com/scm-manager/scm-manager/pull/1756))
- Base revision in modifications command to compute modifications between revisions ([#1761](https://github.com/scm-manager/scm-manager/pull/1761))
- Bounding box for plugin avatar ([#1749](https://github.com/scm-manager/scm-manager/pull/1749))
- Support for enum fields during indexing ([#1792](https://github.com/scm-manager/scm-manager/pull/1792))
- Central Work Queue for coordinating long-running tasks ([#1781](https://github.com/scm-manager/scm-manager/pull/1781))
- Api to modify multiple indices at once ([#1781](https://github.com/scm-manager/scm-manager/pull/1781))
- Event which is fired whenever the default branch of a repository changes ([#1763](https://github.com/scm-manager/scm-manager/pull/1763))
- Proxy support for pull, push and mirror commands ([#1773](https://github.com/scm-manager/scm-manager/pull/1773))
- Option for local proxy configuration to mirror command ([#1773](https://github.com/scm-manager/scm-manager/pull/1773))
- Show repository avatar in quick search ([#1759](https://github.com/scm-manager/scm-manager/issues/1759))
- Add additional help to quick search and an advanced search documentation page ([#1757](https://github.com/scm-manager/scm-manager/pull/1757)
- Support for different types of analyzer per field ([#1755](https://github.com/scm-manager/scm-manager/pull/1755))

### Fixed
- Missing encoding of useBranch api ([#1798](https://github.com/scm-manager/scm-manager/pull/1798))
- Post 'post receive repository hook event' after import ([#1754](https://github.com/scm-manager/scm-manager/pull/1754))
- Preserve request method on force base url ([#1771](https://github.com/scm-manager/scm-manager/issues/1771) and [#1778](https://github.com/scm-manager/scm-manager/pull/1778))
- Search queries containing hypens ([#1743](https://github.com/scm-manager/scm-manager/issues/1743) and [#1753](https://github.com/scm-manager/scm-manager/pull/1753))
- Proxy authentication ([#1773](https://github.com/scm-manager/scm-manager/pull/1773))
- Fix disabled local proxy configuration being used over global config ([#1780](https://github.com/scm-manager/scm-manager/pull/1780))
- Fix HalRepresentationWithEmbedded type ([#1793](https://github.com/scm-manager/scm-manager/pull/1793))
- Error message for parse error on search result page ([#1768](https://github.com/scm-manager/scm-manager/pull/1768))
- Remove deletion of empty modalRoot node to allow a different modal to continue to exist ([#1779](https://github.com/scm-manager/scm-manager/pull/1779))
- Broken login page if login info response could not be parsed ([#1791](https://github.com/scm-manager/scm-manager/issues/1791) and [#1795](https://github.com/scm-manager/scm-manager/pull/1795))
- Submission of empty search queries ([#1769](https://github.com/scm-manager/scm-manager/pull/1769))
- Too heavy logging of SchemeBasedWebTokenGenerator ([#1772](https://github.com/scm-manager/scm-manager/issues/1772) and [#1777](https://github.com/scm-manager/scm-manager/pull/1777))
- Prevent multiple working copy pools ([#1797](https://github.com/scm-manager/scm-manager/issues/1797))
- Repository viewer filename with hash ([#1766](https://github.com/scm-manager/scm-manager/issues/1766) and [#1776](https://github.com/scm-manager/scm-manager/pull/1776))
- Fetch clone modal data on first opening ([#1784](https://github.com/scm-manager/scm-manager/pull/1784))
- Branch selector display revision if selected instead of first branch ([#1767](https://github.com/scm-manager/scm-manager/pull/1767))
- Show empty files instead of endless loading spinner ([#1762](https://github.com/scm-manager/scm-manager/pull/1762))
- redundant git repo closing in some commands ([#1789](https://github.com/scm-manager/scm-manager/pull/1789))
- Keep quick search input on page reload ([#1788](https://github.com/scm-manager/scm-manager/pull/1788))

### Changed
- One index per type instead of one index for all types ([#1781](https://github.com/scm-manager/scm-manager/pull/1781))
- Use central work queue for all indexing tasks ([#1781](https://github.com/scm-manager/scm-manager/pull/1781))
- Keep search result type if searched from result page ([#1764](https://github.com/scm-manager/scm-manager/pull/1764))
- Expose content type resolver api to plugins ([#1752](https://github.com/scm-manager/scm-manager/pull/1752))
- Improve Search API ([#1755](https://github.com/scm-manager/scm-manager/pull/1755))

## [2.22.0] - 2021-07-30
### Added
- Add users and groups to default search index ([#1738](https://github.com/scm-manager/scm-manager/pull/1738))
- Add dedicated search page with more details and different types ([#1738](https://github.com/scm-manager/scm-manager/pull/1738))

### Changed
- Remove repository shortlinks ([#1720](https://github.com/scm-manager/scm-manager/pull/1720))
- The simple workdir cache has a maximum size, an lru semantic and blocks on parallel requests ([#1735](https://github.com/scm-manager/scm-manager/pull/1735))
- Add username/password authentication to push command ([#1734](https://github.com/scm-manager/scm-manager/pull/1734))
- Decrease log level of DefaultAdministrationContext from info to debug
- Logo for small header
- Redesign repository overview ([#1740](https://github.com/scm-manager/scm-manager/pull/1740))

### Fixed
- Make MarkdownView backwards-compatible with edge-cases ([#1737](https://github.com/scm-manager/scm-manager/pull/1737))
- Handle rejected master branch on initial mirror synchronization correctly ([#1747](https://github.com/scm-manager/scm-manager/pull/1747))
- Fix file search on branches with "/" ([#1748](https://github.com/scm-manager/scm-manager/pull/1748))
- Fix overflow of quick search results with long repository names ([#1739](https://github.com/scm-manager/scm-manager/pull/1739))
- Fix login extension point ([#1741](https://github.com/scm-manager/scm-manager/pull/1741))
- Fix file detection on hg fileview command ([#1746](https://github.com/scm-manager/scm-manager/pull/1746))
- Fix svn mirror update if first initialization failed before ([#1745](https://github.com/scm-manager/scm-manager/pull/1745))

## [2.21.0] - 2021-07-21
### Added
- API to index and query objects ([#1727](https://github.com/scm-manager/scm-manager/pull/1727))
- Quick search for repositories ([#1727](https://github.com/scm-manager/scm-manager/pull/1727))
- Additional color scheme to prepare a high contrast mode ([#1730](https://github.com/scm-manager/scm-manager/pull/1731))
- Create files in empty non-initiated repositories ([#1717](https://github.com/scm-manager/scm-manager/pull/1717))
- Prepare plugin center to show cloudogu plugins ([#1709](https://github.com/scm-manager/scm-manager/pull/1709))
- Option to diable automatic refresh for diff view on window focus change ([#1714](https://github.com/scm-manager/scm-manager/pull/1714))
- Building forms documentation with react-hook-form ([#1704](https://github.com/scm-manager/scm-manager/pull/1704))

### Fixed
- Missing update if content of diff changes ([#1714](https://github.com/scm-manager/scm-manager/pull/1714))
- Contributors table in changeset detail view ([#1718](https://github.com/scm-manager/scm-manager/pull/1718))
- Prevent overwrite read-only gpg keys ([#1713](https://github.com/scm-manager/scm-manager/pull/1713))
- Language tag of top level html element ([#1705](https://github.com/scm-manager/scm-manager/pull/1705))

### Changed
- Show last modified date on repository overview ([#1715](https://github.com/scm-manager/scm-manager/pull/1715))
- Redesign SCM-Manager header ([#1721](https://github.com/scm-manager/scm-manager/pull/1721))
- Initial admin user has to be created on first startup ([#1707](https://github.com/scm-manager/scm-manager/pull/1707), [#1722](https://github.com/scm-manager/scm-manager/pull/1722))

## [2.20.1] - 2022-01-18
### Fixed
- Path traversal vulnerability (backport from 2.29.1)

## [2.20.0] - 2021-06-16
### Added
- Support basic authentication with access token ([#1694](https://github.com/scm-manager/scm-manager/pull/1694))
- Form elements that support react-hook-form can now be made read-only ([#1696](https://github.com/scm-manager/scm-manager/pull/1696))

### Fixed
- Post receive hook events after mirror update for git ([#1703](https://github.com/scm-manager/scm-manager/pull/1703))
- Added option to increase LFS authorization token timeout ([#1697](https://github.com/scm-manager/scm-manager/pull/1697))
- Uniform rendering of tooltips for repository badges ([#1698](https://github.com/scm-manager/scm-manager/pull/1698))
- Clear related caches if gpg key was added or deleted ([#1701](https://github.com/scm-manager/scm-manager/pull/1701))
- Redirect after single tag was deleted ([#1700](https://github.com/scm-manager/scm-manager/pull/1700))

## [2.19.1] - 2021-06-09
### Fixed
- Add handling when duplicated branch part cannot be created ([#1692](https://github.com/scm-manager/scm-manager/pull/1692))
- Add log for error in git mirror ([#1689](https://github.com/scm-manager/scm-manager/pull/1689))
- Tag position for repository ([#1691](https://github.com/scm-manager/scm-manager/pull/1691))
- Options requests returning internal server errors ([#1685](https://github.com/scm-manager/scm-manager/issues/1685), [#1688](https://github.com/scm-manager/scm-manager/pull/1688))
- Harmonize FileInput component with styleguide ([#1693](https://github.com/scm-manager/scm-manager/pull/1693))

## [2.19.0] - 2021-06-04
### Added
- Extension Point for repository creators ([#1657](https://github.com/scm-manager/scm-manager/pull/1657))
- Add trust manager provider ([#1654](https://github.com/scm-manager/scm-manager/pull/1654))
- Implement api for extension point typings ([#1638](https://github.com/scm-manager/scm-manager/pull/1638))
- Add mirror command and extension points ([#1683](https://github.com/scm-manager/scm-manager/pull/1683))
- Add support for react-hook-form ([#1656](https://github.com/scm-manager/scm-manager/pull/1656))
- Implement Subversion mirror command ([#1660](https://github.com/scm-manager/scm-manager/pull/1660))
- Notifications for health checks ([#1664](https://github.com/scm-manager/scm-manager/pull/1664))

### Fixed
- Show source code controls even if a file is not present ([#1680](https://github.com/scm-manager/scm-manager/pull/1680))
- Fix annotate overflow and doubled spacing in code views ([#1678](https://github.com/scm-manager/scm-manager/pull/1678))
- SSE for notifications behind nginx reverse proxy ([#1650](https://github.com/scm-manager/scm-manager/pull/1650))
- Use correct syntax for config routes ([#1652](https://github.com/scm-manager/scm-manager/pull/1652))
- Fix administration page error for empty release feed url ([#1667](https://github.com/scm-manager/scm-manager/pull/1667))
- Remove duplicated notification endpoints in openapi ([#1677](https://github.com/scm-manager/scm-manager/pull/1677))

### Changed
- Inject custom trust manager to git https connections ([#1675](https://github.com/scm-manager/scm-manager/pull/1675))

## [2.18.0] - 2021-05-05
### Added
- Patch endpoint for global configuration ([#1629](https://github.com/scm-manager/scm-manager/pull/1629))
- Show hg binary verification error messages ([#1637](https://github.com/scm-manager/scm-manager/pull/1637))
- Add global notifications ([#1646](https://github.com/scm-manager/scm-manager/pull/1646))
- Add extension point for custom link protocol renderers in markdown ([#1639](https://github.com/scm-manager/scm-manager/pull/1639))

### Fixed
- Show correct default branch for repository if not configured yet ([#1643](https://github.com/scm-manager/scm-manager/pull/1643))
- Flickering form elements on repository initialization ([#1644](https://github.com/scm-manager/scm-manager/issues/1644) and [#1645](https://github.com/scm-manager/scm-manager/issues/1645))
- Messages from post commit hooks for git ([#1647](https://github.com/scm-manager/scm-manager/pull/1647))
- External links and anchor links are now correctly rendered in markdown even if no base path is present ([#1639](https://github.com/scm-manager/scm-manager/pull/1639))

### Changed
- Show only relevant information on repository information page ([#1636](https://github.com/scm-manager/scm-manager/pull/1636))

## [2.17.1] - 2021-04-26
### Fixed
- Deserialization of embedded values in HAL objects ([#1630](https://github.com/scm-manager/scm-manager/pull/1630))
- Increase Code font size ([#1631](https://github.com/scm-manager/scm-manager/pull/1631))

## [2.17.0] - 2021-04-22
### Added
- Frontend for, and enhancement of health checks ([#1621](https://github.com/scm-manager/scm-manager/pull/1621))
- New extension points for custom repository avatars ([#1614](https://github.com/scm-manager/scm-manager/pull/1614))
- Trigger mercurial auto config via ui ([#1620](https://github.com/scm-manager/scm-manager/pull/1620))

### Changed
- Make remark compatible with rehype plugins to sanitize html content ([#1622](https://github.com/scm-manager/scm-manager/pull/1622))

### Fixed
- Validation in "Add Entry" components for configuration table ([#1625](https://github.com/scm-manager/scm-manager/pull/1625))
- Add header to toplevel error boundary ([#1613](https://github.com/scm-manager/scm-manager/pull/1613))
- Correct positioning and coloring of button groups in modals ([#1612](https://github.com/scm-manager/scm-manager/pull/1612))
- Fix limit with negativ integer for searchUtil ([#1627](https://github.com/scm-manager/scm-manager/pull/1627))
- Fix compatibility with old redux api ([#1618](https://github.com/scm-manager/scm-manager/pull/1618))
- Missing graphviz in docker image ([#1623](https://github.com/scm-manager/scm-manager/pull/1623))
- Validation for namespaces on rename with UTF-8 characters ([#1611](https://github.com/scm-manager/scm-manager/pull/1611))

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
- Language detection of files with interpreter parameters e.g.: `#!/usr/bin/make -f` ([#1450](https://github.com/scm-manager/scm-manager/issues/1450))
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
- Tracing api ([#1393](https://github.com/scm-manager/scm-manager/pull/1393))
- Automatic user converter for external users ([#1380](https://github.com/scm-manager/scm-manager/pull/1380))
- Create _authenticated group on setup ([#1396](https://github.com/scm-manager/scm-manager/pull/1396))
- The name of the initial git branch can be configured and is set to `main` by default ([#1399](https://github.com/scm-manager/scm-manager/pull/1399))

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
- New extension point `changeset.description.tokens` to "enrich" commit messages ([#1231](https://github.com/scm-manager/scm-manager/pull/1231))
- Restart service after rpm or deb package upgrade

### Changed
- Checkboxes can now be 'indeterminate' ([#1215](https://github.com/scm-manager/scm-manager/pull/1215))
- The old frontend extension point `changeset.description` is deprecated and should be replaced with `changeset.description.tokens` ([#1231](https://github.com/scm-manager/scm-manager/pull/1231))
- Required plugins will be updated, too, when a plugin is updated ([#1233](https://github.com/scm-manager/scm-manager/pull/1233))

### Fixed
- Fixed installation of debian packages on distros without preinstalled `at` ([#1216](https://github.com/scm-manager/scm-manager/issues/1216) and [#1217](https://github.com/scm-manager/scm-manager/pull/1217))
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
- Fixes configuration of jetty listener address with system property `jetty.host` ([#1173](https://github.com/scm-manager/scm-manager/pull/1173), [#1174](https://github.com/scm-manager/scm-manager/pull/1174))
- Fixes loading plugin bundles with context path `/` ([#1182](https://github.com/scm-manager/scm-manager/pull/1182/files), [#1181](https://github.com/scm-manager/scm-manager/issues/1181))
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
- Making WebElements (Servlet or Filter) optional by using the `@Requires` annotation ([#1101](https://github.com/scm-manager/scm-manager/pull/1101))
- Add class to manually validate rest data transfer objects with javax validation annotations ([#1114](https://github.com/scm-manager/scm-manager/pull/1114))
- Missing stories for ui-components ([#1140](https://github.com/scm-manager/scm-manager/pull/1140))

### Changed
- Removed the `requires` attribute on the `@Extension` annotation and instead create a new `@Requires` annotation ([#1097](https://github.com/scm-manager/scm-manager/pull/1097))
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

[2.0.0-rc1]: https://scm-manager.org/download/2.0.0-rc1
[2.0.0-rc2]: https://scm-manager.org/download/2.0.0-rc2
[2.0.0-rc3]: https://scm-manager.org/download/2.0.0-rc3
[2.0.0-rc4]: https://scm-manager.org/download/2.0.0-rc4
[2.0.0-rc5]: https://scm-manager.org/download/2.0.0-rc5
[2.0.0-rc6]: https://scm-manager.org/download/2.0.0-rc6
[2.0.0-rc7]: https://scm-manager.org/download/2.0.0-rc7
[2.0.0-rc8]: https://scm-manager.org/download/2.0.0-rc8
[2.0.0]: https://scm-manager.org/download/2.0.0
[2.1.0]: https://scm-manager.org/download/2.1.0
[2.1.1]: https://scm-manager.org/download/2.1.1
[2.2.0]: https://scm-manager.org/download/2.2.0
[2.3.0]: https://scm-manager.org/download/2.3.0
[2.3.1]: https://scm-manager.org/download/2.3.1
[2.4.0]: https://scm-manager.org/download/2.4.0
[2.4.1]: https://scm-manager.org/download/2.4.1
[2.5.0]: https://scm-manager.org/download/2.5.0
[2.6.0]: https://scm-manager.org/download/2.6.0
[2.6.1]: https://scm-manager.org/download/2.6.1
[2.6.2]: https://scm-manager.org/download/2.6.2
[2.6.3]: https://scm-manager.org/download/2.6.3
[2.7.0]: https://scm-manager.org/download/2.7.0
[2.7.1]: https://scm-manager.org/download/2.7.1
[2.8.0]: https://scm-manager.org/download/2.8.0
[2.9.0]: https://scm-manager.org/download/2.9.0
[2.9.1]: https://scm-manager.org/download/2.9.1
[2.10.0]: https://scm-manager.org/download/2.10.0
[2.10.1]: https://scm-manager.org/download/2.10.1
[2.11.0]: https://scm-manager.org/download/2.11.0
[2.11.1]: https://scm-manager.org/download/2.11.1
[2.12.0]: https://scm-manager.org/download/2.12.0
[2.13.0]: https://scm-manager.org/download/2.13.0
[2.14.0]: https://scm-manager.org/download/2.14.0
[2.14.1]: https://scm-manager.org/download/2.14.1
[2.15.0]: https://scm-manager.org/download/2.15.0
[2.15.1]: https://scm-manager.org/download/2.15.1
[2.16.0]: https://scm-manager.org/download/2.16.0
[2.17.0]: https://scm-manager.org/download/2.17.0
[2.17.1]: https://scm-manager.org/download/2.17.1
[2.18.0]: https://scm-manager.org/download/2.18.0
[2.19.0]: https://scm-manager.org/download/2.19.0
[2.19.1]: https://scm-manager.org/download/2.19.1
[2.20.0]: https://scm-manager.org/download/2.20.0
[2.21.0]: https://scm-manager.org/download/2.21.0
[2.22.0]: https://scm-manager.org/download/2.22.0
[2.23.0]: https://scm-manager.org/download/2.23.0
[2.24.0]: https://scm-manager.org/download/2.24.0
[2.25.0]: https://scm-manager.org/download/2.25.0
[2.26.0]: https://scm-manager.org/download/2.26.0
[2.26.1]: https://scm-manager.org/download/2.26.1
[2.27.0]: https://scm-manager.org/download/2.27.0
[2.27.1]: https://scm-manager.org/download/2.27.1
[2.27.2]: https://scm-manager.org/download/2.27.2
[2.27.3]: https://scm-manager.org/download/2.27.3
[2.27.4]: https://scm-manager.org/download/2.27.4
[2.28.0]: https://scm-manager.org/download/2.28.0
[2.29.0]: https://scm-manager.org/download/2.29.0
[2.29.1]: https://scm-manager.org/download/2.29.1
[2.30.0]: https://scm-manager.org/download/2.30.0
[2.30.1]: https://scm-manager.org/download/2.30.1
[2.31.0]: https://scm-manager.org/download/2.31.0
[2.31.1]: https://scm-manager.org/download/2.31.1
[2.32.0]: https://scm-manager.org/download/2.32.0
[2.32.1]: https://scm-manager.org/download/2.32.1
[2.32.2]: https://scm-manager.org/download/2.32.2
[2.33.0]: https://scm-manager.org/download/2.33.0
[2.34.0]: https://scm-manager.org/download/2.34.0
[2.35.0]: https://scm-manager.org/download/2.35.0
[2.36.0]: https://scm-manager.org/download/2.36.0
[2.36.1]: https://scm-manager.org/download/2.36.1
[2.37.0]: https://scm-manager.org/download/2.37.0
[2.37.1]: https://scm-manager.org/download/2.37.1
[2.37.2]: https://scm-manager.org/download/2.37.2
[2.38.0]: https://scm-manager.org/download/2.38.0
[2.38.1]: https://scm-manager.org/download/2.38.1
[2.39.0]: https://scm-manager.org/download/2.39.0
[2.39.1]: https://scm-manager.org/download/2.39.1
[2.40.0]: https://scm-manager.org/download/2.40.0
[2.40.1]: https://scm-manager.org/download/2.40.1
[2.41.0]: https://scm-manager.org/download/2.41.0
[2.41.1]: https://scm-manager.org/download/2.41.1
[2.42.0]: https://scm-manager.org/download/2.42.0
[2.42.1]: https://scm-manager.org/download/2.42.1
[2.42.2]: https://scm-manager.org/download/2.42.2
[2.42.3]: https://scm-manager.org/download/2.42.3
[2.43.0]: https://scm-manager.org/download/2.43.0
[2.43.1]: https://scm-manager.org/download/2.43.1
[2.44.0]: https://scm-manager.org/download/2.44.0
[2.44.1]: https://scm-manager.org/download/2.44.1
[2.44.2]: https://scm-manager.org/download/2.44.2
[2.44.3]: https://scm-manager.org/download/2.44.3
[2.45.1]: https://scm-manager.org/download/2.45.1
[2.46.0]: https://scm-manager.org/download/2.46.0
[2.46.1]: https://scm-manager.org/download/2.46.1
[2.47.0]: https://scm-manager.org/download/2.47.0
[2.48.0]: https://scm-manager.org/download/2.48.0
[2.48.1]: https://scm-manager.org/download/2.48.1
[2.48.2]: https://scm-manager.org/download/2.48.2
[3.0.0]: https://scm-manager.org/download/3.0.0
[3.0.1]: https://scm-manager.org/download/3.0.1
[3.0.2]: https://scm-manager.org/download/3.0.2
