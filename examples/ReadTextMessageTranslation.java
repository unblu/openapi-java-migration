///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.26.2

/*
 * Builds the translation of a text message and reads its translated text back.
 *
 * Targets the findings of revapi/8.26.2_8.27.2/models-v4:
 *
 *   java.method.removed  TextMessageTranslation::getTranslatedText()
 *   java.method.removed  TextMessageTranslation::setTranslatedText(java.lang.String)
 *   java.method.removed  TextMessageTranslation::translatedText(java.lang.String)
 *
 * The property was renamed to 'text' in 8.27.2 and the old accessors were removed, so
 * this example compiles against 8.26.2 and no longer compiles when the dependency above
 * is changed to 8.27.2.
 *
 * It takes no arguments: a message translation is built and read locally, no server is
 * involved.
 *
 * Usage:
 *   ./ReadTextMessageTranslation.java
 */

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.model.v4.TextMessageTranslation;

public class ReadTextMessageTranslation {

    public static void main(String[] args) throws Exception {
        TextMessageTranslation translation = new TextMessageTranslation();
        translation.set$Type(TextMessageTranslation.TypeEnum.TEXTMESSAGETRANSLATION);
        translation.setLocale("de");
        translation.setTranslatedText("Willkommen beim Support der Example Company.");

        System.out.println("Locale: " + translation.getLocale());
        System.out.println("Translated text: " + translation.getTranslatedText());

        TextMessageTranslation fluent = new TextMessageTranslation()
                .$type(TextMessageTranslation.TypeEnum.TEXTMESSAGETRANSLATION)
                .locale("fr")
                .translatedText("Bienvenue au support de la Example Company.");
        System.out.println("Second translation: " + fluent.getTranslatedText());

        String payload = "{\"$_type\":\"TextMessageTranslation\",\"locale\":\"it\",\"translatedText\":\"Benvenuti\"}";
        TextMessageTranslation parsed = V4ApiUtil.deserializeJson(payload, TextMessageTranslation.class);
        System.out.println("Parsed translation: " + parsed.getTranslatedText());
    }
}
