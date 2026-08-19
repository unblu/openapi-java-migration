///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.0.1
//SOURCES Util.java

/*
 * Exercises every ExternalMessengersApi operation that takes an 'expand' parameter.
 *
 * Targets the findings of revapi/8.0.1_8.1.2/jersey3-client-v4 for ExternalMessengersApi:
 *
 *   java.method.parameterTypeChanged  (14 findings)
 *   old: ...(<body>, java.lang.String)
 *   new: ...(<body>, java.util.List<ExpandFields>)
 *
 *   externalMessengersCreate, externalMessengersGetByName, externalMessengersRead,
 *   externalMessengersSearch, externalMessengersSearchCustom,
 *   externalMessengersSearchSms, externalMessengersUpdate — and the WithHttpInfo
 *   variant of each.
 *
 * In 8.0.1 'expand' is a comma separated String; from 8.1.2 on it is a
 * List<ExpandFields>. This example compiles against 8.0.1 and no longer compiles when
 * the dependency above is changed to 8.1.2.
 *
 * Usage:
 *   ./ExternalMessengersApiWithExpandParameter.java <server> --admin <username> --password <password>
 */

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.ExternalMessengersApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.jersey.v4.invoker.ApiResponse;
import com.unblu.webapi.model.v4.CustomExternalMessengerChannel;
import com.unblu.webapi.model.v4.CustomExternalMessengerChannelQuery;
import com.unblu.webapi.model.v4.CustomExternalMessengerChannelResult;
import com.unblu.webapi.model.v4.EExternalMessengerChannelType;
import com.unblu.webapi.model.v4.ERegistrationStatus;
import com.unblu.webapi.model.v4.EWebApiVersion;
import com.unblu.webapi.model.v4.ExternalMessengerChannel;
import com.unblu.webapi.model.v4.ExternalMessengerChannelQuery;
import com.unblu.webapi.model.v4.ExternalMessengerChannelResult;
import com.unblu.webapi.model.v4.SmsExternalMessengerChannelQuery;
import com.unblu.webapi.model.v4.SmsExternalMessengerChannelResult;

public class ExternalMessengersApiWithExpandParameter {

    private static final String SCRIPT_NAME = "ExternalMessengersApiWithExpandParameter.java";

    /** In 8.0.1 the fields to expand are given as a single comma separated String. */
    private static final String EXPAND = "configuration,text";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        ExternalMessengersApi messengersApi = new ExternalMessengersApi(client);

        ExternalMessengerChannel created = messengersApi.externalMessengersCreate(newChannel(), "channelIcon,metadata");
        System.out.println("Created channel '" + created.getName() + "' (" + created.getId() + ")");

        ApiResponse<ExternalMessengerChannel> createResponse = messengersApi.externalMessengersCreateWithHttpInfo(newChannel(), null);
        System.out.println("Created another with status " + createResponse.getStatusCode());
        String secondId = createResponse.getData().getId();

        ExternalMessengerChannel read = messengersApi.externalMessengersRead(created.getId(), EXPAND);
        System.out.println("Read by id: '" + read.getName() + "'");

        ApiResponse<ExternalMessengerChannel> readResponse = messengersApi.externalMessengersReadWithHttpInfo(created.getId(), null);
        System.out.println("Read by id with status " + readResponse.getStatusCode());

        ExternalMessengerChannel byName = messengersApi.externalMessengersGetByName(created.getName(), "channelIcon");
        System.out.println("Read by name: '" + byName.getName() + "'");

        ApiResponse<ExternalMessengerChannel> byNameResponse = messengersApi.externalMessengersGetByNameWithHttpInfo(created.getName(), null);
        System.out.println("Read by name with status " + byNameResponse.getStatusCode());

        ExternalMessengerChannelResult searchResult = messengersApi.externalMessengersSearch(new ExternalMessengerChannelQuery(), EXPAND);
        System.out.println("Search found " + searchResult.getItems().size() + " channel(s)");

        ApiResponse<ExternalMessengerChannelResult> searchResponse = messengersApi.externalMessengersSearchWithHttpInfo(new ExternalMessengerChannelQuery(), null);
        System.out.println("Search with status " + searchResponse.getStatusCode());

        CustomExternalMessengerChannelResult customResult = messengersApi.externalMessengersSearchCustom(new CustomExternalMessengerChannelQuery(), EXPAND);
        System.out.println("Custom search found " + customResult.getItems().size() + " channel(s)");

        ApiResponse<CustomExternalMessengerChannelResult> customResponse = messengersApi.externalMessengersSearchCustomWithHttpInfo(new CustomExternalMessengerChannelQuery(), null);
        System.out.println("Custom search with status " + customResponse.getStatusCode());

        SmsExternalMessengerChannelResult smsResult = messengersApi.externalMessengersSearchSms(new SmsExternalMessengerChannelQuery(), EXPAND);
        System.out.println("SMS search found " + smsResult.getItems().size() + " channel(s)");

        ApiResponse<SmsExternalMessengerChannelResult> smsResponse = messengersApi.externalMessengersSearchSmsWithHttpInfo(new SmsExternalMessengerChannelQuery(), null);
        System.out.println("SMS search with status " + smsResponse.getStatusCode());

        ((CustomExternalMessengerChannel) read).setDescription("Renamed channel of the Example Company");
        ExternalMessengerChannel updated = messengersApi.externalMessengersUpdate(read, EXPAND);
        System.out.println("Updated description of '" + updated.getName() + "'");

        ApiResponse<ExternalMessengerChannel> updateResponse = messengersApi.externalMessengersUpdateWithHttpInfo(updated, EXPAND);
        System.out.println("Updated again with status " + updateResponse.getStatusCode());

        messengersApi.externalMessengersDelete(secondId);
        messengersApi.externalMessengersDelete(created.getId());
        System.out.println("Deleted both channels");
    }

    private static CustomExternalMessengerChannel newChannel() {
        String unique = Util.randomSuffix();
        CustomExternalMessengerChannel channel = new CustomExternalMessengerChannel();
        channel.set$Type(CustomExternalMessengerChannel.TypeEnum.CUSTOMEXTERNALMESSENGERCHANNEL);
        channel.setName("example-channel-" + unique);
        channel.setDescription("Custom messenger channel of the Example Company");
        channel.setSourceId("example-messenger-source-" + unique);
        channel.setType(EExternalMessengerChannelType.CUSTOM);
        channel.setWebhookEndpoint("https://messenger.example.com/webhook");
        channel.setWebhookStatus(ERegistrationStatus.INACTIVE);
        channel.setWebhookApiVersion(EWebApiVersion.V4);
        channel.setOutboundTimeoutMillis(5000L);
        return channel;
    }
}
