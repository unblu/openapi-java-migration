///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.36.1

/*
 * Parses the payloads a bot webhook endpoint receives from the collaboration server.
 *
 * Targets the findings of revapi/8.36.1_8.37.2/models-v4:
 *
 *   java.class.removed  class BotDialogOpenEvent
 *   java.class.removed  class BotDialogMessageEvent
 *   java.class.removed  class BotDialogMessageStateEvent
 *   java.class.removed  class BotDialogClosedEvent
 *   java.class.removed  class BotDialogCounterpartChanged
 *   java.class.removed  class BotOnboardingOfferEvent
 *   java.class.removed  class BotOffboardingOfferEvent
 *   java.class.removed  class BotReboardingOfferEvent
 *
 * The eight classes describing what a bot webhook receives were all removed in 8.37.2.
 * A middleware that deserializes these payloads stops compiling. This example compiles
 * against 8.36.1 and no longer compiles when the dependency above is changed to 8.37.2.
 *
 * This is the only example in this folder that takes no arguments: a webhook receiver
 * parses what the server sends to it, it does not call the server. Everything it needs
 * is the payload itself.
 *
 * Usage:
 *   ./DeserializeBotDialogEvents.java
 */

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.model.v4.BotDialogClosedEvent;
import com.unblu.webapi.model.v4.BotDialogMessageEvent;
import com.unblu.webapi.model.v4.BotDialogOpenEvent;
import com.unblu.webapi.model.v4.BotOnboardingOfferEvent;

public class DeserializeBotDialogEvents {

    private static final String DIALOG_TOKEN = "example-dialog-token";

    private static final String OPEN_EVENT_PAYLOAD = "{"
            + "\"$_type\":\"BotDialogOpenEvent\","
            + "\"eventType\":\"bot.dialog.open\","
            + "\"timestamp\":1700000000000,"
            + "\"accountId\":\"example-account-id\","
            + "\"dialogToken\":\"" + DIALOG_TOKEN + "\","
            + "\"dialogType\":\"ONBOARDING\""
            + "}";

    private static final String MESSAGE_EVENT_PAYLOAD = "{"
            + "\"$_type\":\"BotDialogMessageEvent\","
            + "\"eventType\":\"bot.dialog.message\","
            + "\"timestamp\":1700000001000,"
            + "\"accountId\":\"example-account-id\","
            + "\"dialogToken\":\"" + DIALOG_TOKEN + "\","
            + "\"conversationId\":\"example-conversation-id\""
            + "}";

    private static final String ONBOARDING_OFFER_PAYLOAD = "{"
            + "\"$_type\":\"BotOnboardingOfferEvent\","
            + "\"eventType\":\"bot.onboarding.offer\","
            + "\"timestamp\":1700000002000,"
            + "\"accountId\":\"example-account-id\","
            + "\"dialogToken\":\"" + DIALOG_TOKEN + "\""
            + "}";

    private static final String CLOSED_EVENT_PAYLOAD = "{"
            + "\"$_type\":\"BotDialogClosedEvent\","
            + "\"eventType\":\"bot.dialog.closed\","
            + "\"timestamp\":1700000003000,"
            + "\"accountId\":\"example-account-id\","
            + "\"dialogToken\":\"" + DIALOG_TOKEN + "\""
            + "}";

    public static void main(String[] args) throws Exception {
        BotDialogOpenEvent openEvent = V4ApiUtil.deserializeJson(OPEN_EVENT_PAYLOAD, BotDialogOpenEvent.class);
        System.out.println("Dialog opened: token '" + openEvent.getDialogToken() + "', type " + openEvent.getDialogType());

        BotDialogMessageEvent messageEvent = V4ApiUtil.deserializeJson(MESSAGE_EVENT_PAYLOAD, BotDialogMessageEvent.class);
        System.out.println("Message received in conversation '" + messageEvent.getConversationId() + "'");

        BotOnboardingOfferEvent onboardingOfferEvent = V4ApiUtil.deserializeJson(ONBOARDING_OFFER_PAYLOAD, BotOnboardingOfferEvent.class);
        System.out.println("Onboarding offered at " + onboardingOfferEvent.getTimestamp());

        BotDialogClosedEvent closedEvent = V4ApiUtil.deserializeJson(CLOSED_EVENT_PAYLOAD, BotDialogClosedEvent.class);
        System.out.println("Dialog closed: token '" + closedEvent.getDialogToken() + "'");
    }
}
