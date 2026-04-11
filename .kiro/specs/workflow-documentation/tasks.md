# Implementation Plan: Workflow Documentation

## Overview

Create a single comprehensive org-mode guide (`docs/guide.org`) documenting the end-to-end gimel workflow. Org-mode is the natural format for this project. Each task corresponds to authoring one section of the guide, verified against the actual source files in the repository.

## Tasks

- [x] 1. Create the guide file and write the Overview & Workflow section
  - Create `docs/guide.org` with a top-level heading and introduction paragraph
  - Write a numbered list or diagram describing the full workflow sequence: Org_File → Gimel_El split → HTML_Snippet → `GET /api/export` → Gimel_Server renders → Webroot
  - Identify Gimel_El and Gimel_Server as distinct components and describe the role of each
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 2. Write the Gimel_El Installation section
  - Document manual installation via `load-file` / `require`
  - Document Doom Emacs installation
  - Document `straight.el` installation
  - Document `el-get` installation
  - Document `quelpa` installation
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 3. Write the Recommended Project Structure section
  - Present the recommended directory layout (`org/`, `html/`, `public/`) as a non-mandatory example
  - State explicitly that this structure is a recommendation, not a requirement
  - _Requirements: 3.1, 3.2_

- [x] 4. Write the Config_File reference section
  - [x] 4.1 Document `gimel.edn` format and location
    - State that the file is EDN, named `gimel.edn` by default, and that `--config <path>` overrides the path
    - State that `resources/config/gimel.edn` in the repository can be used as a starting point
    - Verify the example against `resources/config/gimel.edn` and `src/gimel/config_spec.clj`
    - _Requirements: 4.1, 4.2_
  - [x] 4.2 Document all config fields with annotated example
    - Document each `:configuration :public` field: `:sitemap-source`, `:source-dir`, `:webroot`, `:template`, `:footer`, `:web-url`, `:port`
    - Document `:configuration :database :dbname`
    - Include the complete annotated `gimel.edn` example from requirements 4.5
    - State that `:port` must match `gimel-api-endpoint` in `.dir-locals.el`
    - _Requirements: 4.3, 4.4, 4.5, 4.6_

- [x] 5. Write the Dir_Locals Configuration section
  - Instruct the author to create `.dir-locals.el` in the project root
  - Include the complete example `.dir-locals.el` from requirements 5.2
  - Document each variable: `gimel-auto-publish`, `gimel-api-endpoint`, `gimel-source-path`, `gimel-target-path`, `gimel-navbar-file`
  - State that `gimel-source-path` and `gimel-target-path` are resolved relative to the Projectile project root
  - State that `gimel-navbar-file` defaults to `"navbar.org"` and describe its navbar post-processing behaviour
  - State that the port in `gimel-api-endpoint` must match `:port` in `gimel.edn`
  - Verify variable names against `resources/emacs/gimel.el`
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

- [x] 6. Write the Org_File Authoring and Save Workflow section
  - [x] 6.1 Document Org_File structure and metadata conversion
    - State that Org_Files live in `gimel-source-path` / `:sitemap-source`
    - Describe the split on the first line matching `^\*+ ` into Metadata_Section and Body_Section
    - Document that `#+KEYWORD` lines become YAML frontmatter; `#+startup` and `#+options` are excluded
    - Document the key downcasing rule (e.g. `#+TITLE` → `title`)
    - Document the three value conversion rules (JSON-prefixed values vs. plain strings)
    - Verify conversion rules against `gimel-org-metadata-to-yaml` in `resources/emacs/gimel.el`
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_
  - [x] 6.2 Document the auto-save workflow and batch processing
    - Describe the four-step sequence triggered on save when `gimel-auto-publish` is enabled (copy assets → write HTML_Snippet → `GET /api/export` → Gimel_Server renders)
    - State the condition: `org-mode` buffer and `gimel-auto-publish` non-nil
    - Describe `gimel-batch-process-org-files` and its destructive behaviour (deletes Source_Dir first)
    - Verify hook and batch function names against `resources/emacs/gimel.el`
    - _Requirements: 6.7, 6.8, 6.9, 6.10_

- [x] 7. Write the Export API reference section
  - Document `GET /api/export`: triggers full export, returns HTTP 200 with body `ok`
  - Document `POST /api/export-custom`: required fields `source`, `public`; optional `sitemap-source`; returns HTTP 400 on invalid input
  - State that unrecognised paths return HTTP 404 with a JSON error body
  - Verify endpoint paths and response codes against `src/gimel/api/core.clj`
  - _Requirements: 7.1, 7.2, 7.3, 7.4_

- [x] 8. Write the Template System section
  - Describe required template directory files: `index.html` and `template.edn`
  - Document the `template.edn` format: `:body` map with `:navbar`, `:main`, `:footer` CSS selectors
  - State that CSS and JS assets are fingerprinted and served by Gimel_Server
  - Include a complete example `template.edn`
  - Verify selector format against `resources/templates/html5-page-layout/template.edn` and `src/gimel/templates.clj`
  - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [x] 9. Write the SQLite Database section
  - State that the database is created and populated on every Export using `:dbname` from config
  - Document the `pages` table schema: `id`, `url`, `title`, `content`
  - Describe how non-`title` frontmatter fields create separate metadata tables linked via join tables
  - State that the database is deleted and recreated on each Export
  - State explicitly that the database is not currently used at runtime and is reserved for future features
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [x] 10. Final review and cross-reference check
  - Verify all internal cross-references (e.g. "see the Config_File section") resolve to actual headings in the document
  - Confirm every glossary term from `requirements.md` appears in the guide with a consistent definition
  - Confirm all code examples (EDN, Elisp, shell commands) have been verified against the actual source files
  - _Requirements: 1.1–9.5 (completeness check)_

## Notes

- No property-based tests apply — this feature produces a documentation artifact, not executable code
- All code examples must be verified against the actual source files before the section is considered complete
- The guide should be readable both linearly (first-time setup) and as a reference (individual sections)
