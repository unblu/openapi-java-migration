///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.39.3
//SOURCES Util.java

/*
 * Creates a person label, reads it back and deletes it again.
 *
 * Targets the findings of revapi/8.39.3_8.40.4/models-v4:
 *
 *   java.class.removed  class PersonLabel, PersonLabelList, PersonLabelResult,
 *                       PersonLabelQuery, PersonLabelOrderBy, NamePersonLabelSearchFilter
 *   java.class.removed  enum EPersonLabelTargetType, EPersonLabelManagementRole,
 *                       EPersonLabelOrderByField, EPersonLabelSearchFilterField
 *   java.method.returnTypeChanged / parameterTypeChanged / returnTypeTypeParametersChanged
 *                       every PersonLabelsApi operation, because each of them takes or
 *                       returns one of the renamed types
 *
 * In 8.40.4 the whole "person label" vocabulary was renamed to "label":
 * PersonLabel became Label, EPersonLabelTargetType became ELabelTargetType,
 * EPersonLabelManagementRole became ELabelManagementRole, and so on. The old names
 * were removed rather than deprecated. This example compiles against 8.39.3 and no
 * longer compiles when the dependency above is changed to 8.40.4.
 *
 * Usage:
 *   ./CreatePersonLabel.java <server> --admin <username> --password <password>
 */

import java.util.Arrays;
import java.util.List;

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.PersonLabelsApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.model.v4.EPersonLabelManagementRole;
import com.unblu.webapi.model.v4.EPersonLabelTargetType;
import com.unblu.webapi.model.v4.EPersonLabelOrderByField;
import com.unblu.webapi.model.v4.EPersonLabelSearchFilterField;
import com.unblu.webapi.model.v4.EqualsStringOperator;
import com.unblu.webapi.model.v4.NamePersonLabelSearchFilter;
import com.unblu.webapi.model.v4.Order;
import com.unblu.webapi.model.v4.PersonLabel;
import com.unblu.webapi.model.v4.PersonLabelList;
import com.unblu.webapi.model.v4.PersonLabelOrderBy;
import com.unblu.webapi.model.v4.PersonLabelQuery;
import com.unblu.webapi.model.v4.PersonLabelResult;
import com.unblu.webapi.model.v4.PersonLabelsGetByScopeBody;

public class CreatePersonLabel {

    private static final String SCRIPT_NAME = "CreatePersonLabel.java";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        PersonLabelsApi personLabelsApi = new PersonLabelsApi(client);

        PersonLabel personLabel = new PersonLabel();
        personLabel.setName("premium-support-" + Util.randomSuffix());
        personLabel.setDescription("Persons entitled to the premium support of the Example Company");
        personLabel.setColor("white");
        personLabel.setSettableOn(Arrays.asList(
                EPersonLabelTargetType.AGENT,
                EPersonLabelTargetType.AUTHENTICATED_VISITOR));
        personLabel.setReadableByRoles(Arrays.asList(
                EPersonLabelManagementRole.AGENT,
                EPersonLabelManagementRole.SUPERVISOR));
        personLabel.setSettableByRoles(Arrays.asList(EPersonLabelManagementRole.SUPERVISOR));
        personLabel.setDisplayedToRoles(Arrays.asList(EPersonLabelManagementRole.AGENT));

        PersonLabel createdLabel = personLabelsApi.personLabelsCreate(personLabel);
        System.out.println("Created person label '" + createdLabel.getName() + "' with id '" + createdLabel.getId() + "'");

        PersonLabel readLabel = personLabelsApi.personLabelsRead(createdLabel.getId());
        System.out.println("Read person label '" + readLabel.getName() + "', settable on: " + readLabel.getSettableOn());

        PersonLabelList byNames = personLabelsApi.personLabelsGetByNames(Arrays.asList(createdLabel.getName()));
        System.out.println("Read by names: " + byNames.getItems().size() + " label(s)");

        PersonLabelList multiple = personLabelsApi.personLabelsReadMultiple(Arrays.asList(createdLabel.getId()));
        System.out.println("Read multiple: " + multiple.getItems().size() + " label(s)");

        PersonLabelsGetByScopeBody scopeBody = new PersonLabelsGetByScopeBody();
        scopeBody.setScope("ACCOUNT");
        PersonLabelList byScope = personLabelsApi.personLabelsGetByScope(scopeBody);
        System.out.println("Read by scope: " + byScope.getItems().size() + " label(s)");

        PersonLabelQuery query = new PersonLabelQuery()
                .addSearchFiltersItem(new NamePersonLabelSearchFilter()
                        .field(EPersonLabelSearchFilterField.NAME)
                        .operator(new EqualsStringOperator().value(createdLabel.getName())))
                .addOrderByItem(new PersonLabelOrderBy()
                        .field(EPersonLabelOrderByField.NAME)
                        .order(Order.ASCENDING));

        PersonLabelResult searchResult = personLabelsApi.personLabelsSearch(query);
        List<PersonLabel> found = searchResult.getItems();
        System.out.println("Search found " + found.size() + " label(s)");

        readLabel.setDescription("Renamed label of the Example Company");
        PersonLabel updatedLabel = personLabelsApi.personLabelsUpdate(readLabel);
        System.out.println("Updated description to '" + updatedLabel.getDescription() + "'");

        personLabelsApi.personLabelsDelete(createdLabel.getId());
        System.out.println("Deleted person label '" + createdLabel.getId() + "'");
    }
}
