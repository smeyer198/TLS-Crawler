/**
 * TLS Crawler
 *
 * Licensed under Apache 2.0
 *
 * Copyright 2017 Ruhr-University Bochum
 */
package de.rub.nds.tlscrawler.options;

import com.google.devtools.common.options.*;

import java.util.Collections;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Command Line Options at startup.
 *
 * @author janis.fliegenschmidt@rub.de
 */
public class StartupOptions extends OptionsBase {

    private static Logger LOG = LogManager.getLogger();

    private static OptionsParser parser = OptionsParser.newOptionsParser(StartupOptions.class);

    @Option(
            name = "help",
            abbrev = 'h',
            help = "Prints usage info.",
            defaultValue = "false"
    )
    public boolean help;

    @Option(
            name = "instanceId",
            abbrev = 'i',
            help = "The ID of this TLS-Crawler instance.",
            defaultValue = ""
    )
    public String instanceId;

    @Option(
            name = "mongoDbHost",
            abbrev = 'o',
            help = "Host of the MongoDB instance this crawler saves to.",
            defaultValue = "localhost"
    )
    public String mongoDbHost;

    @Option(
            name = "mongoDbPort",
            abbrev = 'p',
            help = "Port of the MongoDB instance this crawler saves to.",
            defaultValue = "27017"
    )
    public int mongoDbPort;

    @Option(
            name = "mongoDbUser",
            help = "The username to be used to authenticate with the MongoDB instance.",
            defaultValue = ""
    )
    public String mongoDbUser;

    @Option(
            name = "mongoDbPass",
            help = "The passwort to be used to authenticate with MongoDB.",
            defaultValue = ""
    )
    public String mongoDbPass;

    @Option(
            name = "mongoDbAuthSource",
            help = "The DB within the MongoDB instance, in which the user:pass is defined.",
            defaultValue = ""
    )
    public String mongoDbAuthSource;

    @Option(
            name = "redisHost",
            abbrev = 'r',
            help = "Host of the Redis instance this crawler uses to coordinate.",
            defaultValue = "localhost"
    )
    public String redisHost;

    @Option(
            name = "redisPort",
            abbrev = 'q',
            help = "Port of the Redis instance this crawler uses to coordinate.",
            defaultValue = "6379"
    )
    public int redisPort;

    @Option(
            name = "redisPass",
            abbrev = 'a',
            help = "Password of the Redis instance this crawler uses to coordinate.",
            defaultValue = ""
    )
    public String redisPass;

    @Option(
            name = "workspace",
            abbrev = 'w',
            help = "Instances in the same workspace share the same databases.",
            defaultValue = ""
    )
    public String workspace;

    @Option(
            name = "numberOfThreads",
            help = "Number of worker threads the crawler slave should use.",
            defaultValue = "1000"
    )
    public int numberOfThreads;

    @Option(
            name = "parallelProbeThreads",
            abbrev = 'T',
            help = "Number of worker threads the crawler slave should use.",
            defaultValue = "1000"
    )
    public int parallelProbeThreads;
    
    @Option(
            name = "timeout",
            abbrev = 't',
            help = "The timeout to use inside the TLS-Scanner.",
            defaultValue = "2000"
    )
    public int scannerTimeout;
    
    @Option(
            name = "reexecutions",
            help = "Number of threads to use inside the TLS-Scanner.",
            defaultValue = "1000"
    )
    public int reexecutions;
    
    @Option(
            name = "scansToBeExecuted",
            abbrev = 'S',
            help = "The names of the scans that should be exeucted as a comma seperated list.",
            defaultValue = ""
    )

    public String scansToBeExecuted;

    @Option(
            name = "portsToBeScanned",
            abbrev = 'P',
            help = "The port that should be scanned.",
            defaultValue = "443"
    )
    public int port;

    @Option(
            name = "blacklist",
            abbrev = 'b',
            help = "The redis key in which the blacklist of IPs/CIDR-Blocks is placed.",
            defaultValue = "TLSC-blacklist"
    )
    public String blacklist;

    private static String DEFAULT_WORKSPACE = "default";

    /**
     * Implements command line argument parsing.
     *
     * @param args The argument array.
     * @return An object containing sane arguments.
     * @throws OptionsParsingException
     */
    public static StartupOptions parseOptions(String[] args) throws OptionsParsingException {
        StartupOptions result = null;

        LOG.trace("parseOptions()");
        result = Options.parse(StartupOptions.class, args).getOptions();

        if (result != null && result.workspace.equals("")) {
            LOG.warn("No workspace name set. Using default workspace: {}", DEFAULT_WORKSPACE);
            result.workspace = DEFAULT_WORKSPACE;
        }

        if (result != null && result.instanceId.equals("")) {
            result.instanceId = UUID.randomUUID().toString();
            LOG.info("InstanceID: {}", result.instanceId);
        }

        boolean saneMongoLogin = saneMongoLogin(result.mongoDbUser, result.mongoDbPass, result.mongoDbAuthSource);
        if (result != null && !saneMongoLogin) {
            LOG.warn("Did not specify a full set of mongo credentials (none is fine for unsecured instances).");
        }

        return result;
    }

    public static String getHelpString() {
        return parser.describeOptions(Collections.<String, String>emptyMap(), OptionsParser.HelpVerbosity.LONG);
    }

    private static boolean saneMongoLogin(String user, String pass, String authSource) {
        if (user.equals("") && pass.equals("") && authSource.equals("")) {
            return true;
        } else if (!user.equals("") && !pass.equals("") && !authSource.equals("")) {
            return true;
        } else {
            return false;
        }
    }
}
