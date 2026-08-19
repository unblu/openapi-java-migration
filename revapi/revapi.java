///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17
//DEPS org.revapi:revapi-standalone:0.12.1
//FILES logback.xml

/*
 * JBang based replacement for the `revapi.sh` script shipped with the Revapi CLI
 * distribution (https://revapi.org, org.revapi:revapi-standalone).
 *
 * It is a thin wrapper around `org.revapi.standalone.Main` and accepts exactly the
 * same options as `revapi.sh`. Because JBang resolves `revapi-standalone` and its
 * transitive dependencies from Maven, no `lib/` folder has to be checked in.
 *
 * Usage (see `./revapi.java --help` for the full option list):
 *
 *   ./revapi.java \
 *     --extensions=org.revapi:revapi-java:0.28.4,org.revapi:revapi-reporter-text:0.15.1 \
 *     --old-gavs com.unblu.openapi:models-v4:8.39.3 \
 *     --new-gavs com.unblu.openapi:models-v4:8.40.4 \
 *     -Drevapi.java.missing-classes.behavior=report
 *
 * `revapi.sh` passes its own installation directory to `Main`, which uses it as the
 * parent of the default extension cache (`<base dir>/cache`). JBang scripts have no
 * such installation directory, so the base directory is resolved from, in order:
 *
 *   1. the `revapi.home` system property,
 *   2. the `REVAPI_HOME` environment variable,
 *   3. `<user home>/.revapi-cli` (the default; keeps the cache out of the git repo).
 *
 * As with `revapi.sh`, `-d`/`--cache-dir` overrides the cache location outright.
 */
public class revapi {

    private static final String DEFAULT_HOME_DIR_NAME = ".revapi-cli";

    public static void main(String... args) throws Exception {
        String[] mainArgs = new String[args.length + 2];
        mainArgs[0] = "revapi.java";
        mainArgs[1] = baseDir();
        System.arraycopy(args, 0, mainArgs, 2, args.length);

        org.revapi.standalone.Main.main(mainArgs);
    }

    private static String baseDir() {
        String home = System.getProperty("revapi.home");
        if (home == null || home.isBlank()) {
            home = System.getenv("REVAPI_HOME");
        }
        if (home == null || home.isBlank()) {
            home = System.getProperty("user.home") + java.io.File.separator + DEFAULT_HOME_DIR_NAME;
        }
        return home;
    }
}
