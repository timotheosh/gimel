# Requirements Document

## Introduction

Gimel is a flat-file CMS and static site generator written in Clojure. It fits a specific authoring workflow centered on Emacs org-mode: authors write content in org-mode files, the `gimel.el` Emacs package converts those files to HTML snippets (with YAML frontmatter), and the gimel server assembles and exports a complete static site. This document captures the requirements for documenting that end-to-end workflow so that new users and contributors can understand, set up, and operate gimel correctly.

## Glossary

- **Gimel**: The Clojure web application and static site generator being documented.
- **Gimel_Server**: The running instance of the gimel Clojure application (started via `lein run` or uberjar).
- **Gimel_El**: The Emacs Lisp package (`gimel.el`) that converts org-mode files to HTML snippets and triggers exports via the gimel API.
- **Author**: A person who writes and publishes content using the gimel workflow.
- **Operator**: A person who installs, configures, and runs the Gimel_Server.
- **Org_File**: An Emacs org-mode source file (`.org`) containing page content and file-level metadata properties.
- **Metadata_Section**: The portion of an Org_File before the first org heading (i.e., before the first line matching `^\*+ `), containing `#+PROPERTY` lines that Gimel_El converts to YAML frontmatter.
- **Body_Section**: The portion of an Org_File from the first org heading onward, which Gimel_El converts to an HTML fragment.
- **HTML_Snippet**: An HTML fragment file (`.html`) produced by Gimel_El from an Org_File, containing a YAML frontmatter block followed by an HTML body.
- **Frontmatter**: A YAML metadata block delimited by `---` at the top of an HTML_Snippet, derived from the Metadata_Section of the source Org_File.
- **Source_Dir**: The directory Gimel_Server reads HTML_Snippets from (configured as `:source-dir` in `gimel.edn`; also the value of `gimel-target-path` in `.dir-locals.el`).
- **Sitemap_Source**: The directory containing the original Org_Files (configured as `:sitemap-source` in `gimel.edn`; also the value of `gimel-source-path` in `.dir-locals.el`).
- **Webroot**: The output directory where Gimel_Server writes the fully rendered static site (configured as `:webroot` in `gimel.edn`).
- **Template**: An HTML layout and associated configuration that Gimel_Server uses to wrap page content when rendering pages to Webroot.
- **Config_File**: The EDN configuration file (default name `gimel.edn`) that specifies Sitemap_Source, Source_Dir, Webroot, Template, database name, port, and other settings.
- **Dir_Locals**: The `.dir-locals.el` file placed in the project root that configures Gimel_El variables for the project.
- **SQLite_Database**: The SQLite database file Gimel_Server creates and populates with page metadata during each export.
- **Export**: The process by which Gimel_Server reads all HTML_Snippets from Source_Dir, renders them through the Template, and writes the result to Webroot.
- **API**: The HTTP API exposed by Gimel_Server, used by Gimel_El to trigger exports.
- **Navbar**: An optional org-mode file (default `navbar.org`) whose generated HTML links are post-processed to absolute paths and used as the site navigation bar.

---

## Requirements

### Requirement 1: Document the Overall Workflow

**User Story:** As an Author, I want a clear description of the end-to-end gimel workflow, so that I understand how my org-mode files become a published website.

#### Acceptance Criteria

1. THE Workflow_Documentation SHALL describe the sequence: Author edits Org_File in Sitemap_Source → Gimel_El splits Org_File into Metadata_Section and Body_Section → Gimel_El converts Body_Section to an HTML_Snippet in Source_Dir → Gimel_El pings Gimel_Server via `GET /api/export` → Gimel_Server reads HTML_Snippets from Source_Dir, applies the Template, and writes rendered pages to Webroot.
2. THE Workflow_Documentation SHALL include a numbered list or diagram showing each stage of the workflow and the artifacts produced at each stage.
3. THE Workflow_Documentation SHALL identify Gimel_El and Gimel_Server as distinct components and describe the role of each.

---

### Requirement 2: Document Gimel_El Installation

**User Story:** As an Author, I want installation instructions for Gimel_El, so that I can add it to my Emacs configuration using my preferred package manager.

#### Acceptance Criteria

1. THE Workflow_Documentation SHALL provide installation instructions for loading `gimel.el` manually using `load-file` or `require`.
2. THE Workflow_Documentation SHALL provide installation instructions for Doom Emacs.
3. THE Workflow_Documentation SHALL provide installation instructions using `straight.el`.
4. THE Workflow_Documentation SHALL provide installation instructions using `el-get`.
5. THE Workflow_Documentation SHALL provide installation instructions using `quelpa`.

---

### Requirement 3: Document the Recommended Project Structure

**User Story:** As an Author, I want a recommended project layout, so that I can organise my files in a way that works well with gimel.

#### Acceptance Criteria

1. THE Workflow_Documentation SHALL present the following recommended directory structure as a non-mandatory example:
   ```
   <root>/
   ├── .dir-locals.el
   ├── org/          # Org-file sources (Sitemap_Source)
   ├── html/         # HTML snippets generated by Gimel_El (Source_Dir)
   └── public/       # Website HTML generated by Gimel_Server (Webroot)
   ```
2. THE Workflow_Documentation SHALL state that this structure is a recommendation and not strictly required.

---

### Requirement 4: Document the Config_File

**User Story:** As an Operator, I want a reference for the Config_File format and every field it contains, so that I can configure gimel correctly for my environment.

#### Acceptance Criteria

1. THE Workflow_Documentation SHALL state that the Config_File is an EDN file named `gimel.edn` by default, and that an alternative path can be supplied when starting Gimel_Server with `--config <path>`.
2. THE Workflow_Documentation SHALL state that the default example Config_File is located at `resources/config/gimel.edn` in the repository and can be used as a starting point.
3. THE Workflow_Documentation SHALL document each field under `:configuration :public`: `:sitemap-source`, `:source-dir`, `:webroot`, `:template`, `:footer`, `:web-url`, and `:port`.
4. THE Workflow_Documentation SHALL document the field under `:configuration :database`: `:dbname`.
5. THE Workflow_Documentation SHALL include a complete annotated example of a valid `gimel.edn` file matching the structure below:
   ```edn
   {:configuration
    {:public
     {:sitemap-source "/path_to/org_directory"
      :source-dir     "/path_to/html_snippets_directory"
      :webroot        "/path_to/public_dir"
      :template       "resources/templates/html5-page-layout"
      :footer         "Copyright Me © 2026"
      :web-url        "https://example.com"
      :port           8080}
     :database
     {:dbname "gimel.db"}}}
   ```
6. THE Workflow_Documentation SHALL state that the port value in `gimel.edn` must match the port used in `gimel-api-endpoint` in `.dir-locals.el`.

---

### Requirement 5: Document the Dir_Locals Configuration

**User Story:** As an Author, I want instructions for creating `.dir-locals.el`, so that Gimel_El is configured correctly for my project without modifying my global Emacs init file.

#### Acceptance Criteria

1. THE Workflow_Documentation SHALL instruct the Author to create a `.dir-locals.el` file in the project root.
2. THE Workflow_Documentation SHALL include a complete example `.dir-locals.el` matching the structure below:
   ```elisp
   ((nil . ((gimel-auto-publish . t)
            (gimel-api-endpoint . "http://localhost:8080")
            (gimel-source-path  . "/same_as_the/sitemap-source/in_the/gimel_edn")
            (gimel-target-path  . "/same_as_the/source-dir/in_the/gimel_edn")
            (gimel-navbar-file  . "name-of-the-org-file-for-navigation-if-you-want-it.org"))))
   ```
3. THE Workflow_Documentation SHALL document each Gimel_El variable set in Dir_Locals: `gimel-auto-publish`, `gimel-api-endpoint`, `gimel-source-path`, `gimel-target-path`, and `gimel-navbar-file`.
4. THE Workflow_Documentation SHALL state that `gimel-source-path` and `gimel-target-path` are resolved relative to the Projectile project root by default.
5. THE Workflow_Documentation SHALL state that `gimel-navbar-file` defaults to `"navbar.org"`, and that when the current file matches this name, Gimel_El post-processes its generated HTML links to use absolute paths.
6. THE Workflow_Documentation SHALL state that the port in `gimel-api-endpoint` must match the `:port` value in `gimel.edn`.

---

### Requirement 6: Document Org_File Authoring and the Save Workflow

**User Story:** As an Author, I want to understand how to write org-mode files and what happens when I save them, so that I can author content correctly and rely on automatic publishing.

#### Acceptance Criteria

1. THE Workflow_Documentation SHALL state that Org_Files are written in the directory configured as `gimel-source-path` (which corresponds to `:sitemap-source` in `gimel.edn`).
2. THE Workflow_Documentation SHALL state that Gimel_El splits each Org_File on the first org heading — the first line matching the regular expression `^\*+ ` — into a Metadata_Section and a Body_Section.
3. THE Workflow_Documentation SHALL state that the Metadata_Section is everything before the first heading, and that `#+KEYWORD` lines in this section are converted to YAML frontmatter and inserted into the SQLite_Database.
4. THE Workflow_Documentation SHALL state that `#+startup` and `#+options` lines are explicitly excluded from frontmatter conversion, as they are org directives rather than content metadata.
5. THE Workflow_Documentation SHALL state that property keys are downcased during conversion — for example, `#+TITLE: Foo` becomes `title: "Foo"` in the frontmatter — and that authors should be aware their property names are always case-normalized.
6. THE Workflow_Documentation SHALL document the three value conversion rules applied by `gimel-org-metadata-to-yaml`:
   a. Values starting with `[`, `"`, or `{` are parsed as JSON — arrays become YAML sequences, objects become YAML mappings.
   b. All other values are wrapped in double quotes as plain YAML strings.
7. THE Workflow_Documentation SHALL state that the Body_Section is everything from the first heading onward, and that Gimel_El converts it to an HTML fragment placed in Source_Dir (configured as `gimel-target-path`).
8. WHEN `gimel-auto-publish` is enabled and the Author saves an Org_File, THE Workflow_Documentation SHALL describe the following sequence that Gimel_El executes automatically:
   a. Copies asset files (images, CSS, JS) from Sitemap_Source to Source_Dir.
   b. Converts the Org_File to an HTML_Snippet with YAML frontmatter and writes it to Source_Dir.
   c. Sends a `GET /api/export` request to Gimel_Server.
   d. Gimel_Server reads the updated HTML_Snippets from Source_Dir, applies the Template, and writes rendered pages to Webroot.
9. THE Workflow_Documentation SHALL state that the after-save hook `gimel-run-on-save` only executes when the current buffer's major mode is `org-mode` and `gimel-auto-publish` is non-nil.
10. THE Workflow_Documentation SHALL describe the `gimel-batch-process-org-files` function and state that it deletes the entire Source_Dir first, then copies assets, then processes all `.org` files in Sitemap_Source.

---

### Requirement 7: Document the Export API

**User Story:** As an Operator or Author, I want a reference for the gimel HTTP API, so that I can trigger exports programmatically or integrate gimel into other tools.

#### Acceptance Criteria

1. THE Workflow_Documentation SHALL document the `GET /api/export` endpoint, stating that it triggers a full Export using the paths from the Config_File and returns HTTP 200 with body `ok` on success.
2. THE Workflow_Documentation SHALL document the `POST /api/export-custom` endpoint, including the required JSON request body fields: `source`, `public`, and the optional `sitemap-source`.
3. WHEN the `POST /api/export-custom` request body is invalid or a required directory path does not exist, THE Workflow_Documentation SHALL state that the endpoint returns HTTP 400 with a JSON error body.
4. THE Workflow_Documentation SHALL state that all API endpoints return HTTP 404 with a JSON error body for unrecognised paths.

---

### Requirement 8: Document the Template System

**User Story:** As an Operator, I want to understand how templates work, so that I can create or customise the HTML layout for my site.

#### Acceptance Criteria

1. THE Workflow_Documentation SHALL describe the required files in a Template directory: `index.html` and `template.edn`.
2. THE Workflow_Documentation SHALL document the `template.edn` format, including the `:body` map with `:navbar`, `:main`, and `:footer` CSS selectors that Gimel_Server uses to inject content.
3. THE Workflow_Documentation SHALL state that CSS and JS assets in the Template directory are fingerprinted and served by Gimel_Server.
4. THE Workflow_Documentation SHALL include an example `template.edn` file.

---

### Requirement 9: Document the SQLite Database and Metadata Storage

**User Story:** As an Operator, I want to understand what data gimel stores in the SQLite_Database, so that I can understand its current role and its intended future use.

#### Acceptance Criteria

1. THE Workflow_Documentation SHALL state that Gimel_Server creates and populates the SQLite_Database on every Export, using the filename configured under `:configuration :database :dbname`.
2. THE Workflow_Documentation SHALL describe the `pages` table schema: `id`, `url`, `title`, and `content` columns.
3. THE Workflow_Documentation SHALL describe how Frontmatter fields other than `title` are stored as separate metadata tables and linked to pages via join tables.
4. THE Workflow_Documentation SHALL state that the SQLite_Database is deleted and recreated on each Export.
5. THE Workflow_Documentation SHALL explicitly state that the SQLite_Database is not currently used at runtime — it is populated during Export but no existing feature reads from it. It is reserved for future features such as dynamic menus and related-links sections.
