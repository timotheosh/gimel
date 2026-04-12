# Requirements Document

## Introduction

Gimel currently maintains two separate configuration sources: a Clojure EDN file (`resources/config/gimel.edn`) for the backend server, and Emacs Lisp directory-local variables (`.dir-locals.el`) for the Emacs frontend. These two sources overlap on the server port/endpoint value, creating a maintenance burden and a potential source of drift.

This feature replaces `gimel.edn` with a single `gimel.toml` file that both the Clojure backend and the Emacs frontend can read. The TOML format is human-readable, widely supported, and has parsing libraries available for both Clojure (via a JVM TOML library) and Emacs Lisp (via `toml.el` or a hand-rolled parser). The `.dir-locals.el` file is updated to load values from `gimel.toml` rather than duplicating them inline.

## Glossary

- **Config_Loader**: The Clojure namespace (`gimel.config`) responsible for reading and validating the configuration file.
- **TOML_Parser**: The Clojure component that parses a TOML file into a Clojure map.
- **Emacs_Config_Loader**: The Emacs Lisp function(s) in `gimel.el` responsible for reading `gimel.toml` and populating `defcustom` variables.
- **Config_Validator**: The Clojure namespace (`gimel.config-spec`) that validates the parsed configuration map against a clojure.spec schema.
- **gimel.toml**: The single shared configuration file that replaces `gimel.edn`.
- **gimel.edn**: The legacy EDN configuration file to be replaced.
- **defcustom variable**: An Emacs Lisp user-customisable variable declared with `defcustom`.
- **Project_Root**: The directory containing the `gimel.toml` file, typically the root of the Gimel project.
- **gimel-config-file**: A `defcustom` variable in `gimel.el` holding the path to the active `gimel.toml` file. Defaults to `~/.config/gimel/gimel.toml`.

---

## Requirements

### Requirement 1: TOML Configuration File Format

**User Story:** As a developer, I want a single `gimel.toml` file that captures all configuration for both the Clojure backend and the Emacs frontend, so that I only need to edit one file when configuration changes.

#### Acceptance Criteria

1. THE `gimel.toml` file SHALL contain a `[server]` table with the keys `port`, `web-url`, `webroot`, `source-dir`, `sitemap-source`, `template`, and `footer`.
2. THE `gimel.toml` file SHALL contain a `[database]` table with the key `dbname`.
3. THE `gimel.toml` file SHALL contain an `[emacs]` table with the keys `source-path`, `target-path`, `navbar-file`, and `auto-publish`.
4. WHEN `gimel.toml` is parsed by either the Clojure backend or the Emacs frontend, THE resulting data structure SHALL contain values semantically equivalent to those previously held in `gimel.edn` and `.dir-locals.el`.
5. THE `gimel.toml` file SHALL be valid TOML 1.0 as defined by the TOML specification.

---

### Requirement 2: Clojure TOML Parsing

**User Story:** As a backend developer, I want the Clojure `Config_Loader` to read `gimel.toml` instead of `gimel.edn`, so that the server uses the single shared configuration file.

#### Acceptance Criteria

1. THE `Config_Loader` SHALL expose a `load-config` function that accepts a file path string and loads configuration from a TOML file at that path.
2. WHEN the specified file path does not exist, THE `Config_Loader` SHALL fall back to loading `gimel.toml` from the classpath resource `config/gimel.toml`.
3. WHEN a TOML file is successfully parsed, THE `TOML_Parser` SHALL produce a Clojure map whose structure is compatible with the existing `Config_Validator` spec (i.e., keys `:configuration`, `:public`, `:database` with the same leaf keys).
4. WHEN a TOML file contains a syntax error, THE `Config_Loader` SHALL throw an `ex-info` with a descriptive `:error` message and SHALL NOT start the server.
5. IF the parsed configuration map fails `Config_Validator` validation, THEN THE `Config_Loader` SHALL throw an `ex-info` with the spec explanation and SHALL NOT start the server.
6. THE `Config_Loader` SHALL provide the same accessor functions (`get-port`, `get-source-dir`, `get-webroot`, `get-web-url`, `get-template-dir`, `get-footer`, `get-dbname`) with unchanged return-value semantics.
7. FOR ALL valid `gimel.toml` files, parsing the file and then re-serialising the resulting Clojure map back to TOML and parsing again SHALL produce an equivalent Clojure map (round-trip property).

---

### Requirement 3: Clojure Configuration Validation

**User Story:** As a backend developer, I want the `Config_Validator` spec to cover the TOML-derived configuration map, so that invalid configuration is caught at startup.

#### Acceptance Criteria

1. THE `Config_Validator` SHALL validate that `:port` is a positive integer.
2. THE `Config_Validator` SHALL validate that `:web-url` matches the pattern `(http|https)://[^\s]+`.
3. THE `Config_Validator` SHALL validate that `:dbname`, `:source-dir`, `:webroot`, `:template`, `:footer`, and `:sitemap-source` are non-empty strings.
4. WHEN a required key is absent from the parsed map, THE `Config_Validator` SHALL produce a human-readable error message identifying the missing key.
5. WHEN all required keys are present and valid, THE `Config_Validator` SHALL return the configuration map unchanged.

---

### Requirement 4: Emacs TOML Configuration Loading

**User Story:** As an Emacs user, I want `gimel.el` to read `gimel.toml` from the project root and populate the `defcustom` variables automatically, so that I do not need to maintain duplicate values in `.dir-locals.el`.

#### Acceptance Criteria

1. THE `Emacs_Config_Loader` SHALL use the `toml.el` package (declared as a `Package-Requires` dependency in `gimel.el`) to parse `gimel.toml`.
2. THE `Emacs_Config_Loader` SHALL declare a `defcustom` variable `gimel-config-file` with a default value of `~/.config/gimel/gimel.toml` (expanded via `expand-file-name`).
3. THE `Emacs_Config_Loader` SHALL provide a function `gimel-load-config` that accepts a file path string pointing to `gimel.toml` and returns a parsed association list.
3. WHEN `gimel-load-config` is called with a valid `gimel.toml` path, THE `Emacs_Config_Loader` SHALL set `gimel-api-endpoint` to `"http://localhost:<port>"` where `<port>` is the value of `server.port` in `gimel.toml`.
4. WHEN `gimel-load-config` is called with a valid `gimel.toml` path, THE `Emacs_Config_Loader` SHALL set `gimel-source-path` to the value of `emacs.source-path` in `gimel.toml`.
5. WHEN `gimel-load-config` is called with a valid `gimel.toml` path, THE `Emacs_Config_Loader` SHALL set `gimel-target-path` to the value of `emacs.target-path` in `gimel.toml`.
6. WHEN `gimel-load-config` is called with a valid `gimel.toml` path, THE `Emacs_Config_Loader` SHALL set `gimel-navbar-file` to the value of `emacs.navbar-file` in `gimel.toml`.
7. WHEN `gimel-load-config` is called with a valid `gimel.toml` path, THE `Emacs_Config_Loader` SHALL set `gimel-auto-publish` to the boolean value of `emacs.auto-publish` in `gimel.toml`.
8. IF the file at the given path does not exist, THEN THE `Emacs_Config_Loader` SHALL signal an error with a message identifying the missing file and SHALL NOT modify any `defcustom` variable.
9. IF the file at the given path contains a TOML syntax error, THEN THE `Emacs_Config_Loader` SHALL signal an error with a descriptive message and SHALL NOT modify any `defcustom` variable.
10. FOR ALL valid `gimel.toml` files, calling `gimel-load-config` twice on the same file SHALL produce the same `defcustom` variable values as calling it once (idempotence property).

---

### Requirement 5: Directory-Local Variables Update

**User Story:** As an Emacs user, I want `.dir-locals.el` to invoke `gimel-load-config` rather than hard-coding values, so that the endpoint and other settings are always in sync with `gimel.toml`.

#### Acceptance Criteria

1. THE updated `.dir-locals.el` SHALL call `gimel-load-config` with the value of `gimel-config-file` (which defaults to `~/.config/gimel/gimel.toml`).
2. THE updated `.dir-locals.el` MAY override `gimel-config-file` to a project-local path before calling `gimel-load-config`, allowing per-project config files.
3. THE updated `.dir-locals.el` SHALL NOT hard-code `gimel-api-endpoint` or any other value that is now sourced from `gimel.toml`.
4. WHEN `.dir-locals.el` is loaded by Emacs in the project directory, THE `Emacs_Config_Loader` SHALL have populated all `defcustom` variables before any `gimel-*` interactive command is invoked.

---

### Requirement 6: Legacy EDN File Removal

**User Story:** As a developer, I want `gimel.edn` to be removed from the repository, so that there is no ambiguity about which configuration file is authoritative.

#### Acceptance Criteria

1. THE `Config_Loader` SHALL NOT reference or load `gimel.edn` after the migration is complete.
2. THE `gimel.edn` file SHALL be deleted from `resources/config/`.
3. WHEN the Clojure test suite is executed after migration, THE `Config_Loader` tests SHALL use `gimel.toml` fixtures and SHALL NOT reference `gimel.edn`.

---

### Requirement 7: Backward-Compatible Accessor API

**User Story:** As a backend developer, I want the public accessor functions in `gimel.config` to remain unchanged, so that no other Clojure namespace needs to be modified as part of this migration.

#### Acceptance Criteria

1. THE `Config_Loader` SHALL export `get-source-dir`, `get-webroot`, `get-web-url`, `get-template-dir`, `get-footer`, `get-dbname`, and `get-sitemap-source` with the same arities and return types as before the migration.
2. WHEN any accessor function is called before `load-config` has been invoked, THE `Config_Loader` SHALL throw an `ex-info` with the message `"Config is blank!!!"`.
3. FOR ALL valid `gimel.toml` inputs, the value returned by each accessor function SHALL equal the value of the corresponding key in the TOML file (no transformation other than type coercion).
