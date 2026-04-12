# Implementation Plan: TOML Configuration Migration

## Overview

Migrate the Gimel configuration from EDN (`gimel.edn`) to a single shared `gimel.toml` file consumed by both the Clojure backend and the Emacs Lisp frontend. The Clojure accessor API remains unchanged; the Emacs frontend gains a `gimel-load-config` function; `.dir-locals.el` is updated to call it.

## Tasks

- [x] 1. Add TOML dependency and create `gimel.toml` config file
  - Add `[ilevd/toml "0.1.0"]` to `:dependencies` in `project.clj`
  - Create `resources/config/gimel.toml` with `[server]`, `[database]`, and `[emacs]` tables, mirroring the values currently in `gimel.edn` and `resources/org/.dir-locals.el`
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 2. Implement Clojure TOML parsing and config reshaping
  - [x] 2.1 Add `parse-toml` private function to `gimel.config`
    - Use `toml/read` with `:keywordize` to parse a TOML file
    - Reshape the result from `{:server {...} :database {...}}` into `{:configuration {:public {...} :database {...}}}` per the mapping table in the design
    - _Requirements: 2.1, 2.3_
  - [x] 2.2 Update `load-config` to use `parse-toml`
    - Replace `read-edn` call with `parse-toml`; update fallback classpath resource from `config/gimel.edn` to `config/gimel.toml`
    - Wrap `toml/read` in a try/catch; throw `ex-info` with `:error` key on TOML syntax errors
    - _Requirements: 2.1, 2.2, 2.4_
  - [x] 2.3 Write property test for TOML parse produces spec-valid config map
    - Create `test/gimel/config_property_test.clj` using `clojure.test.check`
    - **Property 1: TOML parse produces spec-valid config map**
    - **Validates: Requirements 2.3, 3.1, 3.2, 3.3, 3.5**
  - [x] 2.4 Write property test for config round-trip equivalence
    - **Property 2: Config round-trip equivalence**
    - **Validates: Requirements 2.7**

- [x] 3. Update `gimel.config-spec` validation
  - Ensure `:port` spec uses `pos-int?`, `:web-url` matches `(http|https)://[^\s]+`, and all string fields use `(s/and string? seq)` for non-empty validation
  - Update `check-config` to throw `ex-info` with a human-readable message identifying the missing/invalid key
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
  - [ ]* 3.1 Write property test for invalid config always throws
    - Add to `test/gimel/config_property_test.clj`
    - **Property 3: Invalid config always throws**
    - **Validates: Requirements 2.5, 3.1, 3.2, 3.3, 3.4**

- [x] 4. Verify and update Clojure accessor functions
  - Add `get-port` accessor to `gimel.config` (currently missing from the public API)
  - Confirm all other accessors (`get-source-dir`, `get-webroot`, `get-web-url`, `get-template-dir`, `get-footer`, `get-dbname`, `get-sitemap-source`) still read from the reshaped map correctly
  - _Requirements: 2.6, 7.1, 7.2, 7.3_
  - [ ]* 4.1 Write property test for accessor values match TOML input
    - Add to `test/gimel/config_property_test.clj`
    - **Property 4: Accessor values match TOML input**
    - **Validates: Requirements 7.3, 2.6**

- [x] 5. Checkpoint — Ensure all Clojure tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Update Clojure tests to use TOML fixtures
  - Update `test/gimel/config_test.clj`: replace all references to `gimel.edn` with `gimel.toml`; add a fixture that loads `resources/config/gimel.toml` and asserts each accessor returns the expected value
  - Update `test/gimel/config_spec_test.clj` if any test references EDN-specific behaviour
  - _Requirements: 6.3_

- [x] 7. Remove legacy EDN file
  - Delete `resources/config/gimel.edn`
  - Remove any `require` or reference to `clojure.edn` from `gimel.config` if it is no longer needed
  - _Requirements: 6.1, 6.2_

- [x] 8. Implement `gimel-load-config` in `gimel.el`
  - [x] 8.1 Add `(toml "1.1.0.0")` to `Package-Requires` in `gimel.el`
    - _Requirements: 4.1_
  - [x] 8.2 Add `gimel-config-file` defcustom variable to `gimel.el`
    - Default value: `(expand-file-name "~/.config/gimel/gimel.toml")`
    - Type: `'file`, group: `'gimel`
    - _Requirements: 4.2_
  - [x] 8.3 Implement `gimel-load-config` function
    - Signal `(error "gimel-load-config: file not found: %s" toml-path)` if file does not exist; do not modify any variable
    - Use `condition-case` around `toml:read-from-file`; re-signal with descriptive message on parse error; do not modify any variable
    - On success, set `gimel-api-endpoint`, `gimel-source-path`, `gimel-target-path`, `gimel-navbar-file`, and `gimel-auto-publish` from the parsed alist; return the full alist
    - _Requirements: 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, 4.10_
  - [x] 8.4 Write unit tests for `gimel-load-config` in `resources/emacs/tests/gimel-test.el`
    - Test: valid temp TOML file → returns alist and sets all defcustom variables correctly
    - Test: non-existent path → signals error, variables unchanged
    - Test: invalid TOML → signals error, variables unchanged
    - _Requirements: 4.3, 4.9, 4.10_
  - [ ]* 8.5 Write property test for `gimel-load-config` populates all defcustom variables
    - Create `resources/emacs/tests/gimel-property-test.el`
    - Use a `dotimes` loop (100 iterations) with random port, paths, and boolean values written to a temp TOML file
    - **Property 5: gimel-load-config populates all defcustom variables**
    - **Validates: Requirements 4.4, 4.5, 4.6, 4.7, 4.8**
  - [ ]* 8.6 Write property test for `gimel-load-config` idempotence
    - Add to `resources/emacs/tests/gimel-property-test.el`
    - **Property 6: gimel-load-config is idempotent**
    - **Validates: Requirements 4.11**

- [x] 9. Update `.dir-locals.el`
  - Set `gimel-config-file` to the project-local `gimel.toml` path (e.g. the project root)
  - Call `(gimel-load-config gimel-config-file)` via `eval`
  - Remove all hard-coded values; no `gimel-api-endpoint` or other settings remain inline
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 10. Final checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP
- Property tests use `clojure.test.check` (already in `project.clj`) for Clojure and a `dotimes`-based loop for Emacs Lisp
- The `[emacs]` TOML table is parsed by the Clojure backend but not stored in the config atom — it is only consumed by `gimel-load-config`
- The Clojure config map shape (`{:configuration {:public {...} :database {...}}}`) is unchanged; `gimel.config-spec` requires no structural changes
