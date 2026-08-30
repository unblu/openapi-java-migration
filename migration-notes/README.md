# Migration notes

One note per pair of consecutive released versions of the Unblu java client, describing
what changed for calling code and what has to be done about it.

The clients are `com.unblu.openapi:jersey3-client-v4` and `com.unblu.openapi:models-v4`,
released from [unblu/openapi](https://github.com/unblu/openapi).

**The REST API is kept backward compatible; the generated java client is not.** A release
can leave the wire format untouched and still change a method signature or rename a
class, because the client is generated from the specification and no compatibility
overloads are kept. That is what these notes are for.

To upgrade across several releases, read every note between your current version and your
target: each one is independent and they compose.

| Pair | Java code must change | What changed |
|---|---|---|
| [8.0.1 → 8.1.2](8.0.1_8.1.2.md) | **yes** | The `expand` parameter changed type on every operation that has one. |
| [8.1.2 → 8.2.0](8.1.2_8.2.0.md) | — | no change |
| [8.2.0 → 8.3.1](8.2.0_8.3.1.md) | **yes** | Canned responses can no longer be searched on the `key` field. |
| [8.3.1 → 8.4.2](8.3.1_8.4.2.md) | **yes** | One offboarding reason was removed. |
| [8.4.2 → 8.5.0](8.4.2_8.5.0.md) | — | no change |
| [8.5.0 → 8.6.0](8.5.0_8.6.0.md) | — | no change |
| [8.6.0 → 8.6.1](8.6.0_8.6.1.md) | — | no change |
| [8.6.1 → 8.7.0](8.6.1_8.7.0.md) | **yes** | Persons can no longer be searched by label *name*. |
| [8.7.0 → 8.8.1](8.7.0_8.8.1.md) | — | no change |
| [8.8.1 → 8.8.2](8.8.1_8.8.2.md) | **yes** | The deputy escalation level enum was replaced. |
| [8.8.2 → 8.9.2](8.8.2_8.9.2.md) | **yes** | An external messenger channel property was removed. |
| [8.9.2 → 8.10.1](8.9.2_8.10.1.md) | — | no change |
| [8.10.1 → 8.11.1](8.10.1_8.11.1.md) | — | no change |
| [8.11.1 → 8.12.1](8.11.1_8.12.1.md) | — | no change |
| [8.12.1 → 8.13.1](8.12.1_8.13.1.md) | — | no change |
| [8.13.1 → 8.14.4](8.13.1_8.14.4.md) | **yes** | `SuggestionSourceData` became an interface. |
| [8.14.4 → 8.15.3](8.14.4_8.15.3.md) | **yes** | `DialogBotData` became an interface. |
| [8.15.3 → 8.16.2](8.15.3_8.16.2.md) | — | no change |
| [8.16.2 → 8.17.1](8.16.2_8.17.1.md) | **yes** | The `autoOpen` flag of a branch client link was split in three. |
| [8.17.1 → 8.18.0](8.17.1_8.18.0.md) | **yes** | The auxiliary camera type enum was renamed. |
| [8.18.0 → 8.19.1](8.18.0_8.19.1.md) | — | no change |
| [8.19.1 → 8.20.2](8.19.1_8.20.2.md) | — | no change |
| [8.20.2 → 8.21.0](8.20.2_8.21.0.md) | — | no change |
| [8.21.0 → 8.22.1](8.21.0_8.22.1.md) | **yes** | The branch operations gained an `expand` parameter, and the floor plan moved. |
| [8.22.1 → 8.23.2](8.22.1_8.23.2.md) | — | no change |
| [8.23.2 → 8.24.0](8.23.2_8.24.0.md) | — | no change |
| [8.24.0 → 8.25.1](8.24.0_8.25.1.md) | no | Documentation only. |
| [8.25.1 → 8.26.2](8.25.1_8.26.2.md) | **yes** | The "try a summary template" operation was removed. |
| [8.26.2 → 8.27.2](8.26.2_8.27.2.md) | **yes** | A message translation property was renamed. |
| [8.27.2 → 8.28.4](8.27.2_8.28.4.md) | no | Documentation only. |
| [8.28.4 → 8.29.1](8.28.4_8.29.1.md) | — | no change |
| [8.29.1 → 8.30.1](8.29.1_8.30.1.md) | — | no change |
| [8.30.1 → 8.31.2](8.30.1_8.31.2.md) | — | no change |
| [8.31.2 → 8.32.2](8.31.2_8.32.2.md) | **yes** | One message type was removed. |
| [8.32.2 → 8.32.3](8.32.2_8.32.3.md) | — | no change |
| [8.32.3 → 8.33.3](8.32.3_8.33.3.md) | — | no change |
| [8.33.3 → 8.34.1](8.33.3_8.34.1.md) | no | Documentation only. |
| [8.34.1 → 8.35.2](8.34.1_8.35.2.md) | — | no change |
| [8.35.2 → 8.36.1](8.35.2_8.36.1.md) | — | no change |
| [8.36.1 → 8.37.2](8.36.1_8.37.2.md) | **yes** | Bot and suggestion source operations gained an `expand` parameter; the bot webhook payload classes were removed. |
| [8.37.2 → 8.38.2](8.37.2_8.38.2.md) | **yes** | Two WhatsApp template enums were renamed to fix a spelling mistake. |
| [8.38.2 → 8.39.3](8.38.2_8.39.3.md) | — | no change |
| [8.39.3 → 8.40.4](8.39.3_8.40.4.md) | **yes** | The "person label" vocabulary was renamed to "label". |
| [8.40.4 → 8.41.4](8.40.4_8.41.4.md) | **yes** | The label selection enum of the person visibility rules was renamed. |

18 of the 44 releases require a change to java code. The remaining 26 are a version bump.

Each note links to the working examples in [`examples/`](../examples/) that demonstrate
the break: every one of them compiles against the older client and fails to compile
against the newer one, so the migration can be checked rather than assumed.
