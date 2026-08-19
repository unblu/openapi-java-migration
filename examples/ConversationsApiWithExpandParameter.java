///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.0.1
//SOURCES Util.java

/*
 * Exercises every ConversationsApi operation that takes an 'expand' parameter.
 *
 * Targets the findings of revapi/8.0.1_8.1.2/jersey3-client-v4 for ConversationsApi:
 *
 *   java.method.parameterTypeChanged  (44 findings)
 *   old: ...(<body>, java.lang.String)
 *   new: ...(<body>, java.util.List<ExpandFields>)
 *
 *   conversationsAddExternalParticipant, conversationsAddParticipant,
 *   conversationsChangeParticipantVisibility, conversationsCreate, conversationsEnd,
 *   conversationsGetBySourceIdAndChannelId, conversationsOffboardParticipant,
 *   conversationsRead, conversationsSearch, conversationsSetAssigneePerson,
 *   conversationsSetAwaitedPersonType, conversationsSetContextPerson,
 *   conversationsSetLocale, conversationsSetRecipient,
 *   conversationsSetScheduledTimestamp, conversationsSetStarred, conversationsSetTopic,
 *   conversationsSetVisibility, conversationsSetVisitorData,
 *   conversationsUpdateConfiguration, conversationsUpdateMetadata,
 *   conversationsUpdateText — and the WithHttpInfo variant of each.
 *
 * Two conversations are created: the plain variant of every operation is applied to the
 * first, the WithHttpInfo variant to the second. That keeps the two calls of each pair
 * next to each other without either of them fighting the other's state.
 *
 * In 8.0.1 'expand' is a comma separated String; from 8.1.2 on it is a
 * List<ExpandFields>. This example compiles against 8.0.1 and no longer compiles when
 * the dependency above is changed to 8.1.2.
 *
 * Without the deserialization settings applied by Util.applyRecommendedDeserialization,
 * this example cannot run at all: EConversationLinkType gained the value
 * OPEN_AS_GHOST_IN_AGENT_DESK after 8.0.1, every conversation payload carries the
 * conversation links, and the 8.0.1 enum throws on the unknown value rather than
 * degrading. With READ_UNKNOWN_ENUM_VALUES_AS_NULL the field deserializes to null and
 * the call succeeds. Clients from 8.13.1 on set that flag themselves.
 *
 * Usage:
 *   ./ConversationsApiWithExpandParameter.java <server> --admin <username> --password <password>
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.ConversationsApi;
import com.unblu.webapi.jersey.v4.api.PersonsApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.jersey.v4.invoker.ApiException;
import com.unblu.webapi.jersey.v4.invoker.ApiResponse;
import com.unblu.webapi.model.v4.ConversationCreationData;
import com.unblu.webapi.model.v4.ConversationCreationParticipantData;
import com.unblu.webapi.model.v4.ConversationData;
import com.unblu.webapi.model.v4.ConversationQuery;
import com.unblu.webapi.model.v4.ConversationResult;
import com.unblu.webapi.model.v4.ConversationsAddExternalParticipantBody;
import com.unblu.webapi.model.v4.ConversationsAddParticipantBody;
import com.unblu.webapi.model.v4.ConversationsChangeParticipantVisibilityBody;
import com.unblu.webapi.model.v4.ConversationsEndBody;
import com.unblu.webapi.model.v4.ConversationsOffboardParticipantBody;
import com.unblu.webapi.model.v4.ConversationsSetAssigneePersonBody;
import com.unblu.webapi.model.v4.ConversationsSetAwaitedPersonTypeBody;
import com.unblu.webapi.model.v4.ConversationsSetContextPersonBody;
import com.unblu.webapi.model.v4.ConversationsSetLocaleBody;
import com.unblu.webapi.model.v4.ConversationsSetScheduledTimestampBody;
import com.unblu.webapi.model.v4.ConversationsSetStarredBody;
import com.unblu.webapi.model.v4.ConversationsSetTopicBody;
import com.unblu.webapi.model.v4.ConversationsSetVisibilityBody;
import com.unblu.webapi.model.v4.ConversationsSetVisitorDataBody;
import com.unblu.webapi.model.v4.EAuthorizationRole;
import com.unblu.webapi.model.v4.EAwaitedPersonType;
import com.unblu.webapi.model.v4.EConversationEndReason;
import com.unblu.webapi.model.v4.EConversationLeftReason;
import com.unblu.webapi.model.v4.EConversationRealParticipationType;
import com.unblu.webapi.model.v4.EConversationVisibility;
import com.unblu.webapi.model.v4.EInitialEngagementType;
import com.unblu.webapi.model.v4.EPersonType;
import com.unblu.webapi.model.v4.PersonData;

public class ConversationsApiWithExpandParameter {

    private static final String SCRIPT_NAME = "ConversationsApiWithExpandParameter.java";

    /** In 8.0.1 the fields to expand are given as a single comma separated String. */
    private static final String EXPAND = "configuration,text";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        PersonsApi personsApi = new PersonsApi(client);
        ConversationsApi conversationsApi = new ConversationsApi(client);

        PersonData participant = personsApi.personsCreateOrUpdateVirtual(newAgent(), null);
        PersonData other = personsApi.personsCreateOrUpdateVirtual(newAgent(), null);
        PersonData visitor = personsApi.personsCreateOrUpdateVirtual(newVisitor(), null);
        System.out.println("Created two agent persons and a visitor to act on the conversations");

        String sourceIdA = "example-conversation-" + Util.randomSuffix();
        String sourceIdB = "example-conversation-" + Util.randomSuffix();

        ConversationData a = conversationsApi.conversationsCreate(newConversation(sourceIdA, visitor.getId()), "configuration,text");
        System.out.println("Created conversation A '" + a.getId() + "'");

        ApiResponse<ConversationData> createResponse = conversationsApi.conversationsCreateWithHttpInfo(newConversation(sourceIdB, visitor.getId()), null);
        ConversationData b = createResponse.getData();
        System.out.println("Created conversation B with status " + createResponse.getStatusCode());

        ConversationData read = conversationsApi.conversationsRead(a.getId(), EXPAND);
        System.out.println("Read A, topic '" + read.getTopic() + "'");

        ApiResponse<ConversationData> readResponse = conversationsApi.conversationsReadWithHttpInfo(b.getId(), null);
        System.out.println("Read B with status " + readResponse.getStatusCode());

        ConversationResult searchResult = conversationsApi.conversationsSearch(new ConversationQuery(), EXPAND);
        System.out.println("Search found " + searchResult.getItems().size() + " conversation(s)");

        ApiResponse<ConversationResult> searchResponse = conversationsApi.conversationsSearchWithHttpInfo(new ConversationQuery(), null);
        System.out.println("Search with status " + searchResponse.getStatusCode());

        ConversationData bySource = conversationsApi.conversationsGetBySourceIdAndChannelId(sourceIdA, null, EXPAND);
        System.out.println("Read A by source id, topic '" + bySource.getTopic() + "'");

        ApiResponse<ConversationData> bySourceResponse = conversationsApi.conversationsGetBySourceIdAndChannelIdWithHttpInfo(sourceIdB, null, null);
        System.out.println("Read B by source id with status " + bySourceResponse.getStatusCode());

        ConversationsAddParticipantBody addBody = new ConversationsAddParticipantBody();
        addBody.setPersonId(participant.getId());
        conversationsApi.conversationsAddParticipant(a.getId(), addBody, EXPAND);
        System.out.println("Added a participant to A");

        ApiResponse<ConversationData> addResponse = conversationsApi.conversationsAddParticipantWithHttpInfo(b.getId(), addBody, null);
        System.out.println("Added a participant to B with status " + addResponse.getStatusCode());

        // A second participant, whose visibility can be changed: the assigned agent's
        // participation cannot be hidden.
        ConversationsAddParticipantBody addOtherBody = new ConversationsAddParticipantBody();
        addOtherBody.setPersonId(other.getId());
        conversationsApi.conversationsAddParticipant(a.getId(), addOtherBody, null);
        conversationsApi.conversationsAddParticipant(b.getId(), addOtherBody, null);

        ConversationsChangeParticipantVisibilityBody visibilityBody = new ConversationsChangeParticipantVisibilityBody();
        visibilityBody.setPersonId(other.getId());
        visibilityBody.setHidden(true);
        conversationsApi.conversationsChangeParticipantVisibility(a.getId(), visibilityBody, EXPAND);
        System.out.println("Set the participant visibility of A");

        ApiResponse<ConversationData> visibilityResponse = conversationsApi.conversationsChangeParticipantVisibilityWithHttpInfo(b.getId(), visibilityBody, null);
        System.out.println("Set the participant visibility of B with status " + visibilityResponse.getStatusCode());

        ConversationsSetAssigneePersonBody assigneeBody = new ConversationsSetAssigneePersonBody();
        assigneeBody.setPersonId(participant.getId());
        conversationsApi.conversationsSetAssigneePerson(a.getId(), assigneeBody, EXPAND);
        System.out.println("Set the assignee of A");

        ApiResponse<ConversationData> assigneeResponse = conversationsApi.conversationsSetAssigneePersonWithHttpInfo(b.getId(), assigneeBody, null);
        System.out.println("Set the assignee of B with status " + assigneeResponse.getStatusCode());

        ConversationsSetContextPersonBody contextBody = new ConversationsSetContextPersonBody();
        contextBody.setPersonId(visitor.getId());
        conversationsApi.conversationsSetContextPerson(a.getId(), contextBody, EXPAND);
        System.out.println("Set the context person of A");

        ApiResponse<ConversationData> contextResponse = conversationsApi.conversationsSetContextPersonWithHttpInfo(b.getId(), contextBody, null);
        System.out.println("Set the context person of B with status " + contextResponse.getStatusCode());

        conversationsApi.conversationsSetRecipient(a.getId(), other, EXPAND);
        System.out.println("Set the recipient of A");

        ApiResponse<ConversationData> recipientResponse = conversationsApi.conversationsSetRecipientWithHttpInfo(b.getId(), other, null);
        System.out.println("Set the recipient of B with status " + recipientResponse.getStatusCode());

        ConversationsSetAwaitedPersonTypeBody awaitedBody = new ConversationsSetAwaitedPersonTypeBody();
        awaitedBody.setAwaitedPersonType(EAwaitedPersonType.AGENT);
        conversationsApi.conversationsSetAwaitedPersonType(a.getId(), awaitedBody, EXPAND);
        System.out.println("Set the awaited person type of A");

        ApiResponse<ConversationData> awaitedResponse = conversationsApi.conversationsSetAwaitedPersonTypeWithHttpInfo(b.getId(), awaitedBody, null);
        System.out.println("Set the awaited person type of B with status " + awaitedResponse.getStatusCode());

        ConversationsSetLocaleBody localeBody = new ConversationsSetLocaleBody();
        localeBody.setLocale("en");
        conversationsApi.conversationsSetLocale(a.getId(), localeBody, "metadata");
        System.out.println("Set the locale of A");

        ApiResponse<ConversationData> localeResponse = conversationsApi.conversationsSetLocaleWithHttpInfo(b.getId(), localeBody, null);
        System.out.println("Set the locale of B with status " + localeResponse.getStatusCode());

        setScheduledTimestamp(conversationsApi, a.getId(), b.getId());

        ConversationsSetStarredBody starredBody = new ConversationsSetStarredBody();
        starredBody.setPersonId(participant.getId());
        starredBody.setStarred(true);
        conversationsApi.conversationsSetStarred(a.getId(), starredBody, EXPAND);
        System.out.println("Starred A");

        ApiResponse<ConversationData> starredResponse = conversationsApi.conversationsSetStarredWithHttpInfo(b.getId(), starredBody, null);
        System.out.println("Starred B with status " + starredResponse.getStatusCode());

        ConversationsSetTopicBody topicBody = new ConversationsSetTopicBody();
        topicBody.setTopic("Renamed topic of the Example Company");
        conversationsApi.conversationsSetTopic(a.getId(), topicBody, EXPAND);
        System.out.println("Set the topic of A");

        ApiResponse<ConversationData> topicResponse = conversationsApi.conversationsSetTopicWithHttpInfo(b.getId(), topicBody, null);
        System.out.println("Set the topic of B with status " + topicResponse.getStatusCode());

        ConversationsSetVisibilityBody conversationVisibilityBody = new ConversationsSetVisibilityBody();
        conversationVisibilityBody.setConversationVisibility(EConversationVisibility.PRIVATE);
        conversationsApi.conversationsSetVisibility(a.getId(), conversationVisibilityBody, EXPAND);
        System.out.println("Set the visibility of A");

        ApiResponse<ConversationData> conversationVisibilityResponse = conversationsApi.conversationsSetVisibilityWithHttpInfo(b.getId(), conversationVisibilityBody, null);
        System.out.println("Set the visibility of B with status " + conversationVisibilityResponse.getStatusCode());

        ConversationsSetVisitorDataBody visitorBody = new ConversationsSetVisitorDataBody();
        visitorBody.setVisitorData("example visitor data");
        conversationsApi.conversationsSetVisitorData(a.getId(), visitorBody, EXPAND);
        System.out.println("Set the visitor data of A");

        ApiResponse<ConversationData> visitorResponse = conversationsApi.conversationsSetVisitorDataWithHttpInfo(b.getId(), visitorBody, null);
        System.out.println("Set the visitor data of B with status " + visitorResponse.getStatusCode());

        conversationsApi.conversationsUpdateConfiguration(a.getId(), configurationUpdate(conversationsApi, a.getId()), EXPAND);
        System.out.println("Updated the configuration of A");

        ApiResponse<ConversationData> configurationResponse = conversationsApi.conversationsUpdateConfigurationWithHttpInfo(
                b.getId(), configurationUpdate(conversationsApi, b.getId()), null);
        System.out.println("Updated the configuration of B with status " + configurationResponse.getStatusCode());

        conversationsApi.conversationsUpdateMetadata(a.getId(), metadataUpdate(conversationsApi, a.getId()), EXPAND);
        System.out.println("Updated the metadata of A");

        ApiResponse<ConversationData> metadataResponse = conversationsApi.conversationsUpdateMetadataWithHttpInfo(
                b.getId(), metadataUpdate(conversationsApi, b.getId()), null);
        System.out.println("Updated the metadata of B with status " + metadataResponse.getStatusCode());

        conversationsApi.conversationsUpdateText(a.getId(), textUpdate(conversationsApi, a.getId()), EXPAND);
        System.out.println("Updated the text of A");

        ApiResponse<ConversationData> textResponse = conversationsApi.conversationsUpdateTextWithHttpInfo(
                b.getId(), textUpdate(conversationsApi, b.getId()), null);
        System.out.println("Updated the text of B with status " + textResponse.getStatusCode());

        ConversationsOffboardParticipantBody offboardBody = new ConversationsOffboardParticipantBody();
        offboardBody.setPersonId(participant.getId());
        offboardBody.setReason(EConversationLeftReason.PARTICIPANT_LEFT);
        conversationsApi.conversationsOffboardParticipant(a.getId(), offboardBody, EXPAND);
        System.out.println("Offboarded the participant of A");

        ApiResponse<ConversationData> offboardResponse = conversationsApi.conversationsOffboardParticipantWithHttpInfo(b.getId(), offboardBody, null);
        System.out.println("Offboarded the participant of B with status " + offboardResponse.getStatusCode());

        addExternalParticipant(conversationsApi, a.getId(), b.getId());

        ConversationsEndBody endBody = new ConversationsEndBody();
        endBody.setReason(EConversationEndReason.ENDED_BY_PARTICIPANT);
        conversationsApi.conversationsEnd(a.getId(), endBody, EXPAND);
        System.out.println("Ended A");

        ApiResponse<ConversationData> endResponse = conversationsApi.conversationsEndWithHttpInfo(b.getId(), endBody, null);
        System.out.println("Ended B with status " + endResponse.getStatusCode());
    }

    /*
     * A configuration update has to carry the version of the properties it is based on,
     * otherwise the server answers "Update of the 'configuration' properties is not
     * possible because of missing '$_version' key".
     */
    private static Map<String, String> configurationUpdate(ConversationsApi conversationsApi, String conversationId) throws Exception {
        ConversationData conversation = conversationsApi.conversationsRead(conversationId, "configuration");
        Map<String, String> configuration = new HashMap<>();
        configuration.put("$_version", conversation.getConfiguration().get("$_version"));
        return configuration;
    }

    /** The metadata update needs its own version key, exactly like the configuration. */
    private static Map<String, String> metadataUpdate(ConversationsApi conversationsApi, String conversationId) throws Exception {
        ConversationData conversation = conversationsApi.conversationsRead(conversationId, "metadata");
        Map<String, String> metadata = new HashMap<>();
        metadata.put("$_version", conversation.getMetadata().get("$_version"));
        metadata.put("example-key", "example-value");
        return metadata;
    }

    /** The text update carries its version the same way, nested under the locale key. */
    private static Map<String, Map<String, String>> textUpdate(ConversationsApi conversationsApi, String conversationId) throws Exception {
        ConversationData conversation = conversationsApi.conversationsRead(conversationId, "text");
        Map<String, Map<String, String>> text = new HashMap<>(conversation.getText());
        return text;
    }

    /*
     * conversationsSetScheduledTimestamp only applies to a conversation whose engagement
     * type is SCHEDULED_CONVERSATION, which these two are not. Both calls are expected
     * to fail; they are here because the 'expand' parameter of both changed in 8.1.2.
     */
    private static void setScheduledTimestamp(ConversationsApi conversationsApi, String conversationA, String conversationB) {
        ConversationsSetScheduledTimestampBody body = new ConversationsSetScheduledTimestampBody();
        body.setScheduledTimestamp(System.currentTimeMillis() + 3600000L);
        try {
            conversationsApi.conversationsSetScheduledTimestamp(conversationA, body, EXPAND);
            conversationsApi.conversationsSetScheduledTimestampWithHttpInfo(conversationB, body, null);
        } catch (ApiException e) {
            System.out.println("conversationsSetScheduledTimestamp needs a scheduled conversation, got HTTP " + e.getCode());
        }
    }

    /*
     * conversationsAddExternalParticipant needs the id of a contact on an external
     * messenger channel, which means a configured channel and a contact on it. That is
     * more infrastructure than this example builds, so both calls are expected to fail;
     * they are here because the 'expand' parameter of both changed in 8.1.2.
     */
    private static void addExternalParticipant(ConversationsApi conversationsApi, String conversationA, String conversationB) {
        ConversationsAddExternalParticipantBody body = new ConversationsAddExternalParticipantBody();
        body.setExternalMessengerContactId("example-external-contact-" + Util.randomSuffix());
        try {
            conversationsApi.conversationsAddExternalParticipant(conversationA, body, EXPAND);
            conversationsApi.conversationsAddExternalParticipantWithHttpInfo(conversationB, body, null);
        } catch (ApiException e) {
            System.out.println("conversationsAddExternalParticipant needs an external messenger contact, got HTTP " + e.getCode());
        }
    }

    private static ConversationCreationData newConversation(String sourceId, String participantId) {
        ConversationCreationData conversation = new ConversationCreationData();
        conversation.set$Type(ConversationCreationData.TypeEnum.CONVERSATIONCREATIONDATA);
        conversation.setTopic("Conversation of the Example Company");
        conversation.setInitialEngagementType(EInitialEngagementType.CHAT_REQUEST);
        conversation.setConversationVisibility(EConversationVisibility.PRIVATE);
        conversation.setSourceId(sourceId);

        // A CHAT_REQUEST needs a context person, which is expressed as a participant
        // with that participation type.
        ConversationCreationParticipantData contextPerson = new ConversationCreationParticipantData();
        contextPerson.set$Type(ConversationCreationParticipantData.TypeEnum.CONVERSATIONCREATIONPARTICIPANTDATA);
        contextPerson.setPersonId(participantId);
        contextPerson.setParticipationType(EConversationRealParticipationType.CONTEXT_PERSON);
        conversation.setParticipants(Arrays.asList(contextPerson));

        return conversation;
    }

    private static PersonData newVisitor() {
        PersonData person = newPerson(EPersonType.VISITOR, EAuthorizationRole.WEBUSER);
        return person;
    }

    private static PersonData newAgent() {
        return newPerson(EPersonType.AGENT, EAuthorizationRole.REGISTERED_USER);
    }

    private static PersonData newPerson(EPersonType personType, EAuthorizationRole role) {
        String unique = Util.randomSuffix();
        PersonData person = new PersonData();
        person.set$Type(PersonData.TypeEnum.PERSONDATA);
        person.setPersonType(personType);
        person.setAuthorizationRole(role);
        person.setSourceId("example-agent-" + unique);
        person.setUsername("example-agent-" + unique);
        person.setEmail("jane.smith+" + unique + "@example.com");
        return person;
    }
}
