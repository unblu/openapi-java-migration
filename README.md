# Unblu OpenAPI java migration

Tracks the changes of the java model published by the
[unblu/openapi](https://github.com/unblu/openapi) repository (branch `main/8.x.x`,
versions `8.x.x`).

The artifacts under observation are `com.unblu.openapi:jersey3-client-v4` and
`com.unblu.openapi:models-v4`.

**This repository does not contain the client itself** — the clients are generated and
released from [unblu/openapi](https://github.com/unblu/openapi). What lives here is the
record of what changes for a java caller between two releases of them, and the means to
deal with it.

## Goal of this repository

This is a workbench, not a released artifact. It exists to answer one question
precisely: **what actually changes for a java developer when the generated client moves
from one `8.x` version to the next?**

It grows in three steps.

1. **Measure.** [revapi/](revapi/) holds an API-change report for every pair of
   consecutive released versions, from `8.0.1` up. That is the raw record of what
   changed and how severely.

2. **Verify by experiment.** A report entry is a claim. [examples/](examples/) turns
   the interesting ones into small programs that prove the claim: each one compiles
   against the older client, fails to compile against the newer one, and — where the
   operation still exists — runs against a current server. Anything that turns out to
   compile against both versions is not a real break and is dropped rather than kept.

   These programs double as a growing test suite. Re-running them after a release says
   whether that release moved the java API, and re-reading the ones that no longer run
   says whether it moved the REST API as well, which is a different and more serious
   thing.

3. **Write it up** — [migration-notes/](migration-notes/) turns each verified break into
   a note a library user can act on: what changed, and the before/after for the code they
   have to touch. 18 of the 45 releases require a java change; the rest are a version
   bump, and the notes say so plainly.

4. **Automate the migration** *(next step, not built yet)*. Every verified break is a
   mechanical edit on the caller's side: add an `expand` argument, rename a class,
   replace a removed enum constant. The intent is to turn that catalogue into something
   consumers of the jersey client can simply apply to their own code — an
   [OpenRewrite](https://docs.openrewrite.org/) recipe per breaking change, and/or a
   description of the changes precise enough to hand to a coding agent so it can do the
   upgrade. The examples are what makes this possible: each one is a before/after pair
   with a known-good compile outcome on both sides, which is exactly the fixture such a
   recipe needs.

So the reports say *what* broke, the examples prove *how* it broke, and the recipes will
say *how to fix it*.

## Compatibility

The two things below are often confused with each other. They are not the same promise,
and only one of them is a promise at all.

### The REST API is kept backward compatible

Request and response bodies only change in non-breaking ways. A new query parameter is
optional, a new field is additive. **A client built against an older `8.x` version keeps
working against a newer `8.x` server**, which is why most of the programs in
[examples/](examples/) still run against a current server even though they are compiled
against a client as old as `8.0.1`.

### The generated java client is *not* kept backward compatible

The java client is generated from the specification. When a REST operation gains an
optional parameter, the generated method signature changes — no overload with the old
parameter list is kept. When a model is renamed in the specification, the generated
class is renamed too. Both are source- and binary-breaking for the calling code, even
though nothing about the REST API broke.

**There has never been a commitment to keep the generated java client
source-compatible across `8.x` versions.** It is kept as stable as is practical, but the
goal is a faithful generated client, not a frozen java API.

Two representative cases, both reproduced in [examples/](examples/):

| Release | What changed | Effect on the REST API | Effect on the java client |
|---|---|---|---|
| `8.37.2` | Several `BotsApi` operations gained an optional `expand` parameter | none, the parameter is optional | `botsCreate(DialogBotData)` became `botsCreate(DialogBotData, List<ExpandFields>)` — every caller has to be recompiled |
| `8.40.4` | The "person label" vocabulary was renamed to "label" | none, the JSON is unchanged | `PersonLabel` became `Label`, `EPersonLabelTargetType` became `ELabelTargetType`, and the old names were removed |

Client-breaking releases are rare — [revapi/](revapi/) is the record of exactly how rare
— but they do happen, and they are best planned for rather than promised away. Making
them cheap to absorb is what step 3 above is for.

## Layout

* [migration-notes/](migration-notes/) — **start here if you are upgrading.** One note per
  pair of consecutive releases: what changed for calling code, and what has to be done
  about it.
* [revapi/](revapi/) — the API-change reports between consecutive releases, and the
  tooling that produces them.
* [examples/](examples/) — small runnable programs that demonstrate the changes those
  reports describe.
