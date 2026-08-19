///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.0.1
//SOURCES Util.java

/*
 * Installs and removes a licence.
 *
 * Targets the findings of revapi/8.0.1_8.1.2/jersey3-client-v4 for GlobalApi:
 *
 *   java.method.parameterTypeChanged  (4 findings)
 *   old: GlobalApi::globalInstallLicense(java.lang.String, java.lang.String)
 *   new: GlobalApi::globalInstallLicense(java.lang.String, java.util.List<ExpandFields>)
 *   old: GlobalApi::globalRemoveLicense(java.lang.String)
 *   new: GlobalApi::globalRemoveLicense(java.util.List<ExpandFields>)
 *
 *   — and the WithHttpInfo variant of each.
 *
 * DESTRUCTIVE: installing or removing a licence changes what the whole server is
 * allowed to do. Do not run this against a shared environment. It is kept because the
 * two operations take the same 'expand' parameter as the rest of GlobalApi and the
 * change has to be demonstrated somewhere; the compile contract is what it is for.
 *
 * This example compiles against 8.0.1 and no longer compiles when the dependency above
 * is changed to 8.1.2.
 *
 * Usage:
 *   ./InstallLicenseWithExpandParameter.java <server> --superadmin <username> --password <password>
 */

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.GlobalApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.jersey.v4.invoker.ApiResponse;
import com.unblu.webapi.model.v4.GlobalData;

public class InstallLicenseWithExpandParameter {

    private static final String SCRIPT_NAME = "InstallLicenseWithExpandParameter.java";

    /** In 8.0.1 the fields to expand are given as a single comma separated String. */
    private static final String EXPAND = "configuration,text";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseSuperadminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        GlobalApi globalApi = new GlobalApi(client);

        String licence = "example-licence-key";

        GlobalData installed = globalApi.globalInstallLicense(licence, EXPAND);
        System.out.println("Installed a licence, server identifier '" + installed.getServerIdentifier() + "'");

        ApiResponse<GlobalData> installResponse = globalApi.globalInstallLicenseWithHttpInfo(licence, null);
        System.out.println("Installed again with status " + installResponse.getStatusCode());

        GlobalData removed = globalApi.globalRemoveLicense(EXPAND);
        System.out.println("Removed the licence, server identifier '" + removed.getServerIdentifier() + "'");

        ApiResponse<GlobalData> removeResponse = globalApi.globalRemoveLicenseWithHttpInfo(null);
        System.out.println("Removed again with status " + removeResponse.getStatusCode());
    }
}
