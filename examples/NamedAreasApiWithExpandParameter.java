///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS com.unblu.openapi:jersey3-client-v4:8.0.1
//SOURCES Util.java

/*
 * Exercises every NamedAreasApi operation that takes an 'expand' parameter.
 *
 * Targets the findings of revapi/8.0.1_8.1.2/jersey3-client-v4 for NamedAreasApi:
 *
 *   java.method.parameterTypeChanged  (10 findings)
 *   old: ...(<body>, java.lang.String)
 *   new: ...(<body>, java.util.List<ExpandFields>)
 *
 *   namedAreasCreate, namedAreasRead, namedAreasReadMultiple, namedAreasSearch,
 *   namedAreasUpdate — and the WithHttpInfo variant of each.
 *
 * In 8.0.1 'expand' is a comma separated String; from 8.1.2 on it is a
 * List<ExpandFields>. This example compiles against 8.0.1 and no longer compiles when
 * the dependency above is changed to 8.1.2.
 *
 * Usage:
 *   ./NamedAreasApiWithExpandParameter.java <server> --admin <username> --password <password>
 */

import java.util.Arrays;
import com.unblu.webapi.jersey.v4.V4ApiUtil;
import com.unblu.webapi.jersey.v4.api.NamedAreasApi;
import com.unblu.webapi.jersey.v4.invoker.ApiClient;
import com.unblu.webapi.jersey.v4.invoker.ApiResponse;
import com.unblu.webapi.model.v4.ENamedAreaType;
import com.unblu.webapi.model.v4.NamedArea;
import com.unblu.webapi.model.v4.NamedAreaList;
import com.unblu.webapi.model.v4.NamedAreaQuery;
import com.unblu.webapi.model.v4.NamedAreaResult;

public class NamedAreasApiWithExpandParameter {

    private static final String SCRIPT_NAME = "NamedAreasApiWithExpandParameter.java";

    /** In 8.0.1 the fields to expand are given as a single comma separated String. */
    private static final String EXPAND = "configuration,text";

    public static void main(String[] args) throws Exception {
        Util arguments = Util.parseAdminArgs(args, SCRIPT_NAME);

        ApiClient client = V4ApiUtil.getWithBasicAuth(arguments.getServerUrl(), arguments.getUsername(), arguments.getPassword());
        Util.applyRecommendedDeserialization(client.getJSON().getContext(null));
        NamedAreasApi namedAreasApi = new NamedAreasApi(client);

        NamedArea created = namedAreasApi.namedAreasCreate(newNamedArea(), "configuration,text");
        System.out.println("Created named area '" + created.getName() + "' (" + created.getId() + ")");

        ApiResponse<NamedArea> createResponse = namedAreasApi.namedAreasCreateWithHttpInfo(newNamedArea(), EXPAND);
        System.out.println("Created another with status " + createResponse.getStatusCode());
        String secondId = createResponse.getData().getId();

        NamedArea read = namedAreasApi.namedAreasRead(created.getId(), EXPAND);
        System.out.println("Read by id: '" + read.getName() + "'");

        ApiResponse<NamedArea> readResponse = namedAreasApi.namedAreasReadWithHttpInfo(created.getId(), null);
        System.out.println("Read by id with status " + readResponse.getStatusCode());

        NamedAreaList list = namedAreasApi.namedAreasReadMultiple(Arrays.asList(created.getId(), secondId), EXPAND);
        System.out.println("Read multiple: " + list.getItems().size() + " named area(s)");

        ApiResponse<NamedAreaList> listResponse = namedAreasApi.namedAreasReadMultipleWithHttpInfo(Arrays.asList(created.getId()), EXPAND);
        System.out.println("Read multiple with status " + listResponse.getStatusCode());

        NamedAreaResult searchResult = namedAreasApi.namedAreasSearch(new NamedAreaQuery(), EXPAND);
        System.out.println("Search found " + searchResult.getItems().size() + " named area(s)");

        ApiResponse<NamedAreaResult> searchResponse = namedAreasApi.namedAreasSearchWithHttpInfo(new NamedAreaQuery(), EXPAND);
        System.out.println("Search with status " + searchResponse.getStatusCode());

        read.setDescription("Renamed named area of the Example Company");
        NamedArea updated = namedAreasApi.namedAreasUpdate(read, EXPAND);
        System.out.println("Updated description to '" + updated.getDescription() + "'");

        ApiResponse<NamedArea> updateResponse = namedAreasApi.namedAreasUpdateWithHttpInfo(updated, EXPAND);
        System.out.println("Updated again with status " + updateResponse.getStatusCode());

        namedAreasApi.namedAreasDelete(secondId);
        namedAreasApi.namedAreasDelete(created.getId());
        System.out.println("Deleted both named areas");
    }

    private static NamedArea newNamedArea() {
        String unique = Util.randomSuffix();
        NamedArea namedArea = new NamedArea();
        namedArea.set$Type(NamedArea.TypeEnum.NAMEDAREA);
        namedArea.setName("example-named-area-" + unique);
        namedArea.setDescription("Named area of the Example Company");
        namedArea.setSiteId("example-site-" + unique);
        namedArea.setType(ENamedAreaType.META_TAG);
        return namedArea;
    }
}
