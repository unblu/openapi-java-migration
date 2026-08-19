///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.0.1
//SOURCES Util.java

/*
 * Exercises every AuthenticatorApi operation that takes an 'expand' parameter.
 *
 * Targets the findings of revapi/8.0.1_8.1.2/jersey3-client-v4 for AuthenticatorApi:
 *
 *   java.method.parameterTypeChanged  (2 findings)
 *   old: ...(<body>, java.lang.String)
 *   new: ...(<body>, java.util.List<ExpandFields>)
 *
 *   authenticatorGetCurrentPerson — and its WithHttpInfo variant.
 *
 * In 8.0.1 'expand' is a comma separated String; from 8.1.2 on it is a
 * List<ExpandFields>. This example compiles against 8.0.1 and no longer compiles when
 * the dependency above is changed to 8.1.2.
 *
 * Usage:
 *   ./AuthenticatorApiWithExpandParameter.java <server> --admin <username> --password <password>
 */

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.AuthenticatorApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.jersey.v4.invoker.ApiResponse;
import com.unblu.webapi.model.v4.PersonData;

public class AuthenticatorApiWithExpandParameter {

    private static final String SCRIPT_NAME = "AuthenticatorApiWithExpandParameter.java";

    /** In 8.0.1 the fields to expand are given as a single comma separated String. */
    private static final String EXPAND = "configuration,text";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        AuthenticatorApi authenticatorApi = new AuthenticatorApi(client);

        PersonData currentPerson = authenticatorApi.authenticatorGetCurrentPerson("avatar");
        System.out.println("Current person: '" + currentPerson.getDisplayName() + "' (" + currentPerson.getPersonType() + ")");

        ApiResponse<PersonData> response = authenticatorApi.authenticatorGetCurrentPersonWithHttpInfo(null);
        System.out.println("Read again with status " + response.getStatusCode());
    }
}
