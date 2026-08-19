# CLAUDE.md

Guidance for Claude Code when working in this repository.

## What this repository is

This repository tracks the changes of the java model published by the
[unblu/openapi](https://github.com/unblu/openapi) repository (branch `main/8.x.x`,
versions `8.x.x`). It is a workbench, not a released artifact, and it grows in three
steps:

1. **Measure** — `revapi/` holds an API-change report per pair of consecutive released
   versions.
2. **Verify by experiment** — `examples/` turns the interesting findings into small
   JBang programs that prove the break, and doubles as a regression test suite.
3. **Automate the migration** *(next step, not built yet)* — turn each verified break
   into something a consumer of the jersey client can apply to their own code: an
   OpenRewrite recipe per breaking change, and/or a description precise enough to hand
   to a coding agent. The examples are the fixtures this needs, since each is a
   before/after pair with a known compile outcome on both sides.

Keep that third step in mind when adding examples: an example is not only a
demonstration, it is the raw material for a future migration recipe. Prefer one clear
mechanical change per example over a program that shows several at once.

Apart from `examples/`, the repository contains **no java sources of its own**.

A local clone of the source repository is expected at `../../Git/openapi`
(i.e. `/Users/jbr/Unblu/Git/openapi`); its `docs/index.adoc` and `README.adoc`
describe what the published artifacts are.

Two artifacts are tracked, both in the group `com.unblu.openapi`:

| Artifact            | Content                                        |
|---------------------|------------------------------------------------|
| `models-v4`         | Java classes corresponding to the JSON bodies  |
| `jersey3-client-v4` | Client implemented using Jersey 3              |

Both are published to Maven Central and, as of `8.40.4`, both exist for exactly the
same set of versions.

## Layout

```
examples/
  Util.java                   argument parsing shared by every example
  README.md                   what each example targets, and whether it still runs
  <SomeExample>.java          one JBang program per verified breaking change

revapi/
  revapi.java                 JBang wrapper around the Revapi CLI
  logback.xml                 logging config for the wrapper
  revapi-config.json          what the analysis reports and what it ignores
  create-report-folder.sh     creates a report folder and its run-revapi.sh scripts
  run-revapi.sh               runs the run-revapi.sh scripts of one report folder
  run-all-revapi.sh           runs the run-revapi.sh scripts of every report folder
  recreate-all-run-revapi.sh  re-creates every generated run-revapi.sh
  <old>_<new>/
    jersey3-client-v4/
      run-revapi.sh           generated: the exact command producing the two reports
      report.txt              org.revapi:revapi-reporter-text
      report.json             org.revapi:revapi-reporter-json (indented)
    models-v4/
      run-revapi.sh
      report.txt
      report.json
```

Each report folder is self-describing: the generated `run-revapi.sh` next to the two
reports *is* the command that produced them, with every classpath entry spelled out.
Reading it is the fastest way to understand how a given report was made.

One folder per pair of **consecutive published versions**, in the order returned by
Maven Central's `maven-metadata.xml` sorted with `sort -V`. Note that this is not one
folder per minor version: some minors have two published patches (`8.6.0` → `8.6.1`,
`8.8.1` → `8.8.2`, `8.32.2` → `8.32.3`), and each such step gets its own folder.

## Working on `examples/`

Read `examples/README.md` first — it carries the conventions. The two that matter most:

* **The contract.** An example must compile with the `FROM` version of its
  `revapi/<FROM>_<TO>/` folder and must *fail* to compile with the `TO` version. One
  that compiles with both proves nothing and does not belong in the folder. Always
  verify both directions before considering an example finished.
* **Everything in `examples/` is public.** No real host names, no credentials, no
  customer or company names, no real person names. Use `Example Company`,
  `@example.com` addresses and `https://unblu.example.com/app`. Set-up code adapted
  from elsewhere gets the same treatment: convert the calls, replace every value.

Every example takes the same arguments — `<server> --superadmin|--admin <username>
--password <password>` — parsed by `Util.java`, which is pulled in with
`//SOURCES Util.java` and deliberately uses no generated-client type so it works across
all client versions. The one exception is an example that genuinely has no server to
call (a webhook payload parser); document any such exception in the README.

## Which script to run when

| Situation | Command |
|---|---|
| A new version `N` was released | `create-report-folder.sh <N-1> <N>` then `run-revapi.sh <N-1> <N>` |
| `revapi-config.json` changed | `run-all-revapi.sh` |
| `create-report-folder.sh` changed (extension versions, options, generated layout) | `recreate-all-run-revapi.sh` then `run-all-revapi.sh` |
| One report looks wrong | run that folder's own `run-revapi.sh` |

The split matters: `revapi-config.json` is referenced **by path** and read at run time,
so changing it only requires re-running the analyses. The extension versions and the
command-line options are **baked into** each generated `run-revapi.sh`, so changing
those requires re-creating the scripts first.

### Always try a config change on one pair first

A full run takes about an hour. Never spend it to find out what a change does. Edit
`revapi-config.json`, then run **one** pair and inspect the result:

```
./run-revapi.sh 8.0.1 8.1.2
```

`8.0.1_8.1.2` is the best single probe: it carries the 190-strong `expand` migration,
and its `jersey3-client-v4` report is the one where third-party noise shows up first if
a filter stops working. Pick a second pair when the change targets something it does not
cover — `8.14.4_8.15.3` for the class-to-interface conversions, `8.39.3_8.40.4` for the
newest release.

Compare against the committed report before running anything else. `git diff` on that
one folder is the whole verification: an intended change shows up as the findings you
expected to disappear or appear, and nothing else. Only once that reads correctly is a
full `run-all-revapi.sh` worth starting.

### A run must produce no warnings

Any `WARN` from `run-revapi.sh` mentioning **deprecated**, **missing configuration** or
**obsolete** behaviour means revapi is not being driven the way it is meant to be, and
counts as a defect to fix — not as noise to live with. A clean run prints its `INFO`
lines and nothing else. Two that were fixed this way:

* `revapi.java.filter.packages` is deprecated. Its documented replacement,
  `revapi.filter` with the `java-package` matcher, is **not** equivalent — it lets
  method-level differences of non-matching classes through. Filtering on `archives`
  instead gives the intended result and no warning.
* `minSeverity` / `minCriticality` unset makes revapi fall back to behaviour it calls
  obsolete. Both reporters now get `-DminSeverity=POTENTIALLY_BREAKING` from the
  generated script.

Reporter options belong in the **generated command**, not in `revapi-config.json`.
Configuring `revapi.reporter.json` in the config file while the script also passes
`-Drevapi.reporter.json.output=...` creates a *second* reporter instance whose output
defaults to stdout — which the script redirects into `report.txt`, silently replacing
the text report with JSON.

Both `run-all-revapi.sh` and `recreate-all-run-revapi.sh` operate on the report folders
that already exist; neither discovers new releases. That is deliberate — adding a
release is an explicit `create-report-folder.sh` call.

## Conventions

* Report files *and* the generated `run-revapi.sh` scripts are generated output — never
  hand-edit them. Re-create or re-run instead.
* A full `run-all-revapi.sh` takes about an hour (~40s per artifact per pair). Always
  run it in the background, never in a foreground command with a timeout.
* When a change affects every report (config, extension versions, generated command),
  regenerate all folders in the same commit, so the tree is never half-migrated.

## Tooling notes

* `revapi/revapi.java` requires [JBang](https://www.jbang.dev/) on the `PATH` and pins
  `//JAVA 17`. It is a thin wrapper around `org.revapi.standalone.Main` and accepts
  exactly the same options as the `revapi.sh` script of the Revapi CLI distribution.
* The wrapper logs to `stderr` (not `stdout`, unlike `revapi.sh`) so that `stdout`
  carries only the text report and can be redirected to a file.
* The extension cache lives outside this repository, at `~/.revapi-cli/cache` by
  default; override with `-Drevapi.home`, `REVAPI_HOME`, or `-d`/`--cache-dir`.
* `create-report-folder.sh` needs Maven on the `PATH`; the generated `run-revapi.sh`
  scripts do not — they only read jars from the local Maven repository.
* Generated scripts refer to archives through `$MAVEN_REPO` (defaulting to
  `$HOME/.m2/repository`), never through an absolute machine-specific path, so they
  stay valid for other developers and in CI.

## How the analysis is scoped

Three deliberate decisions shape what ends up in the reports. All three were needed to
get from "mostly third-party noise" to "only `com.unblu.*` changes".

### 1. The classpath is resolved with Maven, not by Revapi

Revapi's own GAV resolution (`--old-gavs`/`--new-gavs`) uses
`ArtifactResolver.getRevapiDependencySelector`, which selects only the `compile` and
`provided` scopes. `models-v4` declares its Jackson dependencies with the `runtime`
scope, so those jars never reached the analysis classpath and every Jackson type
exposed in the API was reported as `java.missing.oldClass` / `java.missing.newClass`
(11 of 78 findings for `8.39.3` → `8.40.4`) — and, worse, types such as
`JsonSerializer` could not be resolved at all, which degrades the analysis of the
`com.unblu` classes that extend them.

`create-report-folder.sh` therefore resolves the full runtime classpath itself with
`mvn dependency:build-classpath -Dmdep.includeScope=runtime` and writes it into the
generated `run-revapi.sh` as `--old`/`--new` plus
`--old-supplementary`/`--new-supplementary`. Note that Revapi ignores `-s`/`-t` when
`-a`/`-b` are used, so the switch to file paths is what makes supplementary archives
possible at all — `--old-gavs` and `--old` cannot be combined to any effect, the file
paths simply win and the GAVs are ignored.

### 2. Only `com.unblu.*` is reported

`revapi-config.json` sets `revapi.java.filter.packages` to include only
`com\.unblu\..*`. Without it, `jersey3-client-v4` (whose Jackson/Jersey dependencies
are `compile` scope, so they *are* analysed) drowned the report in third-party churn —
66 of its 160 findings were `java.method.varargOverloadsOnlyDifferInVarargParameter`
inside `com.fasterxml.jackson.*`, caused purely by a Jackson upgrade between the two
Unblu releases.

Filtering only affects what is *reported*; the supplementary archives stay on the
compiler classpath, so type resolution is unaffected.

Revapi logs a deprecation warning for `revapi.java.filter.packages` in favour of
`revapi.filter` combined with the java matcher. The option still works and is used
deliberately; migrating it would change every report, so do it only together with a
full `--force` regeneration.

### 3. Descriptive and serialization-only annotations are ignored

`revapi-config.json` carries one `revapi.differences` rule per ignored annotation type,
each matching on the `annotationType` attachment of the `java.annotation.*` differences:

* `io.swagger.annotations.*` (`@ApiModel`, `@ApiModelProperty`, …) are purely
  descriptive — a change to one is equivalent to a javadoc update.
* `com.fasterxml.jackson.annotation.JsonPropertyOrder` only affects the order of the
  fields in the serialized JSON, which is not part of the API contract.

The rules are deliberately per-annotation-type rather than a blanket
`java.annotation.*` ignore. `com.fasterxml.jackson.annotation.JsonSubTypes` changes are
**kept** — they signal that a new subtype entered the model. Do not widen the rules to
all annotations.

### Consequences to be aware of

* `java.class.externalClassExposedInAPI` entries in the `jersey3-client-v4` reports are
  about `com.unblu.webapi.model.v4.*` classes: `models-v4` is a supplementary archive
  of `jersey3-client-v4`, so model types used in the client API are "external" to it.
  These are kept — they show which model types entered the client's API surface.
* `revapi-basic-features` provides the `revapi.differences` extension but ships inside
  the Revapi CLI itself. Listing it in `--extensions` loads it twice and fails the
  analysis with `IllegalStateException: Duplicate key regex`.
