///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.39.3

/*
 * Builds the person search filters that select on labels, and reads the labels back off
 * the models that carry them.
 *
 * Targets the findings of revapi/8.39.3_8.40.4/models-v4:
 *
 *   java.class.removed                           interface PersonLabelsOperator
 *   java.class.removed                           class AllOfPersonLabelsOperator,
 *                                                AnyOfPersonLabelsOperator,
 *                                                NoneOfPersonLabelsOperator
 *   java.class.removed                           enum EPersonLabelsOperatorType
 *   java.class.removed                           class PersonLabelModificationEvent
 *   java.method.parameterTypeChanged             PersonLabelsPersonSearchFilter::operator(..),
 *                                                ::setOperator(..) and the typed variant
 *   java.method.returnTypeChanged                ::getOperator()
 *   java.method.parameterTypeChanged             PersonData::addLabelsItem(..),
 *                                                ContextPersonInfo::addLabelsItem(..)
 *   java.method.returnTypeTypeParametersChanged  PersonData::getLabels(),
 *                                                ContextPersonInfo::getLabels()
 *   java.method.parameterTypeChanged             LicenseData::putFeaturesItem(..)
 *
 * In 8.40.4 the "person label" vocabulary was renamed to "label": PersonLabel became
 * Label and PersonLabelsOperator became LabelsOperator, so every signature mentioning
 * them changed. LicenseData::putFeaturesItem changed independently, from Object to
 * JsonNode. This example compiles against 8.39.3 and no longer compiles when the
 * dependency above is changed to 8.40.4.
 *
 * It takes no arguments: the filters and models are built locally, no server is
 * involved.
 *
 * Usage:
 *   ./SearchPersonsByLabels.java
 */

import java.util.Arrays;
import java.util.List;

import com.unblu.webapi.model.v4.AllOfPersonLabelsOperator;
import com.unblu.webapi.model.v4.AnyOfPersonLabelsOperator;
import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.model.v4.ContextPersonInfo;
import com.unblu.webapi.model.v4.EPersonLabelsOperatorType;
import com.unblu.webapi.model.v4.EPersonSearchFilterField;
import com.unblu.webapi.model.v4.EPersonTypedSearchFilterField;
import com.unblu.webapi.model.v4.LicenseData;
import com.unblu.webapi.model.v4.NoneOfPersonLabelsOperator;
import com.unblu.webapi.model.v4.PersonData;
import com.unblu.webapi.model.v4.PersonLabel;
import com.unblu.webapi.model.v4.PersonLabelModificationEvent;
import com.unblu.webapi.model.v4.PersonLabelsOperator;
import com.unblu.webapi.model.v4.PersonLabelsPersonSearchFilter;
import com.unblu.webapi.model.v4.PersonLabelsPersonTypedSearchFilter;
import com.unblu.webapi.model.v4.PersonQuery;
import com.unblu.webapi.model.v4.PersonTypedQuery;

public class SearchPersonsByLabels {

    private static final String FIRST_LABEL = "premium-support";
    private static final String SECOND_LABEL = "priority-queue";

    public static void main(String[] args) throws Exception {
        AllOfPersonLabelsOperator allOf = new AllOfPersonLabelsOperator()
                .values(Arrays.asList(FIRST_LABEL, SECOND_LABEL));
        System.out.println("allOf operator type " + allOf.getType() + ", values " + allOf.getValues());

        AnyOfPersonLabelsOperator anyOf = new AnyOfPersonLabelsOperator();
        anyOf.setValues(Arrays.asList(FIRST_LABEL, SECOND_LABEL));
        System.out.println("anyOf operator type " + anyOf.getType() + ", values " + anyOf.getValues());

        NoneOfPersonLabelsOperator noneOf = new NoneOfPersonLabelsOperator();
        noneOf.setValues(Arrays.asList(SECOND_LABEL));
        System.out.println("noneOf operator type " + noneOf.getType() + ", values " + noneOf.getValues());

        System.out.println("Operator types: " + Arrays.toString(EPersonLabelsOperatorType.values()));

        PersonQuery query = new PersonQuery()
                .addSearchFiltersItem(labelFilter(allOf));
        System.out.println("Person query carries " + query.getSearchFilters().size() + " filter(s)");

        PersonTypedQuery typedQuery = new PersonTypedQuery()
                .addSearchFiltersItem(typedLabelFilter(anyOf));
        System.out.println("Typed person query carries " + typedQuery.getSearchFilters().size() + " filter(s)");

        readLabelsOffModels();
        putLicenseFeature();
        parseModificationEvent();
    }

    private static PersonLabelsPersonSearchFilter labelFilter(PersonLabelsOperator operator) {
        PersonLabelsPersonSearchFilter filter = new PersonLabelsPersonSearchFilter();
        filter.setField(EPersonSearchFilterField.PERSON_LABELS);
        filter.setOperator(operator);
        System.out.println("Filter operator is " + filter.getOperator().getType());
        return filter;
    }

    private static PersonLabelsPersonTypedSearchFilter typedLabelFilter(PersonLabelsOperator operator) {
        PersonLabelsPersonTypedSearchFilter filter = new PersonLabelsPersonTypedSearchFilter();
        filter.setField(EPersonTypedSearchFilterField.PERSON_LABELS);
        filter.setOperator(operator);
        System.out.println("Typed filter operator is " + filter.getOperator().getType());
        return filter;
    }

    /*
     * Both PersonData and ContextPersonInfo carry a List<PersonLabel>, which becomes a
     * List<Label> in 8.40.4.
     */
    private static void readLabelsOffModels() {
        PersonData person = new PersonData();
        person.addLabelsItem(newLabel(FIRST_LABEL));
        person.addLabelsItem(newLabel(SECOND_LABEL));
        List<PersonLabel> personLabels = person.getLabels();
        System.out.println("Person carries " + personLabels.size() + " label(s), first '" + personLabels.get(0).getName() + "'");

        ContextPersonInfo contextPerson = new ContextPersonInfo();
        contextPerson.addLabelsItem(newLabel(FIRST_LABEL));
        List<PersonLabel> contextLabels = contextPerson.getLabels();
        System.out.println("Context person carries " + contextLabels.size() + " label(s), first '" + contextLabels.get(0).getName() + "'");
    }

    /*
     * Unrelated to the rename: the feature map of a licence went from Object values to
     * JsonNode values in 8.40.4.
     */
    private static void putLicenseFeature() {
        LicenseData license = new LicenseData();
        license.putFeaturesItem("example.feature.enabled", Boolean.TRUE);
        license.putFeaturesItem("example.feature.limit", Integer.valueOf(10));
        System.out.println("Licence carries " + license.getFeatures().size() + " feature(s)");
    }

    /*
     * The webhook payload announcing that a label changed is a PersonLabelModificationEvent
     * in 8.39.3 and a LabelModificationEvent in 8.40.4.
     */
    private static void parseModificationEvent() {
        String payload = "{\"$_type\":\"PersonLabelModificationEvent\","
                + "\"eventType\":\"personlabel.modified\","
                + "\"timestamp\":1700000000000,"
                + "\"accountId\":\"example-account-id\","
                + "\"action\":\"UPDATE\"}";

        PersonLabelModificationEvent event = V4ApiUtil.deserializeJson(payload, PersonLabelModificationEvent.class);
        System.out.println("Label " + event.getAction() + " at " + event.getTimestamp());
    }

    private static PersonLabel newLabel(String name) {
        PersonLabel label = new PersonLabel();
        label.setName(name);
        label.setDescription("Label of the Example Company");
        return label;
    }
}
