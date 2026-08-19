///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.13.1
//SOURCES Util.java

/*
 * Registers a suggestion source, reads it back and deletes it again.
 *
 * Targets the findings of revapi/8.13.1_8.14.4/models-v4:
 *
 *   java.class.kindChanged         class SuggestionSourceData -> interface
 *   java.class.nowAbstract         class SuggestionSourceData
 *   java.class.removed             enum SuggestionSourceData.TypeEnum
 *   java.method.nowAbstract        SuggestionSourceData::getOutboundStatus(), ...
 *   java.field.removedWithConstant SuggestionSourceData.JSON_PROPERTY_$_TYPE, ...
 *
 * In 8.13.1 SuggestionSourceData is a *class*, from 8.14.4 on it is an *interface*
 * with one implementation per suggestion source type. This is the same conversion that
 * DialogBotData went through one release later, see
 * CreateDialogBotWithConcreteClass.java. For calling code it means that
 * 'new SuggestionSourceData()' no longer compiles. This example compiles against
 * 8.13.1 and no longer compiles when the dependency above is changed to 8.14.4.
 *
 * Usage:
 *   ./CreateSuggestionSourceWithConcreteClass.java <server> --admin <username> --password <password>
 */

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.SuggestionSourcesApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.model.v4.EOutboundEndpointStatus;
import com.unblu.webapi.model.v4.EWebApiVersion;
import com.unblu.webapi.model.v4.SuggestionSourceData;

public class CreateSuggestionSourceWithConcreteClass {

    private static final String SCRIPT_NAME = "CreateSuggestionSourceWithConcreteClass.java";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        SuggestionSourcesApi suggestionSourcesApi = new SuggestionSourcesApi(client);

        // In 8.13.1 SuggestionSourceData is a class and can be instantiated directly.
        SuggestionSourceData suggestionSource = new SuggestionSourceData();
        suggestionSource.setName("example-suggestion-source-" + Util.randomSuffix());
        suggestionSource.setDescription("Suggestion source of the Example Company");
        suggestionSource.setOutboundEndpoint("https://suggestions.example.com/webhook");
        suggestionSource.setOutboundApiVersion(EWebApiVersion.V4);
        suggestionSource.setOutboundStatus(EOutboundEndpointStatus.DISABLED);
        // The server requires an outbound timeout of at least 5000 ms.
        suggestionSource.setOutboundTimeoutMillis(5000L);

        SuggestionSourceData created = suggestionSourcesApi.suggestionSourcesCreate(suggestionSource);
        System.out.println("Created suggestion source '" + created.getName() + "' with id '" + created.getId() + "'");

        SuggestionSourceData read = suggestionSourcesApi.suggestionSourcesRead(created.getId());
        System.out.println("Read suggestion source '" + read.getName() + "', endpoint: '" + read.getOutboundEndpoint() + "'");

        suggestionSourcesApi.suggestionSourcesDelete(created.getId());
        System.out.println("Deleted suggestion source '" + created.getId() + "'");
    }
}
