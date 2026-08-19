///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.0.1
//SOURCES Util.java

/*
 * Creates a team, asking the server to expand two fields of the response, then deletes
 * it again.
 *
 * Targets the finding of revapi/8.0.1_8.1.2/jersey3-client-v4:
 *
 *   java.method.parameterTypeChanged
 *   old: TeamsApi::teamsCreate(Team, java.lang.String)
 *   new: TeamsApi::teamsCreate(Team, java.util.List<ExpandFields>)
 *
 * In 8.0.1 the 'expand' parameter is a comma separated String; from 8.1.2 on it is a
 * List<ExpandFields>. This example compiles against 8.0.1 and no longer compiles when
 * the dependency above is changed to 8.1.2.
 *
 * Usage:
 *   ./CreateTeamWithExpandParameter.java <server> --admin <username> --password <password>
 */

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.TeamsApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.model.v4.Team;

public class CreateTeamWithExpandParameter {

    private static final String SCRIPT_NAME = "CreateTeamWithExpandParameter.java";

    /** In 8.0.1 the fields to expand are given as a single comma separated String. */
    private static final String EXPAND = "configuration,text";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        TeamsApi teamsApi = new TeamsApi(client);

        Team team = new Team();
        team.set$Type(Team.TypeEnum.TEAM);
        team.setName("example-team-" + Util.randomSuffix());
        team.setDescription("Team of the Example Company support agents");

        Team createdTeam = teamsApi.teamsCreate(team, EXPAND);
        System.out.println("Created team '" + createdTeam.getName() + "' with id '" + createdTeam.getId() + "'");

        Team readTeam = teamsApi.teamsRead(createdTeam.getId(), EXPAND);
        System.out.println("Read team '" + readTeam.getName() + "', description: '" + readTeam.getDescription() + "'");

        teamsApi.teamsDelete(createdTeam.getId());
        System.out.println("Deleted team '" + createdTeam.getId() + "'");
    }
}
