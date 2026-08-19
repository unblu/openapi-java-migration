///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.14.4
//SOURCES Util.java

/*
 * Creates a dialog bot registration, reads it back and deletes it again.
 *
 * Targets the findings of revapi/8.14.4_8.15.3/models-v4:
 *
 *   java.class.kindChanged         class DialogBotData -> interface DialogBotData
 *   java.class.nowAbstract         class DialogBotData
 *   java.method.nowAbstract        DialogBotData::getAccountId(), ::setName(..), ...
 *   java.field.removedWithConstant DialogBotData.JSON_PROPERTY_$_TYPE, ...
 *
 * In 8.14.4 DialogBotData is a *class*, from 8.15.3 on it is an *interface*. The kind
 * change is the actual finding; the 51 'nowAbstract' and 24 'removedWithConstant'
 * findings of that report are all consequences of it. What it means for calling code
 * is that 'new DialogBotData()' no longer compiles. This example compiles against
 * 8.14.4 and no longer compiles when the dependency above is changed to 8.15.3.
 *
 * Usage:
 *   ./CreateDialogBotWithConcreteClass.java <server> --admin <username> --password <password>
 */

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.BotsApi;
import com.unblu.webapi.jersey.v4.api.PersonsApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.model.v4.DialogBotData;
import com.unblu.webapi.model.v4.EAuthorizationRole;
import com.unblu.webapi.model.v4.EBotDialogFilter;
import com.unblu.webapi.model.v4.EBotDialogTimeoutBehavior;
import com.unblu.webapi.model.v4.EPersonType;
import com.unblu.webapi.model.v4.ERegistrationStatus;
import com.unblu.webapi.model.v4.EWebApiVersion;
import com.unblu.webapi.model.v4.PersonData;

public class CreateDialogBotWithConcreteClass {

    private static final String SCRIPT_NAME = "CreateDialogBotWithConcreteClass.java";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        PersonsApi personsApi = new PersonsApi(client);
        BotsApi botsApi = new BotsApi(client);

        // A bot registration needs the id of a person of type BOT.
        PersonData botPerson = createBotPerson(personsApi);
        System.out.println("Created bot person '" + botPerson.getUsername() + "' with id '" + botPerson.getId() + "'");

        // In 8.14.4 DialogBotData is a class and can be instantiated directly.
        DialogBotData bot = new DialogBotData();
        bot.setName("example-bot-" + Util.randomSuffix());
        bot.setDescription("Dialog bot of the Example Company");
        bot.setBotPersonId(botPerson.getId());
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

        DialogBotData createdBot = botsApi.botsCreate(bot);
        System.out.println("Created bot '" + createdBot.getName() + "' with id '" + createdBot.getId() + "'");

        DialogBotData readBot = botsApi.botsRead(createdBot.getId());
        System.out.println("Read bot '" + readBot.getName() + "', endpoint: '" + readBot.getWebhookEndpoint() + "'");

        botsApi.botsDelete(createdBot.getId());
        System.out.println("Deleted bot '" + createdBot.getId() + "'");
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
