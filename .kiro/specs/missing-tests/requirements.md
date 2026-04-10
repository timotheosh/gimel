# Requirements Document

## Introduction

The gimel project is a flat-file CMS written in Clojure. Currently only `gimel.markdown` has test coverage. This feature adds comprehensive test coverage for the remaining namespaces: `gimel.os`, `gimel.config-spec`, `gimel.config`, `gimel.handler`, `gimel.highlight`, `gimel.metadata`, `gimel.static-files`, `gimel.database`, `gimel.partial-pages`, `gimel.sitemap`, `gimel.templates`, `gimel.static-pages`, and `gimel.api.core`. All new tests must pass alongside the existing markdown tests.

## Glossary

- **Test_Suite**: The collection of all Clojure test files under `test/gimel/`
- **SUT** (System Under Test): The namespace being tested in a given test file
- **Config_Spec**: The `gimel.config-spec` namespace that validates configuration maps using `clojure.spec`
- **Config**: The `gimel.config` namespace that loads, stores, and retrieves configuration values
- **OS_Utils**: The `gimel.os` namespace providing path manipulation utilities
- **Static_Files**: The `gimel.static-files` namespace providing file copy and checksum utilities
- **Handler**: The `gimel.handler` namespace providing Ring middleware
- **Highlight**: The `gimel.highlight` namespace providing syntax highlighting via Enlive
- **Metadata**: The `gimel.metadata` namespace providing navigation menu generation
- **Database**: The `gimel.database` namespace providing SQLite persistence via next.jdbc
- **Partial_Pages**: The `gimel.partial-pages` namespace processing HTML partial pages
- **Sitemap**: The `gimel.sitemap` namespace generating XML sitemaps
- **API**: The `gimel.api.core` namespace providing the Ring/Reitit HTTP API
- **Round_Trip**: A property where applying an operation and its inverse returns the original value

---

## Requirements

### Requirement 1: OS Utility Tests

**User Story:** As a developer, I want tests for `gimel.os`, so that path manipulation functions are verified to be correct and robust against edge-case inputs.

#### Acceptance Criteria

1. THE Test_Suite SHALL verify that `path-append` joins path segments with a single `/` separator
2. THE Test_Suite SHALL verify that `path-append` collapses multiple consecutive slashes into a single `/`
3. THE Test_Suite SHALL verify that `dirname` returns the directory portion of a path (everything before the last `/`)
4. THE Test_Suite SHALL verify that `basename` returns the filename portion of a path (everything after the last `/`)
5. FOR ALL valid path strings, `path-append` SHALL produce a result containing no consecutive `/` characters (invariant property)
6. WHEN `path-append` is called with one or more empty string arguments, THE OS_Utils SHALL skip the empty segments and return a path with no leading, trailing, or consecutive `/` characters
7. WHEN `path-append` is called with one or more `nil` arguments, THE OS_Utils SHALL skip the nil arguments and return a path constructed from the remaining non-nil segments
8. WHEN `dirname` is called with a path that contains no `/` character, THE OS_Utils SHALL return `"."` rather than throwing an exception
9. WHEN `basename` is called with a path that contains no `/` character, THE OS_Utils SHALL return the path itself rather than throwing an exception
10. WHEN `dirname` is called with an empty string, THE OS_Utils SHALL return `"."` without throwing an exception
11. WHEN `basename` is called with an empty string, THE OS_Utils SHALL return `""` without throwing an exception

---

### Requirement 2: Config Spec Validation Tests

**User Story:** As a developer, I want tests for `gimel.config-spec`, so that configuration validation logic is verified.

#### Acceptance Criteria

1. WHEN a fully valid configuration map is provided, THE Config_Spec SHALL return the configuration map unchanged
2. WHEN a configuration map is missing a required key, THE Config_Spec SHALL throw an `ex-info` exception
3. WHEN the `:web-url` value does not match the URL pattern, THE Config_Spec SHALL throw an `ex-info` exception
4. WHEN the `:port` value is not a positive integer, THE Config_Spec SHALL throw an `ex-info` exception
5. THE Test_Suite SHALL verify that `check-config` accepts a valid config and rejects an invalid config

---

### Requirement 3: Config Loading Tests

**User Story:** As a developer, I want tests for `gimel.config`, so that configuration loading and accessor functions are verified.

#### Acceptance Criteria

1. WHEN `expand-home` is called with a path starting with `~`, THE Config SHALL replace `~` with the value of the `user.home` system property
2. WHEN `expand-home` is called with a path not starting with `~`, THE Config SHALL return the path unchanged
3. WHEN `load-config` is called with a non-existent path, THE Config SHALL load the bundled default config from `resources/config/gimel.edn`
4. WHEN `get-config` is called after `load-config`, THE Config SHALL return a non-empty map
5. WHEN `get-config` is called before `load-config` has populated the atom, THE Config SHALL throw an `ex-info` exception

---

### Requirement 4: Handler Middleware Tests

**User Story:** As a developer, I want tests for `gimel.handler`, so that the Ring middleware is verified to set correct response headers.

#### Acceptance Criteria

1. WHEN a request URI ends with `.html`, THE Handler SHALL set the `Content-Type` response header to `text/html`
2. WHEN a request URI does not end with `.html`, THE Handler SHALL leave the `Content-Type` response header unchanged
3. THE Test_Suite SHALL verify `wrap-html-content-type` passes the request through to the wrapped handler

---

### Requirement 5: Highlight Tests

**User Story:** As a developer, I want tests for `gimel.highlight`, so that syntax highlighting injection is verified.

#### Acceptance Criteria

1. WHEN `highlight-code-blocks` is called with an HTML string containing a `<pre><code class="clojure">` block, THE Highlight SHALL return HTML where the code class is `language-clojure line-numbers`
2. WHEN `highlight-code-blocks` is called with an HTML string containing no `<pre><code>` blocks, THE Highlight SHALL return the HTML string unchanged

---

### Requirement 6: Metadata Tests

**User Story:** As a developer, I want tests for `gimel.metadata`, so that navigation menu generation is verified once implemented.

#### Acceptance Criteria

1. THE Test_Suite SHALL include a placeholder test for `generate-navmenu` that documents the function exists in the `gimel.metadata` namespace
2. WHEN `generate-navmenu` is called, THE Metadata SHALL return a value (non-nil result once implemented)

---

### Requirement 7: Static Files Tests

**User Story:** As a developer, I want tests for `gimel.static-files`, so that file checksum and copy utilities are verified.

#### Acceptance Criteria

1. WHEN `checksum` is called on a file, THE Static_Files SHALL return a 64-character lowercase hexadecimal SHA-256 string
2. WHEN `checksum` is called on the same file twice, THE Static_Files SHALL return the same value both times (idempotence property)
3. WHEN `ensure-path` is called with a path whose parent directories do not exist, THE Static_Files SHALL create the necessary parent directories
4. WHEN `copy-files` is called with a source directory and target directory, THE Static_Files SHALL copy only files matching the given extensions

---

### Requirement 8: Database Tests

**User Story:** As a developer, I want tests for `gimel.database`, so that SQLite persistence functions are verified.

#### Acceptance Criteria

1. WHEN `match-valid-table-or-column-name?` is called with a string of alphanumeric characters and underscores, THE Database SHALL return `true`
2. WHEN `match-valid-table-or-column-name?` is called with a string containing SQL injection characters (e.g. `; DROP TABLE`), THE Database SHALL throw an `ex-info` exception
3. WHEN `insert-page` is called twice with the same URL, THE Database SHALL return the same page id both times (idempotence property)
4. WHEN `get-page-id` is called for a URL that has been inserted, THE Database SHALL return the correct integer id
5. WHEN `get-page-id` is called for a URL that has not been inserted, THE Database SHALL return `nil`
6. WHEN `insert-data` is called with a metadata map, THE Database SHALL insert the page and all associated metadata rows without error
7. THE Test_Suite SHALL use an isolated in-memory or temporary SQLite database for all database tests so that tests do not affect production data

---

### Requirement 9: Partial Pages Tests

**User Story:** As a developer, I want tests for `gimel.partial-pages`, so that HTML partial page parsing is verified.

#### Acceptance Criteria

1. WHEN `parse-header` is called with a valid YAML string, THE Partial_Pages SHALL return a map containing the parsed key-value pairs
2. WHEN `parse-header` is called with an invalid YAML string, THE Partial_Pages SHALL return a map with `:error` key set to `"NO META DATA"`
3. WHEN `partial-pages` is called with a map containing a `/navbar.html` key, THE Partial_Pages SHALL exclude that entry from the returned map

---

### Requirement 10: Sitemap Tests

**User Story:** As a developer, I want tests for `gimel.sitemap`, so that URL generation for the sitemap is verified.

#### Acceptance Criteria

1. WHEN `path->url` is called with a file whose path ends in `.md`, THE Sitemap SHALL return a URL ending in `.html`
2. WHEN `path->url` is called with a file whose path ends in `.org`, THE Sitemap SHALL return a URL ending in `.html`
3. WHEN `file-data` is called with a file, THE Sitemap SHALL return a map containing `:loc`, `:lastmod`, and `:changefreq` keys
4. THE Test_Suite SHALL verify that the `:loc` value produced by `file-data` begins with `https://`

---

### Requirement 11: API Tests

**User Story:** As a developer, I want tests for `gimel.api.core`, so that the HTTP API routing and validation logic is verified.

#### Acceptance Criteria

1. WHEN a `GET /api/export` request is received, THE API SHALL respond with HTTP status `200`
2. WHEN a `POST /api/export-custom` request is received with an invalid JSON body, THE API SHALL respond with HTTP status `400`
3. WHEN a request is made to an undefined API route, THE API SHALL respond with HTTP status `404` and a JSON body containing an `"error"` key
4. THE Test_Suite SHALL verify `valid-data?` returns a falsy result when required directory paths are missing or invalid

---

### Requirement 12: All Tests Pass

**User Story:** As a developer, I want all tests in the Test_Suite to pass, so that the codebase is in a consistently verified state.

#### Acceptance Criteria

1. WHEN `lein test` is executed, THE Test_Suite SHALL report zero failures and zero errors
2. THE Test_Suite SHALL not leave any temporary files or test databases on disk after test execution completes
3. WHEN any single test fails, THE Test_Suite SHALL report the failing test name and the expected vs actual values
