///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.3.1

/*
 * Reads the reason a participant was offboarded from a conversation, and singles out
 * the one that means the external messenger channel was closed.
 *
 * Targets the finding of revapi/8.3.1_8.4.2/models-v4:
 *
 *   java.field.removed  field EOffboardingReason.EXTERNAL_CHANNEL_CLOSED
 *
 * The constant was removed in 8.4.2, so any code branching on it stops compiling. This
 * example compiles against 8.3.1 and no longer compiles when the dependency above is
 * changed to 8.4.2.
 *
 * It takes no arguments: the reasons are evaluated locally, no server is involved. The
 * constant only ever reaches a client inside a webhook payload, which is why there is
 * nothing to call here.
 *
 * Usage:
 *   ./HandleExternalChannelOffboardingReason.java
 */

import com.unblu.webapi.model.v4.EOffboardingReason;

public class HandleExternalChannelOffboardingReason {

    public static void main(String[] args) throws Exception {
        for (EOffboardingReason reason : new EOffboardingReason[] {
                EOffboardingReason.EXTERNAL_CHANNEL_CLOSED,
                EOffboardingReason.PARTICIPANT_LEFT,
                EOffboardingReason.CONVERSATION_ENDED }) {
            System.out.println(reason + " -> " + describe(reason));
        }
    }

    private static String describe(EOffboardingReason reason) {
        if (reason == EOffboardingReason.EXTERNAL_CHANNEL_CLOSED) {
            return "the external messenger channel was closed, nothing to retry";
        }
        return "the participant left for an ordinary reason";
    }
}
