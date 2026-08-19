///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.8.2
//SOURCES Util.java

/*
 * Registers a custom external messenger channel that allows several conversations per
 * contact, reads it back and deletes it again.
 *
 * Targets the findings of revapi/8.8.2_8.9.2/models-v4:
 *
 *   java.method.removed  ExternalMessengerChannel::setSupportsMultipleConversationsPerContact(Boolean)
 *   java.method.removed  ExternalMessengerChannel::isSupportsMultipleConversationsPerContact()
 *   java.method.removed  ExternalMessengerChannel::supportsMultipleConversationsPerContact(Boolean)
 *
 * The 'supportsMultipleConversationsPerContact' property of an external messenger
 * channel was removed in 8.9.2, together with its getter, its setter and its fluent
 * setter. This example compiles against 8.8.2 and no longer compiles when the
 * dependency above is changed to 8.9.2.
 *
 * Usage:
 *   ./CreateMessengerChannelWithMultiConversationFlag.java <server> --admin <username> --password <password>
 */

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.ExternalMessengersApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.model.v4.CustomExternalMessengerChannel;
import com.unblu.webapi.model.v4.EExternalMessengerChannelType;
import com.unblu.webapi.model.v4.ERegistrationStatus;
import com.unblu.webapi.model.v4.EWebApiVersion;
import com.unblu.webapi.model.v4.ExternalMessengerChannel;

public class CreateMessengerChannelWithMultiConversationFlag {

    private static final String SCRIPT_NAME = "CreateMessengerChannelWithMultiConversationFlag.java";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        ExternalMessengersApi externalMessengersApi = new ExternalMessengersApi(client);

        CustomExternalMessengerChannel channel = new CustomExternalMessengerChannel();
        channel.setName("example-channel-" + Util.randomSuffix());
        channel.setDescription("Custom messenger channel of the Example Company");
        channel.setSourceId("example-messenger-source-" + Util.randomSuffix());
        channel.setType(EExternalMessengerChannelType.CUSTOM);
        channel.setWebhookEndpoint("https://messenger.example.com/webhook");
        channel.setWebhookStatus(ERegistrationStatus.INACTIVE);
        channel.setWebhookApiVersion(EWebApiVersion.V4);
        channel.setOutboundTimeoutMillis(5000L);
        // The property was removed in 8.9.2.
        channel.setSupportsMultipleConversationsPerContact(true);

        ExternalMessengerChannel createdChannel = externalMessengersApi.externalMessengersCreate(channel, null);
        System.out.println("Created channel '" + createdChannel.getName() + "' with id '" + createdChannel.getId() + "'");

        ExternalMessengerChannel readChannel = externalMessengersApi.externalMessengersRead(createdChannel.getId(), null);
        System.out.println("Read channel '" + readChannel.getName() + "'");
        System.out.println("Supports multiple conversations per contact: " + readChannel.isSupportsMultipleConversationsPerContact());

        externalMessengersApi.externalMessengersDelete(createdChannel.getId());
        System.out.println("Deleted channel '" + createdChannel.getId() + "'");
    }
}
