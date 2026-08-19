///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.2.0
//SOURCES Util.java

/*
 * Creates a canned response, searches the canned responses on the 'key' field, then
 * deletes it again.
 *
 * Targets the findings of revapi/8.2.0_8.3.1/models-v4:
 *
 *   java.class.removed  class KeyCannedResponseSearchFilter
 *   java.field.removed  field ECannedResponseSearchFilterField.KEY
 *
 * Searching canned responses on their 'key' was dropped in 8.3.1: the dedicated filter
 * class and the enum constant selecting that field were both removed. Note that the
 * CannedResponse model of 8.2.0 has no 'key' property at all, so this filter could
 * never match anything. This example compiles against 8.2.0 and no longer compiles when
 * the dependency above is changed to 8.3.1.
 *
 * Usage:
 *   ./SearchCannedResponsesByKey.java <server> --admin <username> --password <password>
 */

import java.util.List;

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.AuthenticatorApi;
import com.unblu.webapi.jersey.v4.api.CannedResponsesApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.model.v4.CannedResponse;
import com.unblu.webapi.model.v4.CannedResponseQuery;
import com.unblu.webapi.model.v4.CannedResponseResult;
import com.unblu.webapi.model.v4.ECannedResponseSearchFilterField;
import com.unblu.webapi.model.v4.EPropertyOwnerType;
import com.unblu.webapi.model.v4.EqualsStringOperator;
import com.unblu.webapi.model.v4.KeyCannedResponseSearchFilter;
import com.unblu.webapi.model.v4.PersonData;

public class SearchCannedResponsesByKey {

    private static final String SCRIPT_NAME = "SearchCannedResponsesByKey.java";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        AuthenticatorApi authenticatorApi = new AuthenticatorApi(client);
        CannedResponsesApi cannedResponsesApi = new CannedResponsesApi(client);

        PersonData currentPerson = authenticatorApi.authenticatorGetCurrentPerson(null);
        String key = "example-greeting-" + Util.randomSuffix();

        CannedResponse cannedResponse = new CannedResponse();
        cannedResponse.setTitle("Greeting " + key);
        cannedResponse.setText("Welcome to the Example Company support desk.");
        cannedResponse.setOwnerId(currentPerson.getAccountId());
        cannedResponse.setOwnerType(EPropertyOwnerType.ACCOUNT);

        CannedResponse createdCannedResponse = cannedResponsesApi.cannedResponsesCreate(cannedResponse);
        System.out.println("Created canned response '" + createdCannedResponse.getTitle() + "' with id '" + createdCannedResponse.getId() + "'");

        // Both the filter and the enum constant it uses were removed in 8.3.1.
        KeyCannedResponseSearchFilter filter = new KeyCannedResponseSearchFilter()
                .field(ECannedResponseSearchFilterField.KEY)
                .operator(new EqualsStringOperator().value(key));

        CannedResponseQuery query = new CannedResponseQuery()
                .addSearchFiltersItem(filter);

        CannedResponseResult result = cannedResponsesApi.cannedResponsesSearch(query);

        List<CannedResponse> found = result.getItems();
        System.out.println("Search by key '" + key + "' returned " + found.size() + " canned response(s)");
        for (CannedResponse item : found) {
            System.out.println("- '" + item.getTitle() + "': " + item.getText());
        }

        cannedResponsesApi.cannedResponsesDelete(createdCannedResponse.getId());
        System.out.println("Deleted canned response '" + createdCannedResponse.getId() + "'");
    }
}
