///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.0.1
//SOURCES Util.java

/*
 * Exercises every UsersApi operation that takes an 'expand' parameter.
 *
 * Targets the findings of revapi/8.0.1_8.1.2/jersey3-client-v4 for UsersApi:
 *
 *   java.method.parameterTypeChanged  (24 findings)
 *   old: ...(<body>, java.lang.String)
 *   new: ...(<body>, java.util.List<ExpandFields>)
 *
 *   usersCreate, usersCreateWithPassword, usersCreateWithRandomPassword,
 *   usersGetByUsername, usersRead, usersReadMultiple, usersRemovePassword, usersSearch,
 *   usersSetPassword, usersSetRandomPassword, usersTransformVirtualToPhysical,
 *   usersUpdate — and the WithHttpInfo variant of each.
 *
 * In 8.0.1 'expand' is a comma separated String; from 8.1.2 on it is a
 * List<ExpandFields>. This example compiles against 8.0.1 and no longer compiles when
 * the dependency above is changed to 8.1.2.
 *
 * Usage:
 *   ./UsersApiWithExpandParameter.java <server> --admin <username> --password <password>
 */

import java.util.Arrays;
import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.UsersApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.jersey.v4.invoker.ApiException;
import com.unblu.webapi.jersey.v4.invoker.ApiResponse;
import com.unblu.webapi.model.v4.EAuthorizationRole;
import com.unblu.webapi.model.v4.User;
import com.unblu.webapi.model.v4.UserList;
import com.unblu.webapi.model.v4.UserPasswordContainer;
import com.unblu.webapi.model.v4.UserQuery;
import com.unblu.webapi.model.v4.UserResult;
import com.unblu.webapi.model.v4.UsersSetPasswordBody;
import com.unblu.webapi.model.v4.UsersTransformVirtualToPhysicalBody;

public class UsersApiWithExpandParameter {

    private static final String SCRIPT_NAME = "UsersApiWithExpandParameter.java";

    /** In 8.0.1 the fields to expand are given as a single comma separated String. */
    private static final String EXPAND = "configuration,text";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        UsersApi usersApi = new UsersApi(client);

        User created = usersApi.usersCreate(newUser(), "avatar,configuration");
        System.out.println("Created user '" + created.getUsername() + "' (" + created.getId() + ")");

        ApiResponse<User> createResponse = usersApi.usersCreateWithHttpInfo(newUser(), null);
        System.out.println("Created another with status " + createResponse.getStatusCode());
        String secondId = createResponse.getData().getId();

        UserPasswordContainer container = new UserPasswordContainer();
        container.setUser(newUser());
        container.setPassword("Example-Password-1");
        User withPassword = usersApi.usersCreateWithPassword(container, EXPAND);
        System.out.println("Created user with password '" + withPassword.getUsername() + "'");

        UserPasswordContainer secondContainer = new UserPasswordContainer();
        secondContainer.setUser(newUser());
        secondContainer.setPassword("Example-Password-2");
        ApiResponse<User> withPasswordResponse = usersApi.usersCreateWithPasswordWithHttpInfo(secondContainer, null);
        System.out.println("Created another with password, status " + withPasswordResponse.getStatusCode());

        User withRandomPassword = usersApi.usersCreateWithRandomPassword(newUser(), EXPAND);
        System.out.println("Created user with a random password '" + withRandomPassword.getUsername() + "'");

        ApiResponse<User> randomPasswordResponse = usersApi.usersCreateWithRandomPasswordWithHttpInfo(newUser(), null);
        System.out.println("Created another with a random password, status " + randomPasswordResponse.getStatusCode());

        User read = usersApi.usersRead(created.getId(), EXPAND);
        System.out.println("Read by id: '" + read.getUsername() + "'");

        ApiResponse<User> readResponse = usersApi.usersReadWithHttpInfo(created.getId(), null);
        System.out.println("Read by id with status " + readResponse.getStatusCode());

        User byUsername = usersApi.usersGetByUsername(created.getUsername(), "avatar");
        System.out.println("Read by username: '" + byUsername.getUsername() + "'");

        ApiResponse<User> byUsernameResponse = usersApi.usersGetByUsernameWithHttpInfo(created.getUsername(), null);
        System.out.println("Read by username with status " + byUsernameResponse.getStatusCode());

        UserList list = usersApi.usersReadMultiple(Arrays.asList(created.getId(), secondId), EXPAND);
        System.out.println("Read multiple: " + list.getItems().size() + " user(s)");

        ApiResponse<UserList> listResponse = usersApi.usersReadMultipleWithHttpInfo(Arrays.asList(created.getId()), null);
        System.out.println("Read multiple with status " + listResponse.getStatusCode());

        UserResult searchResult = usersApi.usersSearch(new UserQuery(), EXPAND);
        System.out.println("Search found " + searchResult.getItems().size() + " user(s)");

        ApiResponse<UserResult> searchResponse = usersApi.usersSearchWithHttpInfo(new UserQuery(), null);
        System.out.println("Search with status " + searchResponse.getStatusCode());

        UsersSetPasswordBody passwordBody = new UsersSetPasswordBody();
        passwordBody.setPassword("Example-Password-3");
        User passwordSet = usersApi.usersSetPassword(withPassword.getId(), passwordBody, EXPAND);
        System.out.println("Set the password of '" + passwordSet.getUsername() + "'");

        ApiResponse<User> setPasswordResponse = usersApi.usersSetPasswordWithHttpInfo(withPassword.getId(), passwordBody, null);
        System.out.println("Set the password again with status " + setPasswordResponse.getStatusCode());

        User randomSet = usersApi.usersSetRandomPassword(withPassword.getId(), EXPAND);
        System.out.println("Set a random password on '" + randomSet.getUsername() + "'");

        ApiResponse<User> setRandomResponse = usersApi.usersSetRandomPasswordWithHttpInfo(withPassword.getId(), null);
        System.out.println("Set a random password again with status " + setRandomResponse.getStatusCode());

        User passwordRemoved = usersApi.usersRemovePassword(withPassword.getId(), EXPAND);
        System.out.println("Removed the password of '" + passwordRemoved.getUsername() + "'");

        ApiResponse<User> removePasswordResponse = usersApi.usersRemovePasswordWithHttpInfo(withRandomPassword.getId(), null);
        System.out.println("Removed another password with status " + removePasswordResponse.getStatusCode());

        transformVirtualUser(usersApi);

        read.setLastName("Smith-Renamed");
        User updated = usersApi.usersUpdate(read, EXPAND);
        System.out.println("Updated last name to '" + updated.getLastName() + "'");

        ApiResponse<User> updateResponse = usersApi.usersUpdateWithHttpInfo(updated, EXPAND);
        System.out.println("Updated again with status " + updateResponse.getStatusCode());

        for (String id : Arrays.asList(created.getId(), secondId, withPassword.getId(),
                withPasswordResponse.getData().getId(), withRandomPassword.getId(),
                randomPasswordResponse.getData().getId())) {
            usersApi.usersDelete(id);
        }
        System.out.println("Deleted every user this example created");
    }

    private static User newUser() {
        String unique = Util.randomSuffix();
        User user = new User();
        user.set$Type(User.TypeEnum.USER);
        user.setUsername("example-user-" + unique);
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setEmail("jane.smith+" + unique + "@example.com");
        user.setAuthorizationRole(EAuthorizationRole.REGISTERED_USER);
        return user;
    }

    /*
     * usersTransformVirtualToPhysical needs a virtual user, and a virtual user can only
     * come into existence through a propagated authentication -- there is no API call
     * that creates one. Both calls below therefore fail at run time. They are here
     * because the 'expand' parameter of both changed in 8.1.2, which is what this
     * example exists to demonstrate.
     */
    private static void transformVirtualUser(UsersApi usersApi) {
        UsersTransformVirtualToPhysicalBody body = new UsersTransformVirtualToPhysicalBody();
        body.setUsername("example-virtual-user-" + Util.randomSuffix());
        try {
            usersApi.usersTransformVirtualToPhysical(body, EXPAND);
            usersApi.usersTransformVirtualToPhysicalWithHttpInfo(body, null);
        } catch (ApiException e) {
            System.out.println("usersTransformVirtualToPhysical needs a virtual user, got HTTP " + e.getCode());
        }
    }
}
