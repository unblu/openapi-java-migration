///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.31.2

/*
 * Sorts message payloads by their type, singling out the WhatsApp template messages.
 *
 * Targets the finding of revapi/8.31.2_8.32.2/models-v4:
 *
 *   java.field.removed  field EMessageType.WHATSAPP_TEMPLATE
 *
 * The constant was removed in 8.32.2, so any code branching on it stops compiling. This
 * example compiles against 8.31.2 and no longer compiles when the dependency above is
 * changed to 8.32.2.
 *
 * It takes no arguments: the messages are built locally, no server is involved.
 *
 * Usage:
 *   ./FilterMessagesByWhatsAppTemplateType.java
 */

import com.unblu.webapi.model.v4.EMessageType;
import com.unblu.webapi.model.v4.TextMessageData;

public class FilterMessagesByWhatsAppTemplateType {

    public static void main(String[] args) throws Exception {
        TextMessageData templateMessage = newMessage(EMessageType.WHATSAPP_TEMPLATE, "Your appointment is confirmed");
        TextMessageData chatMessage = newMessage(EMessageType.TEXT, "Hello");

        for (TextMessageData message : new TextMessageData[] { templateMessage, chatMessage }) {
            System.out.println(describe(message.getType()) + ": " + message.getText());
        }
    }

    private static TextMessageData newMessage(EMessageType type, String text) {
        TextMessageData message = new TextMessageData();
        message.set$Type(TextMessageData.TypeEnum.TEXTMESSAGEDATA);
        message.setType(type);
        message.setText(text);
        return message;
    }

    private static String describe(EMessageType type) {
        if (type == EMessageType.WHATSAPP_TEMPLATE) {
            return "WhatsApp template message";
        }
        return "Ordinary message of type " + type;
    }
}
