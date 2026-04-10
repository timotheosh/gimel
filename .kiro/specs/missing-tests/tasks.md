# Implementation Plan: Missing Tests

## Overview

Add comprehensive test coverage to the gimel Clojure CMS. This involves adding the `test.check` dependency, fixing three bugs in `gimel.os`, and creating one test file per untested namespace. All tests must pass alongside the existing `markdown_test`.

## Tasks

- [x] 1. Add `test.check` dependency to `project.clj`
  - Add `[org.clojure/test.check "1.1.1"]` to the `:dependencies` vector in `project.clj`
  - _Requirements: 1.5, 7.2, 8.3_

- [x] 2. Fix `gimel.os` to handle nil/empty inputs
  - Rewrite `path-append` to filter out nil and empty-string segments before joining, then collapse consecutive slashes
  - Rewrite `dirname` to return `"."` when no `/` is found instead of throwing `NullPointerException`
  - Rewrite `basename` to return the path itself when no `/` is found instead of throwing `NullPointerException`
  - _Requirements: 1.6, 1.7, 1.8, 1.9, 1.10, 1.11_

- [x] 3. Create `test/gimel/os_test.clj`
  - [x] 3.1 Write unit tests for `path-append`, `dirname`, and `basename`
    - Test normal join, consecutive-slash collapse, nil args, empty-string args
    - Test `dirname` and `basename` on paths with no `/` and on empty string
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.6, 1.7, 1.8, 1.9, 1.10, 1.11_
  - [ ]* 3.2 Write property test `prop-path-append-no-consecutive-slashes`
    - **Property 1: path-append produces no consecutive slashes**
    - Use `gen/vector` of `gen/string-alphanumeric` segments; assert `(not (re-find #"//" (apply path-append segments)))`
    - Run 100 trials via `defspec`
    - **Validates: Requirements 1.5**

- [x] 4. Create `test/gimel/config_spec_test.clj`
  - Write unit tests for `check-config` using the `valid-config` fixture map
  - Test that a valid config is returned unchanged
  - Test that a config missing a required key throws `ex-info`
  - Test that an invalid `:web-url` throws `ex-info`
  - Test that a non-positive `:port` throws `ex-info`
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 5. Create `test/gimel/config_test.clj`
  - Use `use-fixtures :each` to reset the `config-data` atom before and after each test
  - Test `expand-home` with a `~`-prefixed path and a plain path
  - Test `load-config` with a non-existent path loads the bundled default config
  - Test `get-config` returns a non-empty map after `load-config`
  - Test `get-config` throws `ex-info` when called on a blank atom
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 6. Create `test/gimel/handler_test.clj`
  - Test that `wrap-html-content-type` sets `Content-Type: text/html` for `.html` URIs
  - Test that it leaves `Content-Type` unchanged for non-`.html` URIs
  - Test that the wrapped handler receives the original request unchanged
  - _Requirements: 4.1, 4.2, 4.3_

- [x] 7. Create `test/gimel/highlight_test.clj`
  - Test that `highlight-code-blocks` transforms `<pre><code class="clojure">` to `class="language-clojure line-numbers"`
  - Test that HTML with no `<pre><code>` blocks is returned unchanged
  - _Requirements: 5.1, 5.2_

- [x] 8. Create `test/gimel/metadata_test.clj`
  - Write a placeholder `deftest` that calls `generate-navmenu` and asserts the namespace exists
  - _Requirements: 6.1, 6.2_

- [x] 9. Create `test/gimel/static_files_test.clj`
  - Use `use-fixtures :each` with `java.io.File/createTempFile` / `Files/createTempDirectory`; delete temp files in cleanup
  - [x] 9.1 Write unit tests for `checksum`, `ensure-path`, and `copy-files`
    - Test `checksum` returns a 64-char lowercase hex string
    - Test `ensure-path` creates missing parent directories
    - Test `copy-files` copies only files matching the given extensions
    - _Requirements: 7.1, 7.3, 7.4_
  - [ ]* 9.2 Write property test `prop-checksum-idempotent`
    - **Property 2: checksum is idempotent**
    - Use `gen/bytes`; write bytes to temp file; assert `(= (checksum f) (checksum f))`
    - Run 100 trials via `defspec`
    - **Validates: Requirements 7.2**

- [x] 10. Create `test/gimel/database_test.clj`
  - Use `use-fixtures :each` with an in-memory SQLite datasource via `with-redefs [db/datasource test-ds]`; create schema before each test, drop after
  - [x] 10.1 Write unit tests for database functions
    - Test `match-valid-table-or-column-name?` accepts alphanumeric/underscore names and throws on SQL injection strings
    - Test `insert-page` returns the same id on a second call with the same URL
    - Test `get-page-id` returns the correct id after insert and `nil` for unknown URLs
    - Test `insert-data` inserts page and metadata rows without error
    - _Requirements: 8.1, 8.2, 8.4, 8.5, 8.6, 8.7_
  - [ ]* 10.2 Write property test `prop-insert-page-idempotent`
    - **Property 3: insert-page is idempotent**
    - Use `gen/tuple gen/string-alphanumeric gen/string-alphanumeric gen/string`; insert twice, assert both returned ids are equal integers
    - Run 100 trials via `defspec`
    - **Validates: Requirements 8.3**

- [x] 11. Create `test/gimel/partial_pages_test.clj`
  - Use `use-fixtures :each` with an in-memory SQLite datasource (same pattern as database_test) to isolate `db/insert-data` calls made by `partial-pages`
  - Test `parse-header` with a valid YAML string returns the parsed map
  - Test `parse-header` with invalid YAML returns a map with `:error "NO META DATA"`
  - Test `partial-pages` excludes the `/navbar.html` key from the returned map
  - _Requirements: 9.1, 9.2, 9.3_

- [x] 12. Create `test/gimel/sitemap_test.clj`
  - Stub `get-sitemap-source` and `get-web-url` with `with-redefs` to avoid requiring a loaded config
  - Use `java.io.File/createTempFile` for test files; clean up in fixture
  - Test `path->url` converts `.md` extension to `.html`
  - Test `path->url` converts `.org` extension to `.html`
  - Test `file-data` returns a map with `:loc`, `:lastmod`, and `:changefreq` keys
  - Test that the `:loc` value starts with `https://`
  - _Requirements: 10.1, 10.2, 10.3, 10.4_

- [x] 13. Create `test/gimel/api_test.clj`
  - Stub `export` with `with-redefs` to avoid side effects
  - Use `ring.mock.request/request` to build test Ring requests against `(create-api-handler)`
  - Test `GET /api/export` returns HTTP 200
  - Test `POST /api/export-custom` with an invalid body returns HTTP 400
  - Test a request to an undefined route returns HTTP 404 with a JSON body containing `"error"`
  - Test `valid-data?` returns falsy when required directory paths are missing
  - _Requirements: 11.1, 11.2, 11.3, 11.4_

- [x] 14. Checkpoint — run all tests and verify zero failures
  - Run `lein test` and confirm zero failures and zero errors across all test namespaces
  - Confirm no temp files or test databases remain on disk after the run
  - Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 12.1, 12.2, 12.3_

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP
- Property tests use `defspec` from `clojure.test.check.clojure-test` and run 100 trials each
- All database tests use an in-memory SQLite datasource (`{:dbtype "sqlite" :dbname ":memory:"}`) — no production data is touched
- All filesystem tests use temp files/directories cleaned up in `use-fixtures :each`
- The `valid-config` fixture map (defined in design.md) is shared across config and config-spec tests
