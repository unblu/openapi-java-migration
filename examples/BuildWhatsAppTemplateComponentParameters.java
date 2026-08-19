///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.37.2

/*
 * Builds the component parameters of a WhatsApp template message.
 *
 * Targets the findings of revapi/8.37.2_8.38.2/models-v4:
 *
 *   java.class.removed              enum ETemplateComponentParemetersType
 *   java.class.removed              enum ETemplateComponentParemetersSubType
 *   java.method.parameterTypeChanged  WhatsAppTemplateComponentParameters::setType(..),
 *                                     ::setSubType(..), ::type(..), ::subType(..)
 *   java.method.returnTypeChanged     ::getType(), ::getSubType()
 *
 * Both enums were renamed in 8.38.2 to fix a typo -- "Paremeters" became "Parameters" --
 * and the old names were removed, which changes every signature mentioning them. Note
 * that 8.38.2 also introduces a *singular* ETemplateComponentParameterType, which is a
 * different type. This example compiles against 8.37.2 and no longer compiles when the
 * dependency above is changed to 8.38.2.
 *
 * It takes no arguments: the parameters are built locally, no server is involved.
 *
 * Usage:
 *   ./BuildWhatsAppTemplateComponentParameters.java
 */

import com.unblu.webapi.model.v4.ETemplateComponentParemetersSubType;
import com.unblu.webapi.model.v4.ETemplateComponentParemetersType;
import com.unblu.webapi.model.v4.WhatsAppTemplateComponentParameters;

public class BuildWhatsAppTemplateComponentParameters {

    public static void main(String[] args) throws Exception {
        WhatsAppTemplateComponentParameters header = new WhatsAppTemplateComponentParameters();
        header.setType(ETemplateComponentParemetersType.HEADER);
        System.out.println("Header component type: " + header.getType());

        WhatsAppTemplateComponentParameters body = new WhatsAppTemplateComponentParameters();
        body.setType(ETemplateComponentParemetersType.BODY);
        System.out.println("Body component type: " + body.getType());

        WhatsAppTemplateComponentParameters button = new WhatsAppTemplateComponentParameters()
                .type(ETemplateComponentParemetersType.BUTTON)
                .subType(ETemplateComponentParemetersSubType.QUICK_REPLY);
        button.setIndex(0);
        System.out.println("Button component: " + button.getType() + " / " + button.getSubType());

        WhatsAppTemplateComponentParameters urlButton = new WhatsAppTemplateComponentParameters();
        urlButton.setType(ETemplateComponentParemetersType.BUTTON);
        urlButton.setSubType(ETemplateComponentParemetersSubType.URL);
        System.out.println("Url button component: " + urlButton.getType() + " / " + urlButton.getSubType());
    }
}
