///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.36.1
//SOURCES Util.java

/*
 * Registers a custom dialog bot, reads it back by id and by name, then deletes it.
 *
 * Targets the findings of revapi/8.36.1_8.37.2/jersey3-client-v4:
 *
 *   java.method.numberOfParametersChanged
 *   old: BotsApi::botsCreate(DialogBotData)
 *   new: BotsApi::botsCreate(DialogBotData, java.util.List<ExpandFields>)
 *   old: BotsApi::botsGetByName(java.lang.String)
 *   new: BotsApi::botsGetByName(java.lang.String, java.util.List<ExpandFields>)
 *   old: BotsApi::botsRead(java.lang.String)
 *   new: BotsApi::botsRead(java.lang.String, java.util.List<ExpandFields>)
 *   old: BotsApi::botsUpdate(DialogBotData)
 *   new: BotsApi::botsUpdate(DialogBotData, java.util.List<ExpandFields>)
 *   BINARY: BREAKING, SOURCE: BREAKING
 *
 * This is the change that made integrations built on 8.36.x stop compiling against
 * 8.37.x. At REST level the new 'expand' query parameter is optional and the change is
 * not breaking, which is why a 8.36.x client keeps working against a 8.37.x server. In
 * the generated java client no overload with the old parameter list was kept, so every
 * caller has to be recompiled. See the "Compatibility" section of the root README.
 *
 * This example compiles against 8.36.1 and no longer compiles when the dependency
 * above is changed to 8.37.2.
 *
 * Usage:
 *   ./CreateBotWithoutExpandParameter.java <server> --admin <username> --password <password>
 */

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.BotsApi;
import com.unblu.webapi.jersey.v4.api.PersonsApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.model.v4.CustomDialogBotData;
import com.unblu.webapi.model.v4.DialogBotData;
import com.unblu.webapi.model.v4.EAuthorizationRole;
import com.unblu.webapi.model.v4.EBotDialogFilter;
import com.unblu.webapi.model.v4.EBotDialogTimeoutBehavior;
import com.unblu.webapi.model.v4.EPersonType;
import com.unblu.webapi.model.v4.ERegistrationStatus;
import com.unblu.webapi.model.v4.EWebApiVersion;
import com.unblu.webapi.model.v4.PersonData;

public class CreateBotWithoutExpandParameter {

    private static final String SCRIPT_NAME = "CreateBotWithoutExpandParameter.java";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        PersonsApi personsApi = new PersonsApi(client);
        BotsApi botsApi = new BotsApi(client);

        // A bot registration needs the id of a person of type BOT.
        PersonData botPerson = createBotPerson(personsApi);
        System.out.println("Created bot person '" + botPerson.getUsername() + "' with id '" + botPerson.getId() + "'");

        String botName = "example-bot-" + Util.randomSuffix();

        // In 8.36.1 none of these three operations takes an 'expand' parameter.
        DialogBotData createdBot = botsApi.botsCreate(createBot(botName, botPerson.getId()));
        System.out.println("Created bot '" + createdBot.getName() + "' with id '" + createdBot.getId() + "'");

        DialogBotData botById = botsApi.botsRead(createdBot.getId());
        System.out.println("Read bot by id '" + botById.getId() + "', name: '" + botById.getName() + "'");

        DialogBotData botByName = botsApi.botsGetByName(botName);
        System.out.println("Read bot by name '" + botByName.getName() + "', id: '" + botByName.getId() + "'");

        CustomDialogBotData toUpdate = (CustomDialogBotData) botById;
        toUpdate.setDescription("Renamed dialog bot of the Example Company");
        DialogBotData updatedBot = botsApi.botsUpdate(toUpdate);
        System.out.println("Updated bot description of '" + updatedBot.getName() + "'");

        botsApi.botsDelete(createdBot.getId());
        System.out.println("Deleted bot '" + createdBot.getId() + "'");
    }

    private static CustomDialogBotData createBot(String botName, String botPersonId) {
        CustomDialogBotData bot = new CustomDialogBotData();
        bot.setName(botName);
        bot.setDescription("Dialog bot of the Example Company");
        bot.setBotPersonId(botPersonId);
        bot.setWebhookEndpoint("https://bot.example.com/webhook");
        bot.setWebhookStatus(ERegistrationStatus.INACTIVE);
        bot.setWebhookApiVersion(EWebApiVersion.V4);
        bot.setOutboundTimeoutMillis(3000L);
        bot.setOnboardingFilter(EBotDialogFilter.NONE);
        bot.setOnboardingOrder(10);
        bot.setOffboardingFilter(EBotDialogFilter.NONE);
        bot.setOffboardingOrder(10);
        bot.setReboardingEnabled(false);
        bot.setReboardingOrder(10);
        bot.setNeedsCounterpartPresence(false);
        bot.setOnTimeoutBehavior(EBotDialogTimeoutBehavior.HAND_OFF);
        return bot;
    }

    private static PersonData createBotPerson(PersonsApi personsApi) throws Exception {
        PersonData botPerson = new PersonData();
        botPerson.set$Type(PersonData.TypeEnum.PERSONDATA);
        botPerson.setPersonType(EPersonType.BOT);
        botPerson.setAuthorizationRole(EAuthorizationRole.REGISTERED_USER);
        botPerson.setSourceId("example-bot-person-" + Util.randomSuffix());
        botPerson.setUsername("example-bot-person-" + Util.randomSuffix());
        botPerson.setEmail("bot@example.com");
        return personsApi.personsCreateOrUpdateBot(botPerson, null);
    }
}
