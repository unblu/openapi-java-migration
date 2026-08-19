# Examples

Small [JBang](https://www.jbang.dev/) programs that put the findings of the
[revapi reports](../revapi/) to the test.

Each example is written against the **FROM** version of a `<FROM>_<TO>` report folder
and exercises an API that the **TO** version changed in an incompatible way. The
contract of every example in this folder is:

* it **compiles** with `//DEPS com.unblu.openapi:jersey3-client-v4:<FROM>`
* it **does not compile** any more when that line is changed to `<TO>`

An example that compiles with both versions does not demonstrate anything and is not
kept here.

The header comment of each example names the report folder and the revapi finding codes
it targets, so a finding can be traced from the report to the code that breaks on it.

Only the v4 API is covered. `models-v4` is a transitive dependency of
`jersey3-client-v4`, so examples targeting a `models-v4` finding also declare
`jersey3-client-v4` as their dependency.

## Running an example

Every example takes the same arguments:

```
./SomeExample.java <server> --superadmin <username> --password <password>
./SomeExample.java <server> --admin <username> --password <password>
```

where `<server>` is the url of the collaboration server including the web application
path, for example `https://unblu.example.com/app`. Whether an example needs a super
admin or an account admin is stated in its header comment and in the table below.

The argument parsing lives in [Util.java](Util.java), which every example pulls in with
`//SOURCES Util.java`. It uses no type of the generated client, so that the same file
works for every client version.

## The examples

34 programs covering **every one of the 593 `SOURCE`/`BINARY: BREAKING` findings** in the
reports. "Findings" is how many that example is the primary cover for; "Runs" says
whether it completes against a current server.

| Pair | Example | Findings | Runs | What broke |
|---|---|---|---|---|
| `8.0.1_8.1.2` | [AccountsApiWithExpandParameter.java](AccountsApiWithExpandParameter.java) | 16 | yes | `parameterTypeChanged` — the `expand` parameter of every operation went from `String` to `List<ExpandFields>` |
| `8.0.1_8.1.2` | [ApiKeysApiWithExpandParameter.java](ApiKeysApiWithExpandParameter.java) | 14 | yes | same `expand` migration |
| `8.0.1_8.1.2` | [AuthenticatorApiWithExpandParameter.java](AuthenticatorApiWithExpandParameter.java) | 2 | yes | same `expand` migration |
| `8.0.1_8.1.2` | [ConversationHistoryApiWithExpandParameter.java](ConversationHistoryApiWithExpandParameter.java) | 4 | yes | same `expand` migration |
| `8.0.1_8.1.2` | [ConversationTemplatesApiWithExpandParameter.java](ConversationTemplatesApiWithExpandParameter.java) | 12 | yes | same `expand` migration |
| `8.0.1_8.1.2` | [ConversationsApiWithExpandParameter.java](ConversationsApiWithExpandParameter.java) | 44 | yes | same `expand` migration — the largest single API |
| `8.0.1_8.1.2` | [CreateTeamWithExpandParameter.java](CreateTeamWithExpandParameter.java) | — | yes | the minimal illustration of the `expand` migration |
| `8.0.1_8.1.2` | [CustomActionsApiWithExpandParameter.java](CustomActionsApiWithExpandParameter.java) | 6 | yes | same `expand` migration |
| `8.0.1_8.1.2` | [ExternalMessengersApiWithExpandParameter.java](ExternalMessengersApiWithExpandParameter.java) | 14 | yes | same `expand` migration |
| `8.0.1_8.1.2` | [GlobalApiWithExpandParameter.java](GlobalApiWithExpandParameter.java) | 6 | yes | `expand` migration plus `returnTypeTypeParametersChanged` on `globalPingWithHttpInfo` |
| `8.0.1_8.1.2` | [InstallLicenseWithExpandParameter.java](InstallLicenseWithExpandParameter.java) | 4 | **destructive** | the two licence operations of `GlobalApi` |
| `8.0.1_8.1.2` | [NamedAreasApiWithExpandParameter.java](NamedAreasApiWithExpandParameter.java) | 10 | yes | same `expand` migration |
| `8.0.1_8.1.2` | [PersonsApiWithExpandParameter.java](PersonsApiWithExpandParameter.java) | 22 | yes | same `expand` migration |
| `8.0.1_8.1.2` | [TeamsApiWithExpandParameter.java](TeamsApiWithExpandParameter.java) | 14 | yes | same `expand` migration |
| `8.0.1_8.1.2` | [UsersApiWithExpandParameter.java](UsersApiWithExpandParameter.java) | 24 | yes | same `expand` migration |
| `8.2.0_8.3.1` | [SearchCannedResponsesByKey.java](SearchCannedResponsesByKey.java) | 3 | **no** | `class.removed`, `field.removed` — the `KEY` canned-response filter |
| `8.3.1_8.4.2` | [HandleExternalChannelOffboardingReason.java](HandleExternalChannelOffboardingReason.java) | 1 | yes | `field.removed` — `EOffboardingReason.EXTERNAL_CHANNEL_CLOSED` |
| `8.6.1_8.7.0` | [SearchPersonsByLabelName.java](SearchPersonsByLabelName.java) | 7 | **no** | `class.removed`, `field.removed` — searching persons by label *name* |
| `8.8.1_8.8.2` | [SearchDeputiesByEscalationLevel.java](SearchDeputiesByEscalationLevel.java) | 16 | yes | the deputy escalation-level enum was replaced everywhere it appeared |
| `8.8.2_8.9.2` | [CreateMessengerChannelWithMultiConversationFlag.java](CreateMessengerChannelWithMultiConversationFlag.java) | 6 | yes | `method.removed` — `supportsMultipleConversationsPerContact` |
| `8.13.1_8.14.4` | [CreateSuggestionSourceWithConcreteClass.java](CreateSuggestionSourceWithConcreteClass.java) | 85 | yes | `class.kindChanged` — `SuggestionSourceData` became an interface |
| `8.14.4_8.15.3` | [CreateDialogBotWithConcreteClass.java](CreateDialogBotWithConcreteClass.java) | 151 | yes | `class.kindChanged` — `DialogBotData` became an interface |
| `8.16.2_8.17.1` | [CreateBranchClientLinkWithAutoOpen.java](CreateBranchClientLinkWithAutoOpen.java) | 6 | yes* | `method.removed` — `autoOpen` split into three properties |
| `8.17.1_8.18.0` | [CreateBranchClientAuxiliaryCamera.java](CreateBranchClientAuxiliaryCamera.java) | 7 | yes | `class.removed` — `EBranchClientAuxiliaryCameraType` renamed |
| `8.21.0_8.22.1` | [ReadBranchWithoutExpandParameter.java](ReadBranchWithoutExpandParameter.java) | 18 | yes | `numberOfParametersChanged` — branch operations gained `expand` |
| `8.25.1_8.26.2` | [GenerateSummaryTryForTemplate.java](GenerateSummaryTryForTemplate.java) | 5 | **no** | the "try a summary template" operation and its body class |
| `8.26.2_8.27.2` | [ReadTextMessageTranslation.java](ReadTextMessageTranslation.java) | 3 | yes | `method.removed` — `getTranslatedText()` renamed to `getText()` |
| `8.31.2_8.32.2` | [FilterMessagesByWhatsAppTemplateType.java](FilterMessagesByWhatsAppTemplateType.java) | 2 | yes | `field.removed` — `EMessageType.WHATSAPP_TEMPLATE` |
| `8.36.1_8.37.2` | [CreateBotWithoutExpandParameter.java](CreateBotWithoutExpandParameter.java) | 10 | yes | `numberOfParametersChanged` — `BotsApi` operations gained `expand` |
| `8.36.1_8.37.2` | [DeserializeBotDialogEvents.java](DeserializeBotDialogEvents.java) | 8 | yes | `class.removed` — the eight bot webhook event classes |
| `8.36.1_8.37.2` | [SuggestionSourcesApiWithoutExpandParameter.java](SuggestionSourcesApiWithoutExpandParameter.java) | 14 | yes | `SuggestionSourcesApi` gained `expand`; the Aria agentic flow id and the internal analytics operation were removed |
| `8.37.2_8.38.2` | [BuildWhatsAppTemplateComponentParameters.java](BuildWhatsAppTemplateComponentParameters.java) | 8 | yes | `class.removed` — the two template-parameter enums, renamed to fix a typo |
| `8.39.3_8.40.4` | [CreatePersonLabel.java](CreatePersonLabel.java) | 27 | yes | `class.removed` — the "person label" vocabulary renamed to "label" |
| `8.39.3_8.40.4` | [SearchPersonsByLabels.java](SearchPersonsByLabels.java) | 24 | yes | the label search filters and operators, and the models carrying labels |

\* the request is accepted, but a current server ignores the `autoOpen` value and
returns it as `null`.

### Deserialization settings

Every example that builds a client calls

```java
Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
```

which sets `FAIL_ON_UNKNOWN_PROPERTIES=false` and
`READ_UNKNOWN_ENUM_VALUES_AS_NULL=true`. Without the second one, several of these
examples cannot run at all: the generated enums are closed, so a value added to an enum
after the client was generated makes the whole response fail to deserialize rather than
just that field. `ConversationsApiWithExpandParameter` is the clearest case — every
conversation payload carries a link type that did not exist in 8.0.1.

The clients set both flags themselves from **8.13.1** onward, so the call is a no-op for
the newer examples and is what makes the older ones runnable. Note that the setting
discards the unknown value rather than recovering it, so code that reads an enum has to
be ready for `null`, and code that round-trips data can silently write the `null` back.

### The ones that do not run

Four of the 34:

* **`GenerateSummaryTryForTemplate`**, **`SearchCannedResponsesByKey`**,
  **`SearchPersonsByLabelName`** — the value each one sends was removed from the set the
  server accepts at the same time as from the client, so the request comes back `400`.
  Nothing on the client side can fix that; the failure is the finding.
* **`InstallLicenseWithExpandParameter`** — installing or removing a licence changes what
  the whole server may do. Never run it against a shared environment.

Their compile contract is verified in both directions like every other example, which is
what they are here for.

## Checking the contract of an example

There is no script for this yet. To check an example by hand, build it as it is, then
build a copy with the `TO` version substituted in its `//DEPS` line:

```
jbang build CreateTeamWithExpandParameter.java                 # must succeed

sed 's|jersey3-client-v4:8.0.1|jersey3-client-v4:8.1.2|' \
    CreateTeamWithExpandParameter.java > /tmp/Broken.java
cp Util.java /tmp/
jbang build /tmp/Broken.java                                   # must fail
```

## Conventions for new examples

* Name the file after what it does, not after the versions it compares.
* Keep the cognitive complexity low: one linear `main` method, private helpers for the
  set-up of the request bodies. Being long is fine, being clever is not.
* Follow the same shape as the existing examples — create, read back, delete — so that
  a run leaves no leftovers on the server.
* Everything in this folder is public. Use no real host names, no real credentials, no
  customer names and no real person names. `Example Company`, `example.com` addresses
  and `https://unblu.example.com/app` are the placeholders used throughout.
