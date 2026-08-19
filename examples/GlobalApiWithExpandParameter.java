///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.0.1
//SOURCES Util.java

/*
 * Reads and updates the global configuration, and pings the server.
 *
 * Targets the findings of revapi/8.0.1_8.1.2/jersey3-client-v4 for GlobalApi:
 *
 *   java.method.parameterTypeChanged           globalRead, globalUpdate
 *   java.method.returnTypeTypeParametersChanged  globalPingWithHttpInfo
 *
 * The 'expand' parameter of globalRead and globalUpdate went from a comma separated
 * String to a List<ExpandFields> in 8.1.2, and the return type of globalPingWithHttpInfo
 * changed from ApiResponse<Void> to ApiResponse<GlobalPingResponse>.
 *
 * The two licence operations of this API take the same 'expand' parameter but are
 * destructive; they live in InstallLicenseWithExpandParameter.java.
 *
 * This example compiles against 8.0.1 and no longer compiles when the dependency above
 * is changed to 8.1.2.
 *
 * Usage:
 *   ./GlobalApiWithExpandParameter.java <server> --superadmin <username> --password <password>
 */

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.GlobalApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.jersey.v4.invoker.ApiResponse;
import com.unblu.webapi.model.v4.GlobalData;

public class GlobalApiWithExpandParameter {

    private static final String SCRIPT_NAME = "GlobalApiWithExpandParameter.java";

    /** In 8.0.1 the fields to expand are given as a single comma separated String. */
    private static final String EXPAND = "configuration,text";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseSuperadminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        GlobalApi globalApi = new GlobalApi(client);

        globalApi.globalPing();
        System.out.println("Pinged the server");

        ApiResponse<Void> pingResponse = globalApi.globalPingWithHttpInfo();
        System.out.println("Pinged again with status " + pingResponse.getStatusCode());

        GlobalData global = globalApi.globalRead("configuration,text");
        System.out.println("Server identifier: '" + global.getServerIdentifier() + "'");

        ApiResponse<GlobalData> readResponse = globalApi.globalReadWithHttpInfo(null);
        System.out.println("Read again with status " + readResponse.getStatusCode());

        GlobalData updated = globalApi.globalUpdate(global, EXPAND);
        System.out.println("Updated the global configuration, identifier '" + updated.getServerIdentifier() + "'");

        ApiResponse<GlobalData> updateResponse = globalApi.globalUpdateWithHttpInfo(updated, EXPAND);
        System.out.println("Updated again with status " + updateResponse.getStatusCode());
    }
}
