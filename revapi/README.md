# Revapi API-change reports

[Revapi](https://revapi.org) analysis of the java artifacts published by the
[unblu/openapi](https://github.com/unblu/openapi) repository (branch `main/8.x.x`).

Two artifacts are tracked, both in the group `com.unblu.openapi`:

* `jersey3-client-v4`
* `models-v4`

## Reports

For each release, a folder `<N-1>_<N>` holds the API differences between the two
consecutive versions, one sub-folder per artifact:

```
<old version>_<new version>/
  jersey3-client-v4/
    run-revapi.sh  # the exact command that produced the two reports
    report.txt     # org.revapi:revapi-reporter-text
    report.json    # org.revapi:revapi-reporter-json
  models-v4/
    run-revapi.sh
    report.txt
    report.json
```

The text report is meant to be read, the JSON report is meant to be processed
(and produces a more precise diff when the reports are compared across releases).

Every report folder is self-describing: `run-revapi.sh` is the command that produced
the two reports next to it, with every classpath entry spelled out. Running it again
re-creates them.

## `revapi.java`

`revapi.java` is a [JBang](https://www.jbang.dev/) replacement for the `revapi.sh`
script shipped with the Revapi CLI distribution
([`org.revapi:revapi-standalone`](https://central.sonatype.com/artifact/org.revapi/revapi-standalone)).
It is a thin wrapper around `org.revapi.standalone.Main` and takes exactly the same
options as `revapi.sh`; because JBang resolves the CLI and its transitive dependencies
from Maven Central, the `lib/` folder of the distribution is not needed.

Run `./revapi.java --help` for the full option list. Example:

```
./revapi.java \
  --extensions=org.revapi:revapi-java:0.28.4,org.revapi:revapi-reporter-text:0.15.1 \
  --old-gavs com.unblu.openapi:models-v4:8.39.3 \
  --new-gavs com.unblu.openapi:models-v4:8.40.4 \
  -Drevapi.java.missing-classes.behavior=report
```

Two intentional differences from `revapi.sh`:

* Log messages go to `stderr` instead of `stdout`, and no `logs/revapi.log` file is
  written (see `logback.xml`). This keeps `stdout` free for the report itself, so it
  can be redirected to a file.
* `revapi.sh` uses its own installation directory as the parent of the extension cache.
  A JBang script has no installation directory, so the base directory is taken from the
  `revapi.home` system property, then the `REVAPI_HOME` environment variable, and
  defaults to `~/.revapi-cli` — which keeps the cache out of this git repository.
  As with `revapi.sh`, `-d`/`--cache-dir` overrides the cache location outright.

## Scripts

### After a new release

```
./create-report-folder.sh 8.40.4 8.41.0    # creates the folder and its run-revapi.sh
./run-revapi.sh 8.40.4 8.41.0              # produces report.txt and report.json
```

`create-report-folder.sh` resolves each artifact's classpath with Maven and writes it
into the generated `run-revapi.sh`; it does not run the analysis. By default both
artifacts are handled, artifact ids can be passed as additional arguments to restrict
it. The Revapi extension versions used are defined at the top of the script.

### After changing `revapi-config.json`

Try the change on a single pair first — a full run takes about an hour, which is too
long to spend finding out what a change does:

```
./run-revapi.sh 8.0.1 8.1.2      # then read `git diff` on that one folder
```

`8.0.1_8.1.2` is the most informative single probe: it carries the large `expand`
migration, and it is where third-party noise reappears first if a filter stops working.
Once that diff shows the findings you expected and nothing else:

```
./run-all-revapi.sh
```

The configuration file is referenced by path and read at run time, so only the reports
have to be produced again — the generated scripts stay valid.

A run must print no `WARN` about deprecated options, missing configuration or obsolete
behaviour. Such a warning means revapi is not being driven as intended, and is treated
as a defect rather than as noise.

### After changing `create-report-folder.sh`

```
./recreate-all-run-revapi.sh
./run-all-revapi.sh
```

The extension versions and the command-line options are baked into each generated
`run-revapi.sh`, so those have to be written again before the reports are refreshed.

Both `run-all-revapi.sh` and `recreate-all-run-revapi.sh` work on the report folders
that already exist; neither looks for new releases. Adding a release is always an
explicit `create-report-folder.sh` call.

A full `run-all-revapi.sh` takes about an hour.

## Scope of the analysis

The reports are meant to show what changed in the **`com.unblu.*` API**. Three settings
are needed to get there.

### The classpath is resolved with Maven

Revapi's own GAV resolution only considers the `compile` and `provided` scopes, but
`models-v4` declares its Jackson dependencies with the `runtime` scope. Those jars
never reached the analysis classpath, so Jackson types exposed in the API were reported
as `java.missing.oldClass` / `java.missing.newClass`, and types such as `JsonSerializer`
could not be resolved at all — which also degrades the analysis of the `com.unblu`
classes extending them.

`create-report-folder.sh` therefore resolves the full runtime classpath with
`mvn dependency:build-classpath -Dmdep.includeScope=runtime` and writes it into the
generated `run-revapi.sh`, which passes the artifact itself as `--old`/`--new` and the
remaining jars as `--old-supplementary`/`--new-supplementary`. Revapi also ignores
`-s`/`-t` when `-a`/`-b` are used, so the switch to file paths is what makes
supplementary archives possible at all.

### Only the Unblu artifacts are reported

`revapi-config.json` restricts `revapi.filter` to the archives whose file name matches
`(models-v4|jersey3-client-v4)-[0-9].*\.jar`. `jersey3-client-v4` declares its Jackson
and Jersey dependencies with the `compile` scope, so those classes *are* analysed;
without the filter, the bulk of its report was third-party churn from a Jackson upgrade
rather than anything Unblu changed.

The filter only affects what is reported — supplementary archives stay on the compiler
classpath, so type resolution is unaffected.

Filtering on archives rather than on packages is deliberate. The package-based
`revapi.java.filter.packages` is deprecated, and its documented replacement
(`revapi.filter` with the `java-package` matcher) is not equivalent: it lets
method-level differences of non-matching classes through, because that matcher only
judges type elements and each element is judged independently of its parent.

### What is ignored, and why

Through `revapi.differences` rules, each with its reason recorded in the report:

| Ignored | Reason |
|---|---|
| `io.swagger.annotations.*` changes | purely descriptive, equivalent to a javadoc update |
| `com.fasterxml.jackson.annotation.JsonPropertyOrder` changes | only affects field order in the serialized JSON |
| `java.class.externalClassExposedInAPI` | a model type used in a client signature is defined in `models-v4`; its changes are tracked in that report |
| `java.field.enumConstantOrderChanged` | enum constant order shifts on every insertion; the wire format uses the names |
| `java.field.removedWithConstant` on `JSON_PROPERTY_*` | a generation detail of the model classes, not API a client uses |
| `java.method.addedToInterface` | the model interfaces exist only to type the generated classes and are not meant to be implemented elsewhere |

`com.fasterxml.jackson.annotation.JsonSubTypes` changes are **kept**: they signal that a
new subtype entered the model.

### What is re-classified

| Code | Change | Reason |
|---|---|---|
| `java.method.parameterTypeChanged` | `SOURCE` → `BREAKING` | a changed parameter type means the call no longer compiles |
| `java.element.nowDeprecated` | `SEMANTIC` → `POTENTIALLY_BREAKING` | a deprecation announces a removal, which a caller planning an upgrade needs to see |

Both reporters run with `minSeverity=POTENTIALLY_BREAKING`, which keeps everything that
can affect a caller and drops purely additive changes such as `java.method.added`. The
`nowDeprecated` re-classification above is what keeps deprecations above that threshold.
