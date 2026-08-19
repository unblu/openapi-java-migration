///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.0.1
//SOURCES Util.java

/*
 * Exercises every AccountsApi operation that takes an 'expand' parameter.
 *
 * Targets the findings of revapi/8.0.1_8.1.2/jersey3-client-v4 for AccountsApi:
 *
 *   java.method.parameterTypeChanged  (16 findings)
 *   old: ...(<body>, java.lang.String)
 *   new: ...(<body>, java.util.List<ExpandFields>)
 *
 *   accountsCreate, accountsCreateAccountWithNewAdmin, accountsGetByName,
 *   accountsGetCurrentAccount, accountsRead, accountsReadMultiple, accountsSearch,
 *   accountsUpdate — and the WithHttpInfo variant of each.
 *
 * In 8.0.1 'expand' is a comma separated String; from 8.1.2 on it is a
 * List<ExpandFields>. This example compiles against 8.0.1 and no longer compiles when
 * the dependency above is changed to 8.1.2.
 *
 * Usage:
 *   ./AccountsApiWithExpandParameter.java <server> --superadmin <username> --password <password>
 */

import java.util.Arrays;

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.AccountsApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.jersey.v4.invoker.ApiResponse;
import com.unblu.webapi.model.v4.Account;
import com.unblu.webapi.model.v4.AccountList;
import com.unblu.webapi.model.v4.AccountUserContainer;
import com.unblu.webapi.model.v4.AccountQuery;
import com.unblu.webapi.model.v4.AccountResult;
import com.unblu.webapi.model.v4.User;

public class AccountsApiWithExpandParameter {

    private static final String SCRIPT_NAME = "AccountsApiWithExpandParameter.java";

    /** In 8.0.1 the fields to expand are given as a single comma separated String. */
    private static final String EXPAND = "avatar,text";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseSuperadminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        AccountsApi accountsApi = new AccountsApi(client);

        Account currentAccount = accountsApi.accountsGetCurrentAccount("avatar,text");
        System.out.println("Current account: '" + currentAccount.getName() + "' (" + currentAccount.getId() + ")");

        ApiResponse<Account> currentAccountResponse = accountsApi.accountsGetCurrentAccountWithHttpInfo(EXPAND);
        System.out.println("Read again with status " + currentAccountResponse.getStatusCode());

        String accountName = "example-account-" + Util.randomSuffix();
        Account account = new Account();
        account.set$Type(Account.TypeEnum.ACCOUNT);
        account.setName(accountName);

        Account createdAccount = accountsApi.accountsCreate(account, EXPAND);
        System.out.println("Created account '" + createdAccount.getName() + "' (" + createdAccount.getId() + ")");

        Account readAccount = accountsApi.accountsRead(createdAccount.getId(), EXPAND);
        System.out.println("Read by id: '" + readAccount.getName() + "'");

        ApiResponse<Account> readResponse = accountsApi.accountsReadWithHttpInfo(createdAccount.getId(), null);
        System.out.println("Read by id with status " + readResponse.getStatusCode());

        Account accountByName = accountsApi.accountsGetByName(accountName, EXPAND);
        System.out.println("Read by name: '" + accountByName.getName() + "'");

        ApiResponse<Account> accountByNameResponse = accountsApi.accountsGetByNameWithHttpInfo(accountName, EXPAND);
        System.out.println("Read by name with status " + accountByNameResponse.getStatusCode());

        AccountList accountList = accountsApi.accountsReadMultiple(Arrays.asList(createdAccount.getId()), EXPAND);
        System.out.println("Read multiple: " + accountList.getItems().size() + " account(s)");

        ApiResponse<AccountList> accountListResponse = accountsApi.accountsReadMultipleWithHttpInfo(Arrays.asList(createdAccount.getId()), EXPAND);
        System.out.println("Read multiple with status " + accountListResponse.getStatusCode());

        AccountResult searchResult = accountsApi.accountsSearch(new AccountQuery(), EXPAND);
        System.out.println("Search found " + searchResult.getItems().size() + " account(s)");

        ApiResponse<AccountResult> searchResponse = accountsApi.accountsSearchWithHttpInfo(new AccountQuery(), EXPAND);
        System.out.println("Search with status " + searchResponse.getStatusCode());

        readAccount.setName(accountName + "-renamed");
        Account updatedAccount = accountsApi.accountsUpdate(readAccount, EXPAND);
        System.out.println("Updated account name to '" + updatedAccount.getName() + "'");

        ApiResponse<Account> updateResponse = accountsApi.accountsUpdateWithHttpInfo(updatedAccount, EXPAND);
        System.out.println("Updated again with status " + updateResponse.getStatusCode());

        accountsApi.accountsDelete(createdAccount.getId());
        System.out.println("Deleted account '" + createdAccount.getId() + "'");

        // accountsCreateAccountWithNewAdmin takes the same 'expand' parameter. It is
        // called through a helper so that the account it creates can be cleaned up.
        createAccountWithNewAdmin(accountsApi);
    }

    private static void createAccountWithNewAdmin(AccountsApi accountsApi) throws Exception {
        AccountUserContainer created = accountsApi.accountsCreateAccountWithNewAdmin(newAccountWithAdmin(), EXPAND);
        System.out.println("Created account with new admin: '" + created.getAccount().getName() + "'");

        ApiResponse<AccountUserContainer> response = accountsApi
                .accountsCreateAccountWithNewAdminWithHttpInfo(newAccountWithAdmin(), EXPAND);
        System.out.println("Created another with status " + response.getStatusCode());

        accountsApi.accountsDelete(response.getData().getAccount().getId());
        accountsApi.accountsDelete(created.getAccount().getId());
        System.out.println("Deleted both accounts");
    }

    /** Each call needs its own account name and admin username, so build a fresh one. */
    private static AccountUserContainer newAccountWithAdmin() {
        String unique = Util.randomSuffix();

        Account account = new Account();
        account.set$Type(Account.TypeEnum.ACCOUNT);
        account.setName("example-account-admin-" + unique);

        User admin = new User();
        admin.setUsername("example-admin-" + unique);
        admin.setFirstName("Jane");
        admin.setLastName("Smith");
        admin.setEmail("jane.smith@example.com");

        AccountUserContainer container = new AccountUserContainer();
        container.setAccount(account);
        container.setAdminUser(admin);
        return container;
    }
}
