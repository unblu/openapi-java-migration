///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.36.1
//SOURCES Util.java

/*
 * Registers a suggestion source, reads it back by id and by name, updates it and
 * deletes it. Also reads the agentic flow id of the Aria variants, and calls the
 * internal analytics operation.
 *
 * Targets the findings of revapi/8.36.1_8.37.2/jersey3-client-v4 and
 * revapi/8.36.1_8.37.2/models-v4:
 *
 *   java.method.numberOfParametersChanged  SuggestionSourcesApi::suggestionSourcesCreate,
 *                                          ::suggestionSourcesRead, ::suggestionSourcesGetByName,
 *                                          ::suggestionSourcesUpdate — plus WithHttpInfo (8)
 *   java.method.removed                    AriaDialogBotData::getAgenticFlowId() and friends (3)
 *   java.method.removed                    AriaSuggestionSourceData::getAgenticFlowId() and friends (3)
 *   java.method.removed                    AnalyticsDataApi::analyticsDataInternal() (2)
 *
 * The suggestion source operations gained an 'expand' parameter in 8.37.2, the agentic
 * flow id moved off the two Aria models, and the internal analytics operation was
 * removed. This example compiles against 8.36.1 and no longer compiles when the
 * dependency above is changed to 8.37.2.
 *
 * Usage:
 *   ./SuggestionSourcesApiWithoutExpandParameter.java <server> --admin <username> --password <password>
 */

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.AnalyticsDataApi;
import com.unblu.webapi.jersey.v4.api.SuggestionSourcesApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.jersey.v4.invoker.ApiException;
import com.unblu.webapi.jersey.v4.invoker.ApiResponse;
import com.unblu.webapi.model.v4.AriaDialogBotData;
import com.unblu.webapi.model.v4.AriaSuggestionSourceData;
import com.unblu.webapi.model.v4.CustomSuggestionSourceData;
import com.unblu.webapi.model.v4.EOutboundEndpointStatus;
import com.unblu.webapi.model.v4.EWebApiVersion;
import com.unblu.webapi.model.v4.SuggestionSourceData;

public class SuggestionSourcesApiWithoutExpandParameter {

    private static final String SCRIPT_NAME = "SuggestionSourcesApiWithoutExpandParameter.java";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        SuggestionSourcesApi suggestionSourcesApi = new SuggestionSourcesApi(client);

        // In 8.36.1 none of these four operations takes an 'expand' parameter.
        SuggestionSourceData created = suggestionSourcesApi.suggestionSourcesCreate(newSuggestionSource());
        System.out.println("Created suggestion source '" + created.getName() + "' (" + created.getId() + ")");

        ApiResponse<SuggestionSourceData> createResponse = suggestionSourcesApi.suggestionSourcesCreateWithHttpInfo(newSuggestionSource());
        System.out.println("Created another with status " + createResponse.getStatusCode());
        String secondId = createResponse.getData().getId();

        SuggestionSourceData read = suggestionSourcesApi.suggestionSourcesRead(created.getId());
        System.out.println("Read by id: '" + read.getName() + "'");

        ApiResponse<SuggestionSourceData> readResponse = suggestionSourcesApi.suggestionSourcesReadWithHttpInfo(created.getId());
        System.out.println("Read by id with status " + readResponse.getStatusCode());

        SuggestionSourceData byName = suggestionSourcesApi.suggestionSourcesGetByName(created.getName());
        System.out.println("Read by name: '" + byName.getName() + "'");

        ApiResponse<SuggestionSourceData> byNameResponse = suggestionSourcesApi.suggestionSourcesGetByNameWithHttpInfo(created.getName());
        System.out.println("Read by name with status " + byNameResponse.getStatusCode());

        ((CustomSuggestionSourceData) read).setDescription("Renamed suggestion source of the Example Company");
        SuggestionSourceData updated = suggestionSourcesApi.suggestionSourcesUpdate(read);
        System.out.println("Updated description of '" + updated.getName() + "'");

        ApiResponse<SuggestionSourceData> updateResponse = suggestionSourcesApi.suggestionSourcesUpdateWithHttpInfo(updated);
        System.out.println("Updated again with status " + updateResponse.getStatusCode());

        suggestionSourcesApi.suggestionSourcesDelete(secondId);
        suggestionSourcesApi.suggestionSourcesDelete(created.getId());
        System.out.println("Deleted both suggestion sources");

        readAgenticFlowIds();
        callInternalAnalytics(client);
    }

    /*
     * The agentic flow id sits directly on the two Aria models in 8.36.1 and is gone
     * from them in 8.37.2. Nothing is sent to the server here, the models are built and
     * read locally.
     */
    private static void readAgenticFlowIds() {
        AriaDialogBotData bot = new AriaDialogBotData();
        bot.setAgenticFlowId("example-agentic-flow");
        System.out.println("Aria bot agentic flow id: " + bot.getAgenticFlowId());

        AriaDialogBotData fluentBot = new AriaDialogBotData().agenticFlowId("example-other-flow");
        System.out.println("Second Aria bot agentic flow id: " + fluentBot.getAgenticFlowId());

        AriaSuggestionSourceData source = new AriaSuggestionSourceData();
        source.setAgenticFlowId("example-agentic-flow");
        System.out.println("Aria suggestion source agentic flow id: " + source.getAgenticFlowId());

        AriaSuggestionSourceData fluentSource = new AriaSuggestionSourceData().agenticFlowId("example-other-flow");
        System.out.println("Second Aria suggestion source agentic flow id: " + fluentSource.getAgenticFlowId());
    }

    /*
     * analyticsDataInternal was removed in 8.37.2. It is an internal operation and may
     * not be reachable, so a failure is reported rather than thrown.
     */
    private static void callInternalAnalytics(ApiClient client) {
        AnalyticsDataApi analyticsDataApi = new AnalyticsDataApi(client);
        try {
            analyticsDataApi.analyticsDataInternal();
            ApiResponse<Void> response = analyticsDataApi.analyticsDataInternalWithHttpInfo();
            System.out.println("Triggered the internal analytics operation, status " + response.getStatusCode());
        } catch (ApiException e) {
            System.out.println("analyticsDataInternal is not reachable here, got HTTP " + e.getCode());
        }
    }

    private static CustomSuggestionSourceData newSuggestionSource() {
        String unique = Util.randomSuffix();
        CustomSuggestionSourceData source = new CustomSuggestionSourceData();
        source.setName("example-suggestion-source-" + unique);
        source.setDescription("Suggestion source of the Example Company");
        source.setOutboundEndpoint("https://suggestions.example.com/webhook");
        source.setOutboundApiVersion(EWebApiVersion.V4);
        source.setOutboundStatus(EOutboundEndpointStatus.DISABLED);
        source.setOutboundTimeoutMillis(5000L);
        return source;
    }
}
