///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.21.0
//SOURCES Util.java

/*
 * Creates a branch, reads it back and deletes it again.
 *
 * Targets the finding of revapi/8.21.0_8.22.1/jersey3-client-v4:
 *
 *   java.method.numberOfParametersChanged
 *   old: BranchesApi::branchesRead(java.lang.String)
 *   new: BranchesApi::branchesRead(java.lang.String, java.util.List<ExpandFields>)
 *
 * In 8.21.0 the branch operations have no 'expand' parameter at all; from 8.22.1 on
 * every one of them takes an additional List<ExpandFields>. Since no overload with the
 * old parameter list was kept, this example compiles against 8.21.0 and no longer
 * compiles when the dependency above is changed to 8.22.1.
 *
 * Usage:
 *   ./ReadBranchWithoutExpandParameter.java <server> --superadmin <username> --password <password>
 */

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.BranchesApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.model.v4.BranchData;
import com.unblu.webapi.model.v4.EBranchState;

public class ReadBranchWithoutExpandParameter {

    private static final String SCRIPT_NAME = "ReadBranchWithoutExpandParameter.java";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseSuperadminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        BranchesApi branchesApi = new BranchesApi(client);

        BranchData branch = new BranchData();
        branch.setName("example-branch-" + Util.randomSuffix());
        branch.setDescription("Branch of the Example Company");
        branch.setState(EBranchState.ACTIVE);
        branch.setStreetAddress("123 Example Street");
        branch.setZip("12345");
        branch.setCity("Example City");
        branch.setRegion("Example Region");
        branch.setCountry("Exampleland");
        // The server rejects the creation with "Width and height can't be null if no
        // floor plan image is provided", so both are always set here.
        branch.setWidth(800);
        branch.setHeight(600);

        BranchData createdBranch = branchesApi.branchesCreate(branch);
        System.out.println("Created branch '" + createdBranch.getName() + "' with id '" + createdBranch.getId() + "'");

        // In 8.21.0 branchesRead(..) takes the branch id and nothing else.
        BranchData readBranch = branchesApi.branchesRead(createdBranch.getId());
        System.out.println("Read branch '" + readBranch.getName() + "' in '" + readBranch.getCity() + "'");

        branchesApi.branchesDelete(createdBranch.getId());
        System.out.println("Deleted branch '" + createdBranch.getId() + "'");
    }
}
