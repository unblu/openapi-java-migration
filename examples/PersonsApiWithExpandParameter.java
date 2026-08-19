///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.0.1
//SOURCES Util.java

/*
 * Exercises every PersonsApi operation that takes an 'expand' parameter.
 *
 * Targets the findings of revapi/8.0.1_8.1.2/jersey3-client-v4 for PersonsApi:
 *
 *   java.method.parameterTypeChanged  (22 findings)
 *   old: ...(<body>, java.lang.String)
 *   new: ...(<body>, java.util.List<ExpandFields>)
 *
 *   personsAddLabels, personsCreateOrUpdateBot, personsCreateOrUpdateVirtual,
 *   personsGetBySource, personsRead, personsRemoveLabels, personsSearch,
 *   personsSearchAgents, personsSearchBots, personsSearchVisitors, personsSetLabels
 *   — and the WithHttpInfo variant of each.
 *
 * In 8.0.1 'expand' is a comma separated String; from 8.1.2 on it is a
 * List<ExpandFields>. This example compiles against 8.0.1 and no longer compiles when
 * the dependency above is changed to 8.1.2.
 *
 * Usage:
 *   ./PersonsApiWithExpandParameter.java <server> --admin <username> --password <password>
 */

import java.util.Arrays;
import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.PersonLabelsApi;
import com.unblu.webapi.jersey.v4.api.PersonsApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.jersey.v4.invoker.ApiResponse;
import com.unblu.webapi.model.v4.EAuthorizationRole;
import com.unblu.webapi.model.v4.EPersonSource;
import com.unblu.webapi.model.v4.EPersonType;
import com.unblu.webapi.model.v4.EPersonLabelTargetType;
import com.unblu.webapi.model.v4.PersonData;
import com.unblu.webapi.model.v4.PersonLabel;
import com.unblu.webapi.model.v4.PersonQuery;
import com.unblu.webapi.model.v4.PersonResult;
import com.unblu.webapi.model.v4.PersonTypedQuery;

public class PersonsApiWithExpandParameter {

    private static final String SCRIPT_NAME = "PersonsApiWithExpandParameter.java";

    /** In 8.0.1 the fields to expand are given as a single comma separated String. */
    private static final String EXPAND = "configuration,text";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        PersonsApi personsApi = new PersonsApi(client);
        PersonLabelsApi personLabelsApi = new PersonLabelsApi(client);

        // The label operations below refuse names that do not exist yet.
        String firstLabel = createLabel(personLabelsApi);
        String secondLabel = createLabel(personLabelsApi);
        String thirdLabel = createLabel(personLabelsApi);

        PersonData bot = personsApi.personsCreateOrUpdateBot(newPerson(EPersonType.BOT), "avatar");
        System.out.println("Created bot person '" + bot.getUsername() + "' (" + bot.getId() + ")");

        ApiResponse<PersonData> botResponse = personsApi.personsCreateOrUpdateBotWithHttpInfo(newPerson(EPersonType.BOT), null);
        System.out.println("Created another bot person with status " + botResponse.getStatusCode());

        PersonData virtual = personsApi.personsCreateOrUpdateVirtual(newPerson(EPersonType.AGENT), EXPAND);
        System.out.println("Created virtual person '" + virtual.getUsername() + "' (" + virtual.getId() + ")");

        ApiResponse<PersonData> virtualResponse = personsApi.personsCreateOrUpdateVirtualWithHttpInfo(newPerson(EPersonType.AGENT), null);
        System.out.println("Created another virtual person with status " + virtualResponse.getStatusCode());

        PersonData read = personsApi.personsRead(bot.getId(), EXPAND);
        System.out.println("Read by id: '" + read.getDisplayName() + "'");

        ApiResponse<PersonData> readResponse = personsApi.personsReadWithHttpInfo(bot.getId(), null);
        System.out.println("Read by id with status " + readResponse.getStatusCode());

        PersonData bySource = personsApi.personsGetBySource(EPersonSource.VIRTUAL, virtual.getSourceId(), "avatar,metadata");
        System.out.println("Read by source: '" + bySource.getDisplayName() + "'");

        ApiResponse<PersonData> bySourceResponse = personsApi.personsGetBySourceWithHttpInfo(EPersonSource.VIRTUAL, virtual.getSourceId(), null);
        System.out.println("Read by source with status " + bySourceResponse.getStatusCode());

        PersonResult searchResult = personsApi.personsSearch(new PersonQuery(), EXPAND);
        System.out.println("Search found " + searchResult.getItems().size() + " person(s)");

        ApiResponse<PersonResult> searchResponse = personsApi.personsSearchWithHttpInfo(new PersonQuery(), null);
        System.out.println("Search with status " + searchResponse.getStatusCode());

        PersonResult agents = personsApi.personsSearchAgents(new PersonTypedQuery(), EXPAND);
        System.out.println("Agent search found " + agents.getItems().size() + " agent(s)");

        ApiResponse<PersonResult> agentsResponse = personsApi.personsSearchAgentsWithHttpInfo(new PersonTypedQuery(), null);
        System.out.println("Agent search with status " + agentsResponse.getStatusCode());

        PersonResult bots = personsApi.personsSearchBots(new PersonTypedQuery(), EXPAND);
        System.out.println("Bot search found " + bots.getItems().size() + " bot(s)");

        ApiResponse<PersonResult> botsResponse = personsApi.personsSearchBotsWithHttpInfo(new PersonTypedQuery(), null);
        System.out.println("Bot search with status " + botsResponse.getStatusCode());

        PersonResult visitors = personsApi.personsSearchVisitors(new PersonTypedQuery(), EXPAND);
        System.out.println("Visitor search found " + visitors.getItems().size() + " visitor(s)");

        ApiResponse<PersonResult> visitorsResponse = personsApi.personsSearchVisitorsWithHttpInfo(new PersonTypedQuery(), null);
        System.out.println("Visitor search with status " + visitorsResponse.getStatusCode());

        PersonData labelled = personsApi.personsSetLabels(bot.getId(), Arrays.asList(firstLabel), EXPAND);
        System.out.println("Set labels on '" + labelled.getDisplayName() + "'");

        ApiResponse<PersonData> setLabelsResponse = personsApi.personsSetLabelsWithHttpInfo(bot.getId(), Arrays.asList(firstLabel), null);
        System.out.println("Set labels again with status " + setLabelsResponse.getStatusCode());

        PersonData added = personsApi.personsAddLabels(bot.getId(), Arrays.asList(secondLabel), EXPAND);
        System.out.println("Added a label to '" + added.getDisplayName() + "'");

        ApiResponse<PersonData> addLabelsResponse = personsApi.personsAddLabelsWithHttpInfo(bot.getId(), Arrays.asList(thirdLabel), null);
        System.out.println("Added another label with status " + addLabelsResponse.getStatusCode());

        PersonData removed = personsApi.personsRemoveLabels(bot.getId(), Arrays.asList(secondLabel), EXPAND);
        System.out.println("Removed a label from '" + removed.getDisplayName() + "'");

        ApiResponse<PersonData> removeLabelsResponse = personsApi.personsRemoveLabelsWithHttpInfo(bot.getId(), Arrays.asList(thirdLabel), null);
        System.out.println("Removed another label with status " + removeLabelsResponse.getStatusCode());
    }

    private static String createLabel(PersonLabelsApi personLabelsApi) throws Exception {
        PersonLabel label = new PersonLabel();
        label.setName("example-label-" + Util.randomSuffix());
        label.setDescription("Label of the Example Company");
        label.setColor("white");
        label.setSettableOn(Arrays.asList(EPersonLabelTargetType.AGENT, EPersonLabelTargetType.BOT));
        return personLabelsApi.personLabelsCreate(label).getName();
    }

    private static PersonData newPerson(EPersonType personType) {
        String unique = Util.randomSuffix();
        PersonData person = new PersonData();
        person.set$Type(PersonData.TypeEnum.PERSONDATA);
        person.setPersonType(personType);
        person.setAuthorizationRole(EAuthorizationRole.REGISTERED_USER);
        person.setSourceId("example-person-" + unique);
        person.setUsername("example-person-" + unique);
        person.setEmail("jane.smith+" + unique + "@example.com");
        return person;
    }
}
