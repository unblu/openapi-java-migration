///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.6.1
//SOURCES Util.java

/*
 * Searches the persons carrying one of the given person labels.
 *
 * Targets the findings of revapi/8.6.1_8.7.0/models-v4:
 *
 *   java.class.removed   class PersonLabelNamePersonSearchFilter
 *   java.class.removed   class PersonLabelNamePersonTypedSearchFilter
 *   java.class.removed   class HavingPersonLabelNameOperator
 *   java.field.removed   field EPersonSearchFilterField.PERSON_LABEL_NAME
 *   java.field.removed   field EPersonTypedSearchFilterField.PERSON_LABEL_NAME
 *
 * Searching persons by the *name* of their label was dropped in 8.7.0; the whole filter
 * class, its operator and the enum constant selecting the field were removed together.
 * This example compiles against 8.6.1 and no longer compiles when the dependency above
 * is changed to 8.7.0.
 *
 * Usage:
 *   ./SearchPersonsByLabelName.java <server> --admin <username> --password <password>
 */

import java.util.List;

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.PersonsApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.model.v4.EPersonSearchFilterField;
import com.unblu.webapi.model.v4.HavingPersonLabelNameOperator;
import com.unblu.webapi.model.v4.PersonData;
import com.unblu.webapi.model.v4.PersonLabelNamePersonSearchFilter;
import com.unblu.webapi.model.v4.EPersonTypedSearchFilterField;
import com.unblu.webapi.model.v4.PersonLabelNamePersonTypedSearchFilter;
import com.unblu.webapi.model.v4.PersonQuery;
import com.unblu.webapi.model.v4.PersonResult;
import com.unblu.webapi.model.v4.PersonTypedQuery;

public class SearchPersonsByLabelName {

    private static final String SCRIPT_NAME = "SearchPersonsByLabelName.java";

    private static final String LABEL_NAME = "premium-support";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        PersonsApi personsApi = new PersonsApi(client);

        // Both the filter and its operator were removed in 8.7.0.
        HavingPersonLabelNameOperator operator = new HavingPersonLabelNameOperator()
                .addValuesItem(LABEL_NAME);

        PersonLabelNamePersonSearchFilter filter = new PersonLabelNamePersonSearchFilter()
                .field(EPersonSearchFilterField.PERSON_LABEL_NAME)
                .operator(operator);

        PersonQuery query = new PersonQuery()
                .addSearchFiltersItem(filter);

        PersonResult result = personsApi.personsSearch(query, null);

        List<PersonData> persons = result.getItems();
        System.out.println("Found " + persons.size() + " person(s) labelled '" + LABEL_NAME + "'");
        for (PersonData person : persons) {
            System.out.println("- '" + person.getDisplayName() + "' (" + person.getPersonType() + ")");
        }

        // The agent search takes the typed variant of the same filter, which was removed
        // in 8.7.0 as well.
        PersonLabelNamePersonTypedSearchFilter typedFilter = new PersonLabelNamePersonTypedSearchFilter()
                .field(EPersonTypedSearchFilterField.PERSON_LABEL_NAME)
                .operator(new HavingPersonLabelNameOperator().addValuesItem(LABEL_NAME));

        PersonTypedQuery typedQuery = new PersonTypedQuery()
                .addSearchFiltersItem(typedFilter);

        PersonResult agents = personsApi.personsSearchAgents(typedQuery, null);
        System.out.println("Found " + agents.getItems().size() + " agent(s) labelled '" + LABEL_NAME + "'");
    }
}
