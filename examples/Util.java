import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Command line argument parsing shared by all examples.
 *
 * Every example is started the same way:
 *
 *   ./SomeExample.java <server> --superadmin <username> --password <password>
 *   ./SomeExample.java <server> --admin <username> --password <password>
 *
 * where &lt;server&gt; is the url of the collaboration server, including the web
 * application path, for example https://unblu.example.com/app
 *
 * This class deliberately does not use any type of the generated client, so that it can
 * be shared by examples that are compiled against different versions of it.
 */
public class Util {

    public static final String SUPERADMIN = "--superadmin";
    public static final String ADMIN = "--admin";

    private static final String PASSWORD = "--password";

    private final String serverUrl;
    private final String username;
    private final String password;

    private Util(String serverUrl, String username, String password) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
    }

    /**
     * Parses the arguments of an example expecting a super admin user.
     *
     * @param args       the arguments of the main method
     * @param scriptName the file name of the example, used in the usage message
     */
    public static Util parseSuperadminArgs(String[] args, String scriptName) {
        return parseArgs(args, scriptName, SUPERADMIN);
    }

    /**
     * Parses the arguments of an example expecting an account admin user.
     *
     * @param args       the arguments of the main method
     * @param scriptName the file name of the example, used in the usage message
     */
    public static Util parseAdminArgs(String[] args, String scriptName) {
        return parseArgs(args, scriptName, ADMIN);
    }

    private static Util parseArgs(String[] args, String scriptName, String userOption) {
        if (args.length != 5) {
            exitWithUsage(scriptName, userOption);
        }

        String serverUrl = args[0];
        if (serverUrl.startsWith("--")) {
            exitWithUsage(scriptName, userOption);
        }

        String username = readOption(args, userOption, scriptName, userOption);
        String password = readOption(args, PASSWORD, scriptName, userOption);

        return new Util(removeTrailingSlash(serverUrl), username, password);
    }

    private static String readOption(String[] args, String option, String scriptName, String userOption) {
        for (int i = 1; i < args.length - 1; i++) {
            if (option.equals(args[i])) {
                return args[i + 1];
            }
        }
        System.err.println("Missing option: " + option);
        exitWithUsage(scriptName, userOption);
        return null;
    }

    private static String removeTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static void exitWithUsage(String scriptName, String userOption) {
        System.err.println("Usage: " + scriptName + " <server> " + userOption + " <username> " + PASSWORD + " <password>");
        System.err.println();
        System.err.println("  <server>  url of the collaboration server, for example https://unblu.example.com/app");
        System.exit(1);
    }

    /**
     * Applies the deserialization settings recommended for every consumer of the
     * generated client:
     *
     * <ul>
     * <li>{@code FAIL_ON_UNKNOWN_PROPERTIES = false} so that a property added to a
     *     response after this client was generated is ignored instead of failing;
     * <li>{@code READ_UNKNOWN_ENUM_VALUES_AS_NULL = true} so that an enum value added
     *     after this client was generated deserializes to {@code null} instead of
     *     throwing.
     * </ul>
     *
     * Both matter more than they look. The generated enums are closed: without the
     * second setting, an ordinary read fails outright as soon as the server returns a
     * value the client does not know, and the whole response is lost rather than the
     * single field. Adding an enum value is a routine, non-breaking change on the
     * server, so this is not a rare situation.
     *
     * Call it on the mapper of the client:
     *
     * <pre>Util.applyRecommendedDeserialization(client.getJSON().getContext(null));</pre>
     */
    public static void applyRecommendedDeserialization(ObjectMapper mapper) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }

    /**
     * A short random suffix, for names that have to be unique on the server. Timestamps
     * are not good enough: two calls in the same millisecond collide, and a run that
     * creates several entities of the same kind needs a fresh value for each one.
     */
    public static String randomSuffix() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
