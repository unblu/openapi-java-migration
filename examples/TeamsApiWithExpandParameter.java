///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.0.1
//SOURCES Util.java

/*
 * Exercises every TeamsApi operation that takes an 'expand' parameter.
 *
 * Targets the findings of revapi/8.0.1_8.1.2/jersey3-client-v4 for TeamsApi:
 *
 *   java.method.parameterTypeChanged  (14 findings)
 *   old: TeamsApi::teamsCreate(Team, java.lang.String)
 *   new: TeamsApi::teamsCreate(Team, java.util.List<ExpandFields>)
 *
 *   teamsCreate, teamsGetChildTeams, teamsGetTeamOfCurrentUser, teamsRead,
 *   teamsReadMultiple, teamsSearch, teamsUpdate — and the WithHttpInfo variant of each.
 *
 * In 8.0.1 'expand' is a comma separated String; from 8.1.2 on it is a
 * List<ExpandFields>. This example compiles against 8.0.1 and no longer compiles when
 * the dependency above is changed to 8.1.2.
 *
 * Usage:
 *   ./TeamsApiWithExpandParameter.java <server> --admin <username> --password <password>
 */

import java.util.Arrays;

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.TeamsApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.jersey.v4.invoker.ApiResponse;
import com.unblu.webapi.model.v4.Team;
import com.unblu.webapi.model.v4.TeamList;
import com.unblu.webapi.model.v4.TeamQuery;
import com.unblu.webapi.model.v4.TeamResult;

public class TeamsApiWithExpandParameter {

    private static final String SCRIPT_NAME = "TeamsApiWithExpandParameter.java";

    /**
     * In 8.0.1 the fields to expand are given as a single comma separated String.
     *
     * The calls below deliberately use every form a caller can write: this constant, an
     * inline literal, a literal with whitespace after the comma, a single-value literal,
     * and `null`. A migration recipe has to handle each of them differently -- `null` in
     * particular is already valid for the new `List<ExpandFields>` type and must be left
     * untouched -- so the examples are only useful as recipe fixtures if all the forms
     * appear somewhere.
     */
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

        Team createdTeam = teamsApi.teamsCreate(team, "configuration,text");
        System.out.println("Created team '" + createdTeam.getName() + "' (" + createdTeam.getId() + ")");

        ApiResponse<Team> createResponse = teamsApi.teamsCreateWithHttpInfo(childOf(createdTeam), EXPAND);
        System.out.println("Created a child team with status " + createResponse.getStatusCode());
        String childTeamId = createResponse.getData().getId();

        Team readTeam = teamsApi.teamsRead(createdTeam.getId(), "configuration,text");
        System.out.println("Read by id: '" + readTeam.getName() + "'");

        ApiResponse<Team> readResponse = teamsApi.teamsReadWithHttpInfo(createdTeam.getId(), null);
        System.out.println("Read by id with status " + readResponse.getStatusCode());

        TeamList teamList = teamsApi.teamsReadMultiple(Arrays.asList(createdTeam.getId(), childTeamId), EXPAND);
        System.out.println("Read multiple: " + teamList.getItems().size() + " team(s)");

        ApiResponse<TeamList> teamListResponse = teamsApi.teamsReadMultipleWithHttpInfo(Arrays.asList(createdTeam.getId()), "configuration, text");
        System.out.println("Read multiple with status " + teamListResponse.getStatusCode());

        TeamList childTeams = teamsApi.teamsGetChildTeams(createdTeam.getId(), null);
        System.out.println("Child teams: " + childTeams.getItems().size());

        ApiResponse<TeamList> childTeamsResponse = teamsApi.teamsGetChildTeamsWithHttpInfo(createdTeam.getId(), EXPAND);
        System.out.println("Child teams with status " + childTeamsResponse.getStatusCode());

        Team ownTeam = teamsApi.teamsGetTeamOfCurrentUser("text");
        System.out.println("Team of the current user: '" + (ownTeam == null ? "<none>" : ownTeam.getName()) + "'");

        ApiResponse<Team> ownTeamResponse = teamsApi.teamsGetTeamOfCurrentUserWithHttpInfo(EXPAND);
        System.out.println("Team of the current user with status " + ownTeamResponse.getStatusCode());

        TeamResult searchResult = teamsApi.teamsSearch(new TeamQuery(), EXPAND);
        System.out.println("Search found " + searchResult.getItems().size() + " team(s)");

        ApiResponse<TeamResult> searchResponse = teamsApi.teamsSearchWithHttpInfo(new TeamQuery(), "configuration");
        System.out.println("Search with status " + searchResponse.getStatusCode());

        readTeam.setDescription("Renamed team of the Example Company");
        Team updatedTeam = teamsApi.teamsUpdate(readTeam, EXPAND);
        System.out.println("Updated description to '" + updatedTeam.getDescription() + "'");

        ApiResponse<Team> updateResponse = teamsApi.teamsUpdateWithHttpInfo(updatedTeam, null);
        System.out.println("Updated again with status " + updateResponse.getStatusCode());

        teamsApi.teamsDelete(childTeamId);
        teamsApi.teamsDelete(createdTeam.getId());
        System.out.println("Deleted both teams");
    }

    private static Team childOf(Team parent) {
        Team child = new Team();
        child.set$Type(Team.TypeEnum.TEAM);
        child.setName("example-child-team-" + Util.randomSuffix());
        child.setDescription("Child team of the Example Company");
        child.setParentId(parent.getId());
        return child;
    }
}
