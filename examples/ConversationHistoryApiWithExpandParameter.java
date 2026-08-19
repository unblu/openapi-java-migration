///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.0.1
//SOURCES Util.java

/*
 * Exercises every ConversationHistoryApi operation that takes an 'expand' parameter.
 *
 * Targets the findings of revapi/8.0.1_8.1.2/jersey3-client-v4 for ConversationHistoryApi:
 *
 *   java.method.parameterTypeChanged  (4 findings)
 *   old: ...(<body>, java.lang.String)
 *   new: ...(<body>, java.util.List<ExpandFields>)
 *
 *   conversationHistoryRead, conversationHistorySearch — and the WithHttpInfo variant of
 *   each.
 *
 * In 8.0.1 'expand' is a comma separated String; from 8.1.2 on it is a
 * List<ExpandFields>. This example compiles against 8.0.1 and no longer compiles when
 * the dependency above is changed to 8.1.2.
 *
 * Usage:
 *   ./ConversationHistoryApiWithExpandParameter.java <server> --admin <username> --password <password>
 */

import java.util.List;
import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.ConversationHistoryApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.jersey.v4.invoker.ApiResponse;
import com.unblu.webapi.model.v4.ConversationHistoryData;
import com.unblu.webapi.model.v4.ConversationHistoryDataResult;
import com.unblu.webapi.model.v4.ConversationHistoryQuery;

public class ConversationHistoryApiWithExpandParameter {

    private static final String SCRIPT_NAME = "ConversationHistoryApiWithExpandParameter.java";

    /** In 8.0.1 the fields to expand are given as a single comma separated String. */
    private static final String EXPAND = "configuration,text";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        ConversationHistoryApi historyApi = new ConversationHistoryApi(client);

        ConversationHistoryDataResult searchResult = historyApi.conversationHistorySearch(new ConversationHistoryQuery(), EXPAND);
        List<ConversationHistoryData> conversations = searchResult.getItems();
        System.out.println("Search found " + conversations.size() + " past conversation(s)");

        ApiResponse<ConversationHistoryDataResult> searchResponse = historyApi.conversationHistorySearchWithHttpInfo(new ConversationHistoryQuery(), null);
        System.out.println("Search with status " + searchResponse.getStatusCode());

        if (conversations.isEmpty()) {
            System.out.println("No past conversation to read, nothing more to do");
            return;
        }

        String conversationId = conversations.get(0).getId();

        ConversationHistoryData read = historyApi.conversationHistoryRead(conversationId, "metadata");
        System.out.println("Read conversation '" + read.getId() + "', state " + read.getState());

        ApiResponse<ConversationHistoryData> readResponse = historyApi.conversationHistoryReadWithHttpInfo(conversationId, EXPAND);
        System.out.println("Read again with status " + readResponse.getStatusCode());
    }
}
