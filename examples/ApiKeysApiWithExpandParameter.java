///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.0.1
//SOURCES Util.java

/*
 * Exercises every ApiKeysApi operation that takes an 'expand' parameter.
 *
 * Targets the findings of revapi/8.0.1_8.1.2/jersey3-client-v4 for ApiKeysApi:
 *
 *   java.method.parameterTypeChanged  (14 findings)
 *   old: ApiKeysApi::apiKeysCreate(ApiKey, java.lang.String)
 *   new: ApiKeysApi::apiKeysCreate(ApiKey, java.util.List<ExpandFields>)
 *
 *   apiKeysCreate, apiKeysGetByKey, apiKeysGetDefault, apiKeysRead,
 *   apiKeysReadMultiple, apiKeysSearch, apiKeysUpdate — and the WithHttpInfo variant
 *   of each.
 *
 * In 8.0.1 'expand' is a comma separated String; from 8.1.2 on it is a
 * List<ExpandFields>. This example compiles against 8.0.1 and no longer compiles when
 * the dependency above is changed to 8.1.2.
 *
 * Usage:
 *   ./ApiKeysApiWithExpandParameter.java <server> --admin <username> --password <password>
 */

import java.util.Arrays;

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.ApiKeysApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.jersey.v4.invoker.ApiResponse;
import com.unblu.webapi.model.v4.ApiKey;
import com.unblu.webapi.model.v4.ApiKeyList;
import com.unblu.webapi.model.v4.ApiKeyQuery;
import com.unblu.webapi.model.v4.ApiKeyResult;

public class ApiKeysApiWithExpandParameter {

    private static final String SCRIPT_NAME = "ApiKeysApiWithExpandParameter.java";

    /** In 8.0.1 the fields to expand are given as a single comma separated String. */
    private static final String EXPAND = "configuration,text";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        ApiKeysApi apiKeysApi = new ApiKeysApi(client);

        ApiKey apiKey = new ApiKey();
        apiKey.set$Type(ApiKey.TypeEnum.APIKEY);
        apiKey.setName("example-api-key-" + Util.randomSuffix());
        apiKey.setDescription("API key of the Example Company integration");

        ApiKey createdApiKey = apiKeysApi.apiKeysCreate(apiKey, "configuration,text");
        System.out.println("Created api key '" + createdApiKey.getName() + "' (" + createdApiKey.getId() + ")");

        ApiResponse<ApiKey> createResponse = apiKeysApi.apiKeysCreateWithHttpInfo(apiKey, EXPAND);
        System.out.println("Created another with status " + createResponse.getStatusCode());
        String secondApiKeyId = createResponse.getData().getId();

        ApiKey readApiKey = apiKeysApi.apiKeysRead(createdApiKey.getId(), EXPAND);
        System.out.println("Read by id: '" + readApiKey.getName() + "'");

        ApiResponse<ApiKey> readResponse = apiKeysApi.apiKeysReadWithHttpInfo(createdApiKey.getId(), null);
        System.out.println("Read by id with status " + readResponse.getStatusCode());

        ApiKey byKey = apiKeysApi.apiKeysGetByKey(createdApiKey.getApiKey(), EXPAND);
        System.out.println("Read by key: '" + byKey.getName() + "'");

        ApiResponse<ApiKey> byKeyResponse = apiKeysApi.apiKeysGetByKeyWithHttpInfo(createdApiKey.getApiKey(), EXPAND);
        System.out.println("Read by key with status " + byKeyResponse.getStatusCode());

        ApiKey defaultApiKey = apiKeysApi.apiKeysGetDefault(EXPAND);
        System.out.println("Default api key: '" + (defaultApiKey == null ? "<none>" : defaultApiKey.getName()) + "'");

        ApiResponse<ApiKey> defaultResponse = apiKeysApi.apiKeysGetDefaultWithHttpInfo(EXPAND);
        System.out.println("Default api key with status " + defaultResponse.getStatusCode());

        ApiKeyList apiKeyList = apiKeysApi.apiKeysReadMultiple(Arrays.asList(createdApiKey.getId(), secondApiKeyId), EXPAND);
        System.out.println("Read multiple: " + apiKeyList.getItems().size() + " api key(s)");

        ApiResponse<ApiKeyList> apiKeyListResponse = apiKeysApi.apiKeysReadMultipleWithHttpInfo(Arrays.asList(createdApiKey.getId()), EXPAND);
        System.out.println("Read multiple with status " + apiKeyListResponse.getStatusCode());

        ApiKeyResult searchResult = apiKeysApi.apiKeysSearch(new ApiKeyQuery(), EXPAND);
        System.out.println("Search found " + searchResult.getItems().size() + " api key(s)");

        ApiResponse<ApiKeyResult> searchResponse = apiKeysApi.apiKeysSearchWithHttpInfo(new ApiKeyQuery(), EXPAND);
        System.out.println("Search with status " + searchResponse.getStatusCode());

        readApiKey.setDescription("Renamed api key of the Example Company");
        ApiKey updatedApiKey = apiKeysApi.apiKeysUpdate(readApiKey, EXPAND);
        System.out.println("Updated description to '" + updatedApiKey.getDescription() + "'");

        ApiResponse<ApiKey> updateResponse = apiKeysApi.apiKeysUpdateWithHttpInfo(updatedApiKey, EXPAND);
        System.out.println("Updated again with status " + updateResponse.getStatusCode());

        apiKeysApi.apiKeysDelete(secondApiKeyId);
        apiKeysApi.apiKeysDelete(createdApiKey.getId());
        System.out.println("Deleted both api keys");
    }
}
