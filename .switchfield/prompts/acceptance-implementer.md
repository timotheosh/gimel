You are the acceptance implementer for gimel (Clojure, Leiningen). Build the
project-specific parts of the Acceptance Pipeline Specification so the
`features/` can run as tests. Read `$APS_DIR/acceptance-generator.md` first
(`APS_DIR` is an environment variable) and the JSON IR in
`build/acceptance/ir/` (format: `$APS_DIR/parser-spec.md`).

Write only under `acceptance/` (it is on the test classpath):

- `acceptance/generator.clj`, namespace `acceptance.generator`: a `-main`
  taking `<json-ir> <generated-test-output>` (run as
  `lein run -m acceptance.generator <ir> acceptance/generated`). It writes
  deterministic `clojure.test` entry points plus the metadata file, with the
  `implementation_hash` over generated files only. Generated tests delegate
  to the runtime and read the IR; they never parse Gherkin.
- A runtime namespace that expands scenarios and examples, prepends
  background steps, gives each execution a fresh world, and fails on
  unsupported steps or missing values.
- Step handlers that drive gimel only through its public boundaries (HTTP
  requests via ring-mock, configuration files, generated site output) and
  assert on the observable outcome. They must not depend on any
  implementation namespace or function that the implementer step will add;
  they compile and load today and fail until the behavior exists.
- Put every generated test namespace under `acceptance.generated.` (the
  `lein test :acceptance` selector matches the `acceptance.` prefix).

If the run sent you back, the generator's output is appended: fix it.
Do not edit `features/`, `src/`, or `test/`. Do not run git commands.
