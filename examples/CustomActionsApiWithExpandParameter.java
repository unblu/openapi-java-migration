///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.0.1
//SOURCES Util.java

/*
 * Exercises every CustomActionsApi operation that takes an 'expand' parameter.
 *
 * Targets the findings of revapi/8.0.1_8.1.2/jersey3-client-v4 for CustomActionsApi:
 *
 *   java.method.parameterTypeChanged  (6 findings)
 *   old: ...(<body>, java.lang.String)
 *   new: ...(<body>, java.util.List<ExpandFields>)
 *
 *   customActionsCreate, customActionsRead, customActionsUpdate — and the WithHttpInfo
 *   variant of each.
 *
 * In 8.0.1 'expand' is a comma separated String; from 8.1.2 on it is a
 * List<ExpandFields>. This example compiles against 8.0.1 and no longer compiles when
 * the dependency above is changed to 8.1.2.
 *

 * The icon of a custom action is mandatory and there is no operation that creates an
 * avatar first: the avatar is passed inline as the expanded value of the actionIcon
 * ExpandableField, and every call that carries it has to expand 'actionIcon'.
 *
 * Usage:
 *   ./CustomActionsApiWithExpandParameter.java <server> --admin <username> --password <password>
 */

import java.util.Arrays;

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.CustomActionsApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.jersey.v4.invoker.ApiResponse;
import com.unblu.webapi.model.v4.CustomActionData;
import com.unblu.webapi.model.v4.Avatar;
import com.unblu.webapi.model.v4.CustomActionWebhookRegistration;
import com.unblu.webapi.model.v4.CustomConversationActionData;
import com.unblu.webapi.model.v4.EActionBarItemPosition;
import com.unblu.webapi.model.v4.ECustomActionState;
import com.unblu.webapi.model.v4.EConversationImpactingParticipationType;
import com.unblu.webapi.model.v4.EConversationParticipationState;
import com.unblu.webapi.model.v4.EConversationState;
import com.unblu.webapi.model.v4.EFrontend;
import com.unblu.webapi.model.v4.EWebApiVersion;
import com.unblu.webapi.model.v4.ExpandableField;

public class CustomActionsApiWithExpandParameter {

    private static final String SCRIPT_NAME = "CustomActionsApiWithExpandParameter.java";

    /** In 8.0.1 the fields to expand are given as a single comma separated String. */
    private static final String EXPAND = "configuration,text";

    /** A 16x16 PNG, so that the mandatory action icon has valid image data. */
    private static final String ICON_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAAAFklEQVR42mMw6LpBEmIY1TCqYfhqAAAPGZIQnuznDwAAAABJRU5ErkJggg==";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        CustomActionsApi customActionsApi = new CustomActionsApi(client);

        CustomActionData created = customActionsApi.customActionsCreate(newCustomAction(), "actionIcon,text");
        System.out.println("Created custom action '" + created.getName() + "' (" + created.getId() + ")");

        ApiResponse<CustomActionData> createResponse = customActionsApi.customActionsCreateWithHttpInfo(newCustomAction(), "actionIcon");
        System.out.println("Created another with status " + createResponse.getStatusCode());
        String secondId = createResponse.getData().getId();

        CustomActionData read = customActionsApi.customActionsRead(created.getId(), "actionIcon,text");
        System.out.println("Read by id: '" + read.getName() + "'");

        ApiResponse<CustomActionData> readResponse = customActionsApi.customActionsReadWithHttpInfo(created.getId(), null);
        System.out.println("Read by id with status " + readResponse.getStatusCode());

        read.setDescription("Renamed custom action of the Example Company");
        CustomActionData updated = customActionsApi.customActionsUpdate(read, "actionIcon");
        System.out.println("Updated description to '" + updated.getDescription() + "'");

        ApiResponse<CustomActionData> updateResponse = customActionsApi.customActionsUpdateWithHttpInfo(updated, "actionIcon");
        System.out.println("Updated again with status " + updateResponse.getStatusCode());

        customActionsApi.customActionsDelete(secondId);
        customActionsApi.customActionsDelete(created.getId());
        System.out.println("Deleted both custom actions");
    }

    /*
     * CustomActionData is already an interface in 8.0.1 -- its schema is polymorphic --
     * so the concrete variant has to be instantiated.
     */
    private static CustomConversationActionData newCustomAction() {
        String unique = Util.randomSuffix();
        CustomConversationActionData action = new CustomConversationActionData();
        action.set$Type(CustomConversationActionData.TypeEnum.CUSTOMCONVERSATIONACTIONDATA);
        action.setKey("example-action-" + unique);
        action.setName("example-action-" + unique);
        action.setDescription("Custom action of the Example Company");
        action.setState(ECustomActionState.ACTIVE);
        action.setInvocableFromFrontends(Arrays.asList(EFrontend.AGENT_DESK));
        action.setInvocableForConversationStates(Arrays.asList(EConversationState.ACTIVE));
        action.setInvocableForParticipationStates(Arrays.asList(EConversationParticipationState.ACTIVE));
        action.setInvocableBy(Arrays.asList(EConversationImpactingParticipationType.ASSIGNED_AGENT));
        action.setSortingOrder(10);
        action.setActionBarPosition(EActionBarItemPosition.SHOW_IF_POSSIBLE);
        action.setTriggerWebhook(webhookTrigger());
        // The icon is mandatory. The avatar is passed inline, as the expanded value of
        // the ExpandableField, and the call has to expand 'actionIcon' for it to be
        // taken into account.
        action.setActionIcon(new ExpandableField<>(null, minimalAvatar()));
        return action;
    }

    private static Avatar minimalAvatar() {
        Avatar avatar = new Avatar();
        avatar.set$Type(Avatar.TypeEnum.AVATAR);
        avatar.setImageData(ICON_IMAGE);
        return avatar;
    }

    private static CustomActionWebhookRegistration webhookTrigger() {
        CustomActionWebhookRegistration webhook = new CustomActionWebhookRegistration();
        webhook.setEndpoint("https://actions.example.com/webhook");
        webhook.setApiVersion(EWebApiVersion.V4);
        webhook.setOutboundTimeout(5000L);
        return webhook;
    }
}
