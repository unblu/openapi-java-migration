///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.8.1

/*
 * Builds the deputy-delegation search filters that select on the escalation level.
 *
 * Targets the findings of revapi/8.8.1_8.8.2/models-v4:
 *
 *   java.method.parameterTypeChanged        DeputyRelationship::escalationLevel(..),
 *                                           ::setEscalationLevel(..) and the value/values
 *                                           setters of the four operators
 *   java.method.returnTypeChanged           ::getEscalationLevel(), ::getValue()
 *   java.method.returnTypeTypeParametersChanged  ::getValues()
 *
 * In 8.8.2 the enum EDeputyDelegationEscalationLevel was replaced by
 * EAssistantAndDeputyDelegationEscalationLevel everywhere it appeared, so every
 * signature mentioning it changed. This example compiles against 8.8.1 and no longer
 * compiles when the dependency above is changed to 8.8.2.
 *
 * It takes no arguments: the filters are built locally, no server is involved.
 *
 * Usage:
 *   ./SearchDeputiesByEscalationLevel.java
 */

import java.util.ArrayList;
import java.util.Arrays;
import com.unblu.webapi.model.v4.DeputyRelationship;
import com.unblu.webapi.model.v4.EDeputyDelegationEscalationLevel;
import com.unblu.webapi.model.v4.EqualsDeputyDelegationEscalationLevelOperator;
import com.unblu.webapi.model.v4.InDeputyDelegationEscalationLevelOperator;
import com.unblu.webapi.model.v4.NotEqualsDeputyDelegationEscalationLevelOperator;
import com.unblu.webapi.model.v4.NotInDeputyDelegationEscalationLevelOperator;

public class SearchDeputiesByEscalationLevel {

    public static void main(String[] args) throws Exception {
        EDeputyDelegationEscalationLevel first = EDeputyDelegationEscalationLevel.values()[0];
        EDeputyDelegationEscalationLevel second = EDeputyDelegationEscalationLevel.values()[1];

        DeputyRelationship relationship = new DeputyRelationship();
        relationship.setEscalationLevel(first);
        System.out.println("Relationship escalation level: " + relationship.getEscalationLevel());

        DeputyRelationship fluent = new DeputyRelationship().escalationLevel(second);
        System.out.println("Second relationship escalation level: " + fluent.getEscalationLevel());

        EqualsDeputyDelegationEscalationLevelOperator equals = new EqualsDeputyDelegationEscalationLevelOperator();
        equals.setValue(first);
        System.out.println("equals -> " + equals.getValue());

        NotEqualsDeputyDelegationEscalationLevelOperator notEquals = new NotEqualsDeputyDelegationEscalationLevelOperator();
        notEquals.setValue(second);
        System.out.println("notEquals -> " + notEquals.getValue());

        InDeputyDelegationEscalationLevelOperator in = new InDeputyDelegationEscalationLevelOperator()
                .values(new ArrayList<>(Arrays.asList(first, second)));
        in.addValuesItem(first);
        System.out.println("in -> " + in.getValues());

        NotInDeputyDelegationEscalationLevelOperator notIn = new NotInDeputyDelegationEscalationLevelOperator()
                .values(new ArrayList<>(Arrays.asList(second)));
        notIn.addValuesItem(first);
        System.out.println("notIn -> " + notIn.getValues());
    }
}
