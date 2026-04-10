# Design Document: Missing Tests

## Overview

This design covers adding comprehensive test coverage to the gimel Clojure CMS project. Currently only `gimel.markdown` has tests. We need to add tests for 13 additional namespaces, fix several bugs in `gimel.os` that are exposed by the new requirements, and introduce property-based testing for three universal invariants using `test.check`.

The approach is:
- Fix `gimel.os` to handle nil/empty inputs gracefully before writing tests
- Use in-memory SQLite for all database tests (no production data touched)
- Use `java.io.File/createTempFile` and temp directories for file-system tests
- Use `clojure.test` fixtures for setup/teardown and isolation
- Use `org.clojure/test.check` for three property-based tests

## Architecture

```
test/gimel/
├── os_test.clj            (Req 1)  - path-append, dirname, basename
├── config_spec_test.clj   (Req 2)  - check-config spec validation
├── config_test.clj        (Req 3)  - expand-home, load-config, get-config
├── handler_test.clj       (Req 4)  - wrap-html-content-type
├── highlight_test.clj     (Req 5)  - highlight-code-blocks
├── metadata_test.clj      (Req 6)  - generate-navmenu placeholder
├── static_files_test.clj  (Req 7)  - checksum, ensure-path, copy-files
├── database_test.clj      (Req 8)  - SQLite CRUD with in-memory DB
├── partial_pages_test.clj (Req 9)  - parse-header, partial-pages
├── sitemap_test.clj       (Req 10) - path->url, file-data
├── api_test.clj           (Req 11) - Ring handler routing
└── markdown_test.clj      (existing, unchanged)
```

Source fix:
```
src/gimel/os.clj           - nil/empty guard in path-append, dirname, basename
```

## Components and Interfaces

### gimel.os fixes

The current implementation has three bugs exposed by requirements 1.6–1.11:

1. `path-append` — `string/join` on a seq containing `nil` will call `.toString` on nil producing `"null"`. Empty strings produce leading/trailing/consecutive slashes.
2. `dirname` — `string/last-index-of` returns `nil` when no `/` is found; `subs` then throws `NullPointerException`.
3. `basename` — same issue as `dirname`.

Fixed implementation:

```clojure
(defn path-append [& paths]
  (-> (remove #(or (nil? %) (empty? %)) paths)
      (string/join "/")
      (string/replace #"[\\/]+" "/")))

(defn dirname [path]
  (let [idx (string/last-index-of path "/")]
    (if (nil? idx) "." (subs path 0 idx))))

(defn basename [path]
  (let [idx (string/last-index-of path "/")]
    (if (nil? idx) path (subs path (inc idx)))))
```

### Test isolation helpers

**Database tests** — override the `datasource` mount state with an in-memory SQLite datasource using `with-redefs` inside a `use-fixtures` block:

```clojure
(def test-ds (jdbc/get-datasource {:dbtype "sqlite" :dbname ":memory:"}))

(defn db-fixture [f]
  (with-redefs [db/datasource test-ds]
    (jdbc/execute! test-ds ["CREATE TABLE IF NOT EXISTS pages ..."])
    (f)
    (jdbc/execute! test-ds ["DROP TABLE IF EXISTS pages"])))

(use-fixtures :each db-fixture)
```

**File-system tests** — use `java.io.File/createTempFile` for single files and `Files/createTempDirectory` for directories. Clean up in a `finally` block or `use-fixtures :each`.

**Config tests** — reset the `config-data` atom before and after each test using `use-fixtures :each`.

**Sitemap tests** — stub `get-sitemap-source` and `get-web-url` with `with-redefs` to avoid requiring a loaded config.

**API tests** — call `(create-api-handler)` directly; stub `export` with `with-redefs` to avoid side effects.

### Property-based testing

Add `[org.clojure/test.check "1.1.1"]` to `:dependencies` in `project.clj`.

Use `clojure.test.check.generators` for generators and `clojure.test.check.properties/for-all` for properties. Run each property with at least 100 trials via `clojure.test.check/quick-check`.

Integrate with `clojure.test` using the `defspec` macro from `clojure.test.check.clojure-test`.

## Data Models

### Test config fixture

A minimal valid config map used across config and spec tests:

```clojure
(def valid-config
  {:configuration
   {:public {:sitemap-source "/tmp/src"
             :source-dir     "/tmp/src"
             :webroot        "/tmp/public"
             :template       "resources/templates/naurrnen-layout"
             :footer         "Footer text"
             :web-url        "https://example.com"
             :port           8080}
    :database {:dbname "/tmp/test.db"}}})
```

### In-memory datasource

SQLite supports `:memory:` as the dbname. Each test fixture creates the schema fresh and drops it after, ensuring full isolation.

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: path-append produces no consecutive slashes

*For any* sequence of non-nil path segment strings, calling `path-append` on those segments SHALL produce a string that contains no two or more consecutive `/` characters.

**Validates: Requirements 1.5**

### Property 2: checksum is idempotent

*For any* sequence of bytes written to a temporary file, calling `checksum` on that file twice SHALL return the same 64-character hexadecimal string both times.

**Validates: Requirements 7.2**

### Property 3: insert-page is idempotent

*For any* URL, title, and content strings, calling `insert-page` twice with the same arguments on an in-memory database SHALL return the same integer page id both times.

**Validates: Requirements 8.3**

## Error Handling

| Scenario | Current behavior | Fixed behavior |
|---|---|---|
| `path-append` with nil arg | NullPointerException | nil args silently skipped |
| `path-append` with empty string arg | leading/trailing/double slashes | empty strings silently skipped |
| `dirname` on path with no `/` | NullPointerException | returns `"."` |
| `basename` on path with no `/` | NullPointerException | returns the path itself |
| `dirname` on empty string | NullPointerException | returns `"."` |
| `basename` on empty string | NullPointerException | returns `""` |

All other error handling (config validation, database SQL injection guard, YAML parse errors) is already implemented in the source; tests verify the existing behavior.

## Testing Strategy

### Unit tests (clojure.test)

Each test namespace uses `clojure.test/deftest` and `is`/`are` assertions. Fixtures handle setup and teardown. The goal is one test file per source namespace.

- `os_test` — example tests for normal behavior + edge-case tests for nil/empty/no-slash inputs
- `config_spec_test` — example tests for valid and invalid config maps
- `config_test` — example tests for expand-home, load-config, get-config; reset atom in fixture
- `handler_test` — example tests for Content-Type header logic
- `highlight_test` — example tests for code block class transformation
- `metadata_test` — smoke/placeholder test for generate-navmenu
- `static_files_test` — example tests using temp files and temp directories
- `database_test` — example tests using in-memory SQLite via fixture
- `partial_pages_test` — example tests for parse-header and partial-pages filtering
- `sitemap_test` — example tests using temp files and stubbed config accessors
- `api_test` — example tests using ring-mock requests against the live handler

### Property-based tests (test.check via defspec)

Three `defspec` tests, each running 100 trials minimum:

1. **`prop-path-append-no-consecutive-slashes`** in `os_test.clj`
   - Generator: `gen/vector (gen/such-that some? gen/string-alphanumeric)`
   - Property: `(not (re-find #"//" (apply path-append segments)))`
   - Tag: `Feature: missing-tests, Property 1: path-append produces no consecutive slashes`

2. **`prop-checksum-idempotent`** in `static_files_test.clj`
   - Generator: `gen/bytes`
   - Property: write bytes to temp file, assert `(= (checksum f) (checksum f))`
   - Tag: `Feature: missing-tests, Property 2: checksum is idempotent`

3. **`prop-insert-page-idempotent`** in `database_test.clj`
   - Generator: `gen/tuple gen/string-alphanumeric gen/string-alphanumeric gen/string`
   - Property: insert twice, assert both returned ids are equal integers
   - Tag: `Feature: missing-tests, Property 3: insert-page is idempotent`

### Cleanup guarantee

Every test namespace that touches the filesystem or the config atom uses `use-fixtures :each` with a cleanup step. No temp files or in-memory databases persist after the test run.

### Running tests

```
lein test
```

All tests (existing markdown tests + new tests) must pass with zero failures and zero errors.
