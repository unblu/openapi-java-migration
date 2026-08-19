///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.17.1
//SOURCES Util.java

/*
 * Creates a branch whose branch client has one auxiliary camera, reads it back and
 * deletes the branch again.
 *
 * Targets the finding of revapi/8.17.1_8.18.0/models-v4:
 *
 *   java.class.removed  enum EBranchClientAuxiliaryCameraType
 *
 * The enum was renamed to EAuxiliaryCameraType in 8.18.0 and the old name removed, so
 * every caller setting the type of an auxiliary camera has to be touched. This example
 * compiles against 8.17.1 and no longer compiles when the dependency above is changed
 * to 8.18.0.
 *
 * Usage:
 *   ./CreateBranchClientAuxiliaryCamera.java <server> --superadmin <username> --password <password>
 */

import java.util.Arrays;
import java.util.UUID;

import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.BranchesApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.model.v4.BranchClientAuxiliaryCameraData;
import com.unblu.webapi.model.v4.BranchClientData;
import com.unblu.webapi.model.v4.BranchData;
import com.unblu.webapi.model.v4.EBranchClientAuxiliaryCameraType;
import com.unblu.webapi.model.v4.EBranchState;

public class CreateBranchClientAuxiliaryCamera {

    private static final String SCRIPT_NAME = "CreateBranchClientAuxiliaryCamera.java";

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
        BranchClientAuxiliaryCameraData createdCamera = createdBranchClient.getBranchClientAuxiliaryCameras().get(0);
        System.out.println("Branch client '" + createdBranchClient.getName() + "' has camera '" + createdCamera.getName() + "'");
        System.out.println("Camera type: " + createdCamera.getType());

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
        branch.setWidth(800);
        branch.setHeight(600);
        return branch;
    }

    private static BranchClientData createBranchClient() {
        BranchClientData branchClient = new BranchClientData();
        branchClient.setName("example-branch-client");
        // The branch client key is limited to 36 characters (the length of a UUID).
        branchClient.setBranchClientKey(UUID.randomUUID().toString());
        branchClient.setMainCameraMediaDeviceLabel("main-camera-device-label");
        branchClient.setMicrophoneMediaDeviceLabel("microphone-device-label");
        branchClient.setX(0);
        branchClient.setY(0);
        branchClient.setSize(20);
        branchClient.setRotation(0);
        branchClient.setBranchClientAuxiliaryCameras(Arrays.asList(createAuxiliaryCamera()));
        return branchClient;
    }

    private static BranchClientAuxiliaryCameraData createAuxiliaryCamera() {
        BranchClientAuxiliaryCameraData camera = new BranchClientAuxiliaryCameraData();
        camera.setName("example-document-camera");
        // In 8.17.1 the type is an EBranchClientAuxiliaryCameraType.
        camera.setType(EBranchClientAuxiliaryCameraType.DOCUMENT);
        camera.setMediaDeviceLabel("document-camera-device-label");
        camera.setIconId("icon_id");
        camera.setX(0);
        camera.setY(0);
        camera.setSize(20);
        camera.setRotation(0);
        camera.setAlwaysDisplayDuringCall(true);
        camera.setDisplayInFloorPlan(true);
        return camera;
    }
}
