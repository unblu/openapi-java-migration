///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.25.1
//SOURCES Util.java

/*
 * Creates a conversation summary template and tries it out on a handful of messages
 * without storing anything, then deletes the template again.
 *
 * Targets the findings of revapi/8.25.1_8.26.2/models-v4 and
 * revapi/8.25.1_8.26.2/jersey3-client-v4:
 *
 *   java.class.removed   class ConversationSummaryTemplateTest
 *   java.field.removed   field ESummarizationTimeFrame.WHOLE_CONVERSATION_WITHOUT_ONBOARDING
 *   java.method.removed  ConversationSummaryTemplatesApi
 *                          ::conversationSummaryTemplatesGenerateSummaryTryForTemplate(..)
 *
 * The "try a template on a set of messages" operation, the request body class it took
 * and one of the summarization time frames were all removed in 8.26.2. This example
 * compiles against 8.25.1 and no longer compiles when the dependency above is changed
 * to 8.26.2.
 *
 * Usage:
 *   ./GenerateSummaryTryForTemplate.java <server> --admin <username> --password <password>
 */

import java.util.Arrays;

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.ConversationSummaryTemplatesApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.model.v4.ConversationSummaryData;
import com.unblu.webapi.model.v4.ConversationSummaryTemplateData;
import com.unblu.webapi.model.v4.ConversationSummaryTemplateTest;
import com.unblu.webapi.model.v4.ESummarizationTimeFrame;
import com.unblu.webapi.model.v4.TextMessageData;

public class GenerateSummaryTryForTemplate {

    private static final String SCRIPT_NAME = "GenerateSummaryTryForTemplate.java";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        ConversationSummaryTemplatesApi templatesApi = new ConversationSummaryTemplatesApi(client);

        ConversationSummaryTemplateData template = new ConversationSummaryTemplateData();
        template.setName("example-summary-template-" + Util.randomSuffix());
        template.setDescription("Summary template of the Example Company");
        template.setReviewRequired(false);
        // The time frame constant was removed in 8.26.2.
        template.setSummarizationTimeFrame(ESummarizationTimeFrame.WHOLE_CONVERSATION_WITHOUT_ONBOARDING);

        ConversationSummaryTemplateData createdTemplate = templatesApi.conversationSummaryTemplatesCreate(template);
        System.out.println("Created summary template '" + createdTemplate.getName() + "' with id '" + createdTemplate.getId() + "'");

        // Both the operation and its request body class were removed in 8.26.2.
        ConversationSummaryTemplateTest templateTest = new ConversationSummaryTemplateTest();
        templateTest.setConversationSummaryTemplateData(createdTemplate);
        templateTest.setMessages(Arrays.asList(
                textMessage("Hello, I cannot log in to my account."),
                textMessage("Could you please tell me the error message you get?"),
                textMessage("It says that my password has expired.")));

        ConversationSummaryData summary = templatesApi.conversationSummaryTemplatesGenerateSummaryTryForTemplate(templateTest);
        System.out.println("Generated summary in state " + summary.getSummarizationState() + ", id '" + summary.getId() + "'");

        templatesApi.conversationSummaryTemplatesDelete(createdTemplate.getId());
        System.out.println("Deleted summary template '" + createdTemplate.getId() + "'");
    }

    private static TextMessageData textMessage(String text) {
        TextMessageData message = new TextMessageData();
        message.setText(text);
        return message;
    }
}
