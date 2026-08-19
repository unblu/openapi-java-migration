///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.0.1
//SOURCES Util.java

/*
 * Exercises every ConversationTemplatesApi operation that takes an 'expand' parameter.
 *
 * Targets the findings of revapi/8.0.1_8.1.2/jersey3-client-v4 for ConversationTemplatesApi:
 *
 *   java.method.parameterTypeChanged  (12 findings)
 *   old: ...(<body>, java.lang.String)
 *   new: ...(<body>, java.util.List<ExpandFields>)
 *
 *   conversationTemplatesCreate, conversationTemplatesGetDefaultTemplateByEngagementType,
 *   conversationTemplatesRead, conversationTemplatesReadMultiple,
 *   conversationTemplatesSearch, conversationTemplatesUpdate — and the WithHttpInfo
 *   variant of each.
 *
 * In 8.0.1 'expand' is a comma separated String; from 8.1.2 on it is a
 * List<ExpandFields>. This example compiles against 8.0.1 and no longer compiles when
 * the dependency above is changed to 8.1.2.
 *
 * Usage:
 *   ./ConversationTemplatesApiWithExpandParameter.java <server> --admin <username> --password <password>
 */

import java.util.Arrays;
import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.ConversationTemplatesApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.jersey.v4.invoker.ApiResponse;
import com.unblu.webapi.model.v4.ConversationTemplate;
import com.unblu.webapi.model.v4.ConversationTemplateList;
import com.unblu.webapi.model.v4.ConversationTemplateQuery;
import com.unblu.webapi.model.v4.EConversationTemplateSearchFilterField;
import com.unblu.webapi.model.v4.EqualsStringOperator;
import com.unblu.webapi.model.v4.NameConversationTemplateSearchFilter;
import com.unblu.webapi.model.v4.ConversationTemplateResult;
import com.unblu.webapi.model.v4.EConversationVisibility;
import com.unblu.webapi.model.v4.EInitialEngagementType;

public class ConversationTemplatesApiWithExpandParameter {

    private static final String SCRIPT_NAME = "ConversationTemplatesApiWithExpandParameter.java";

    /** In 8.0.1 the fields to expand are given as a single comma separated String. */
    private static final String EXPAND = "configuration,text";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        ConversationTemplatesApi templatesApi = new ConversationTemplatesApi(client);

        ConversationTemplate created = templatesApi.conversationTemplatesCreate(newTemplate(), "configuration,text");
        System.out.println("Created template '" + created.getName() + "' (" + created.getId() + ")");

        ApiResponse<ConversationTemplate> createResponse = templatesApi.conversationTemplatesCreateWithHttpInfo(newTemplate(), EXPAND);
        System.out.println("Created another with status " + createResponse.getStatusCode());
        String secondId = createResponse.getData().getId();

        ConversationTemplate read = templatesApi.conversationTemplatesRead(created.getId(), EXPAND);
        System.out.println("Read by id: '" + read.getName() + "'");

        ApiResponse<ConversationTemplate> readResponse = templatesApi.conversationTemplatesReadWithHttpInfo(created.getId(), null);
        System.out.println("Read by id with status " + readResponse.getStatusCode());

        ConversationTemplateList list = templatesApi.conversationTemplatesReadMultiple(Arrays.asList(created.getId(), secondId), EXPAND);
        System.out.println("Read multiple: " + list.getItems().size() + " template(s)");

        ApiResponse<ConversationTemplateList> listResponse = templatesApi.conversationTemplatesReadMultipleWithHttpInfo(Arrays.asList(created.getId()), EXPAND);
        System.out.println("Read multiple with status " + listResponse.getStatusCode());

        ConversationTemplate defaultTemplate = templatesApi.conversationTemplatesGetDefaultTemplateByEngagementType(EInitialEngagementType.CHAT_REQUEST, EXPAND);
        System.out.println("Default chat template: '" + (defaultTemplate == null ? "<none>" : defaultTemplate.getName()) + "'");

        ApiResponse<ConversationTemplate> defaultResponse = templatesApi.conversationTemplatesGetDefaultTemplateByEngagementTypeWithHttpInfo(EInitialEngagementType.CHAT_REQUEST, EXPAND);
        System.out.println("Default chat template with status " + defaultResponse.getStatusCode());

        ConversationTemplateResult searchResult = templatesApi.conversationTemplatesSearch(queryByName(created.getName()), EXPAND);
        System.out.println("Search found " + searchResult.getItems().size() + " template(s)");

        ApiResponse<ConversationTemplateResult> searchResponse = templatesApi.conversationTemplatesSearchWithHttpInfo(queryByName(created.getName()), EXPAND);
        System.out.println("Search with status " + searchResponse.getStatusCode());

        read.setDescription("Renamed template of the Example Company");
        ConversationTemplate updated = templatesApi.conversationTemplatesUpdate(read, EXPAND);
        System.out.println("Updated description to '" + updated.getDescription() + "'");

        ApiResponse<ConversationTemplate> updateResponse = templatesApi.conversationTemplatesUpdateWithHttpInfo(updated, EXPAND);
        System.out.println("Updated again with status " + updateResponse.getStatusCode());

        templatesApi.conversationTemplatesDelete(secondId);
        templatesApi.conversationTemplatesDelete(created.getId());
        System.out.println("Deleted both templates");
    }

    /*
     * The search is deliberately scoped to the template this example created. An
     * unfiltered search returns every template of the account, and a current server has
     * templates whose initialEngagementType is a value that did not exist in 8.0.1 --
     * the 8.0.1 client then fails to deserialize the response. See report section on
     * enum widening.
     */
    private static ConversationTemplateQuery queryByName(String name) {
        return new ConversationTemplateQuery()
                .addSearchFiltersItem(new NameConversationTemplateSearchFilter()
                        .field(EConversationTemplateSearchFilterField.NAME)
                        .operator(new EqualsStringOperator().value(name)));
    }

    private static ConversationTemplate newTemplate() {
        ConversationTemplate template = new ConversationTemplate();
        template.set$Type(ConversationTemplate.TypeEnum.CONVERSATIONTEMPLATE);
        template.setName("example-template-" + Util.randomSuffix());
        template.setDescription("Conversation template of the Example Company");
        template.setInitialEngagementType(EInitialEngagementType.CHAT_REQUEST);
        template.setConversationVisibility(EConversationVisibility.PRIVATE);
        template.setDefaultTemplate(false);
        return template;
    }
}
