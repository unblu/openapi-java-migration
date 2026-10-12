///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.42.2
//SOURCES Util.java

/*
 * Creates a webhook registration, reads it back by id and by name, searches for it,
 * updates it and deletes it again.
 *
 * Targets the findings of revapi/8.42.2_8.43.1:
 *
 *   models-v4          java.class.removed                    class WebhookRegistration
 *   models-v4          java.class.removed                    class WebhookRegistrationList
 *   models-v4          java.class.removed                    class WebhookRegistrationResult
 *   jersey3-client-v4  java.method.numberOfParametersChanged WebhookRegistrationsApi create,
 *                                                            read, getByName, update
 *   jersey3-client-v4  java.method.returnTypeChanged         the same four, plus
 *                                                            readMultiple and search
 *
 * In 8.43.1 a webhook registration can trigger an Aria agentic flow, carried in a new
 * expandable field agenticFlowData. The model was renamed to WebhookRegistrationData -
 * with WebhookRegistrationDataList and WebhookRegistrationDataResult - and the four
 * operations that take or return a single registration gained an expand parameter.
 * Every call below is affected by one or both. This example compiles against 8.42.2 and
 * no longer compiles when the dependency above is changed to 8.43.1.
 *
 * The rename also reaches the webhook events: WebhookPingEvent.webhookConfiguration and
 * WebhookExpiredEvent.webhookRegistration are typed WebhookRegistrationData in 8.43.1.
 * Only a webhook receiver deserializing those events is affected; it is not exercised
 * here.
 *
 * The JSON did not break. On a model like this one, $_type is informational: a 8.43.1
 * server ignores the "$_type":"WebhookRegistration" this client sends, and this client
 * reads the "$_type":"WebhookRegistrationData" it answers with, its TypeEnum falling back
 * to its single constant for any value. So this program keeps running against a current
 * server. The update below sends no expand parameter, so the server leaves
 * agenticFlowData as it is.
 *
 * Usage:
 *   ./ManageWebhookRegistration.java <server> --superadmin <username> --password <password>
 */

import java.util.List;

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.WebhookRegistrationsApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.model.v4.ERegistrationStatus;
import com.unblu.webapi.model.v4.EWebApiVersion;
import com.unblu.webapi.model.v4.WebhookRegistration;
import com.unblu.webapi.model.v4.WebhookRegistrationList;
import com.unblu.webapi.model.v4.WebhookRegistrationQuery;
import com.unblu.webapi.model.v4.WebhookRegistrationResult;

public class ManageWebhookRegistration {

    private static final String SCRIPT_NAME = "ManageWebhookRegistration.java";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseSuperadminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        WebhookRegistrationsApi webhookRegistrationsApi = new WebhookRegistrationsApi(client);

        WebhookRegistration created = webhookRegistrationsApi.webhookRegistrationsCreate(createRegistration());
        System.out.println("Created webhook registration '" + created.getName() + "' with id '" + created.getId() + "'");

        try {
            WebhookRegistration read = webhookRegistrationsApi.webhookRegistrationsRead(created.getId());
            System.out.println("Read by id: endpoint " + read.getEndpoint() + ", status " + read.getStatus());

            WebhookRegistration byName = webhookRegistrationsApi.webhookRegistrationsGetByName(created.getName());
            System.out.println("Read by name: id " + byName.getId());

            WebhookRegistrationList list = webhookRegistrationsApi.webhookRegistrationsReadMultiple(List.of(created.getId()));
            System.out.println("Read multiple: " + list.getItems().size() + " registration(s)");

            WebhookRegistrationResult result = webhookRegistrationsApi.webhookRegistrationsSearch(new WebhookRegistrationQuery());
            System.out.println("Search: " + result.getItems().size() + " registration(s) in the account");

            read.setDescription("Webhook of the Example Company, updated");
            WebhookRegistration updated = webhookRegistrationsApi.webhookRegistrationsUpdate(read);
            System.out.println("Updated: description '" + updated.getDescription() + "', version " + updated.getVersion());
        } finally {
            webhookRegistrationsApi.webhookRegistrationsDelete(created.getId());
            System.out.println("Deleted webhook registration '" + created.getId() + "'");
        }
    }

    private static WebhookRegistration createRegistration() {
        WebhookRegistration registration = new WebhookRegistration();
        registration.setName("example-webhook-" + Util.randomSuffix());
        registration.setDescription("Webhook of the Example Company");
        registration.setEndpoint("https://unblu.example.com/webhook");
        registration.setApiVersion(EWebApiVersion.V4);
        registration.setEvents(List.of("conversation.created"));
        // INACTIVE, so the server never tries to deliver to the example endpoint.
        registration.setStatus(ERegistrationStatus.INACTIVE);
        return registration;
    }
}
