///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.16.2
//SOURCES Util.java

/*
 * Creates a branch with a branch client holding one link, reads the branch client back
 * and deletes the branch again.
 *
 * Targets the findings of revapi/8.16.2_8.17.1/models-v4:
 *
 *   java.method.removed            BranchClientLinkData::setAutoOpen(java.lang.Boolean)
 *   java.method.removed            BranchClientLinkData::isAutoOpen()
 *   java.method.removed            BranchClientLinkData::autoOpen(java.lang.Boolean)
 *   java.field.removedWithConstant BranchClientLinkData.JSON_PROPERTY_AUTO_OPEN
 *
 * The single 'autoOpen' flag of a branch client link was replaced in 8.17.1 by three
 * more specific ones: 'autoOpenOnConnect', 'autoOpenOnCallEnd' and
 * 'autoOpenOnConversationEnd'. The old property was removed outright, so this example
 * compiles against 8.16.2 and no longer compiles when the dependency above is changed
 * to 8.17.1.
 *
 * Usage:
 *   ./CreateBranchClientLinkWithAutoOpen.java <server> --superadmin <username> --password <password>
 */

import java.util.Arrays;
import java.util.UUID;

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.BranchesApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.model.v4.BranchClientData;
import com.unblu.webapi.model.v4.BranchClientLinkData;
import com.unblu.webapi.model.v4.BranchData;
import com.unblu.webapi.model.v4.EBranchState;

public class CreateBranchClientLinkWithAutoOpen {

    private static final String SCRIPT_NAME = "CreateBranchClientLinkWithAutoOpen.java";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseSuperadminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        BranchesApi branchesApi = new BranchesApi(client);

        BranchData branch = createBranch();
        branch.setBranchClients(Arrays.asList(createBranchClient()));

        BranchData createdBranch = branchesApi.branchesCreate(branch);
        System.out.println("Created branch '" + createdBranch.getName() + "' with id '" + createdBranch.getId() + "'");

        BranchClientData createdBranchClient = createdBranch.getBranchClients().get(0);
        BranchClientLinkData createdLink = createdBranchClient.getBranchClientLinks().get(0);
        System.out.println("Branch client '" + createdBranchClient.getName() + "' has link '" + createdLink.getName() + "'");
        System.out.println("Link url: '" + createdLink.getUrl() + "', autoOpen: " + createdLink.isAutoOpen());

        branchesApi.branchesDelete(createdBranch.getId());
        System.out.println("Deleted branch '" + createdBranch.getId() + "'");
    }

    private static BranchData createBranch() {
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
        return branch;
    }

    private static BranchClientData createBranchClient() {
        BranchClientData branchClient = new BranchClientData();
        branchClient.setName("example-branch-client");
        // The branch client key is limited to 36 characters (the length of a UUID).
        // A longer value makes the server answer 500 instead of 400.
        branchClient.setBranchClientKey(UUID.randomUUID().toString());
        branchClient.setMainCameraMediaDeviceLabel("main-camera-device-label");
        branchClient.setMicrophoneMediaDeviceLabel("microphone-device-label");
        branchClient.setX(0);
        branchClient.setY(0);
        branchClient.setSize(20);
        branchClient.setRotation(0);
        branchClient.setBranchClientLinks(Arrays.asList(createBranchClientLink()));
        return branchClient;
    }

    private static BranchClientLinkData createBranchClientLink() {
        BranchClientLinkData link = new BranchClientLinkData();
        link.setName("example-link");
        link.setUrl("https://www.example.com");
        link.setLinkTarget("_blank");
        link.setIconId("icon_id");
        // In 8.16.2 a single flag decides whether the link opens automatically.
        link.setAutoOpen(true);
        return link;
    }
}
