///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.40.4
//SOURCES Util.java

/*
 * Creates a person visibility rule, reads it back and deletes it again.
 *
 * Targets the finding of revapi/8.40.4_8.41.4/models-v4:
 *
 *   java.class.removed  enum EPersonVisibilityLabelSelection
 *
 * The enum was renamed to EVisibilityLabelSelection in 8.41.4 and the old name removed,
 * so every caller selecting agents or visitors by label has to be touched. It is the
 * same "drop the Person prefix from the label vocabulary" rename that turned PersonLabel
 * into Label in 8.40.4. This example compiles against 8.40.4 and no longer compiles when
 * the dependency above is changed to 8.41.4.
 *
 * The constants are unchanged (ALL, UNLABELED, DEFINED_LABELS) and so is the JSON, so
 * this program keeps running against a current server even though it no longer compiles
 * against a current client.
 *
 * Usage:
 *   ./CreatePersonVisibilityRule.java <server> --superadmin <username> --password <password>
 */

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.PersonVisibilityRulesApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.model.v4.EPersonVisibilityLabelSelection;
import com.unblu.webapi.model.v4.PersonVisibilityRuleData;

public class CreatePersonVisibilityRule {

    private static final String SCRIPT_NAME = "CreatePersonVisibilityRule.java";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseSuperadminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        PersonVisibilityRulesApi personVisibilityRulesApi = new PersonVisibilityRulesApi(client);

        PersonVisibilityRuleData rule = createVisibilityRule();

        PersonVisibilityRuleData createdRule = personVisibilityRulesApi.personVisibilityRulesCreate(rule);
        System.out.println("Created visibility rule '" + createdRule.getName() + "' with id '" + createdRule.getId() + "'");

        PersonVisibilityRuleData readRule = personVisibilityRulesApi.personVisibilityRulesRead(createdRule.getId());
        System.out.println("Agent label selection: " + readRule.getAgentLabelSelection());
        System.out.println("Visitor label selection: " + readRule.getVisitorLabelSelection());

        personVisibilityRulesApi.personVisibilityRulesDelete(createdRule.getId());
        System.out.println("Deleted visibility rule '" + createdRule.getId() + "'");
    }

    private static PersonVisibilityRuleData createVisibilityRule() {
        PersonVisibilityRuleData rule = new PersonVisibilityRuleData();
        rule.setName("example-visibility-rule-" + Util.randomSuffix());
        rule.setDescription("Visibility rule of the Example Company");
        rule.setEnabled(true);
        // In 8.40.4 both label selections are an EPersonVisibilityLabelSelection.
        // DEFINED_LABELS is deliberately not used here: it requires labels that already
        // exist on the server, which would make this example depend on a second API.
        rule.setAgentLabelSelection(EPersonVisibilityLabelSelection.ALL);
        rule.setVisitorLabelSelection(EPersonVisibilityLabelSelection.UNLABELED);
        return rule;
    }
}
