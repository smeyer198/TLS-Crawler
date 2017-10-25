/**
 * TLS Crawler
 *
 * Licensed under Apache 2.0
 *
 * Copyright 2017 Ruhr-University Bochum
 */
package de.rub.nds.tlscrawler;

import com.google.devtools.common.options.OptionsParser;
import com.google.devtools.common.options.OptionsParsingException;
import com.mongodb.MongoClient;
import com.mongodb.MongoTimeoutException;
import de.rub.nds.tlscrawler.core.TLSCrawlerMaster;
import de.rub.nds.tlscrawler.core.TLSCrawlerSlave;
import de.rub.nds.tlscrawler.data.IScan;
import de.rub.nds.tlscrawler.orchestration.IOrchestrationProvider;
import de.rub.nds.tlscrawler.orchestration.InMemoryOrchestrationProvider;
import de.rub.nds.tlscrawler.orchestration.RedisOrchestrationProvider;
import de.rub.nds.tlscrawler.persistence.IPersistenceProvider;
import de.rub.nds.tlscrawler.persistence.InMemoryPersistenceProvider;
import de.rub.nds.tlscrawler.persistence.MongoPersistenceProvider;
import de.rub.nds.tlscrawler.scans.NullScan;
import de.rub.nds.tlscrawler.scans.PingScan;
import de.rub.nds.tlscrawler.utility.IpGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * TLS-Crawler's main class.
 *
 * @author janis.fliegenschmidt@rub.de
 */
public class Main {
    private static Logger LOG = LoggerFactory.getLogger(Main.class);

    private static String usageInfo;

    public static void main(String[] args) {
        CLOptions options;

        try {
            options = parseOptions(args);
        } catch (OptionsParsingException ex) {
            LOG.error("Command Line Options could not be parsed.");
            options = null;
        }

        if (options == null || options.help) {
            System.out.println("Could not parse Command Line Options. Try again:");
            System.out.println(usageInfo);
            System.exit(0);
        }

        // TODO: Set up scans. TBD: Scan plug-ins.
        List<IScan> scans = new LinkedList<>();
        scans.add(new PingScan());
        scans.add(new NullScan());

        IOrchestrationProvider orchestrationProvider;
        IPersistenceProvider persistenceProvider;
        if (!options.testMode) {
            MongoClient mongo = new MongoClient(options.mongoDbConnectionString);
            try {
                String address = mongo.getAddress().toString();
                LOG.info("Connected to MongoDB at " + address);
            } catch (MongoTimeoutException ex) {
                LOG.error("Connecting to MongoDB failed.");
                System.exit(0);
            }

            persistenceProvider = new MongoPersistenceProvider(mongo);

            String redisEndpoint = options.redisConnectionString;
            Jedis jedis = new Jedis(redisEndpoint);
            jedis.connect();
            if (jedis.isConnected()) {
                LOG.info("Connected to Redis at " + (redisEndpoint.equals("") ? "localhost" : redisEndpoint));
            } else {
                LOG.error("Connecting to Redis failed.");
                System.exit(0);
            }

            orchestrationProvider = new RedisOrchestrationProvider(jedis);
        } else { // TLS Crawler is in test mode:
            orchestrationProvider = new InMemoryOrchestrationProvider();
            persistenceProvider = new InMemoryPersistenceProvider();
        }

        TLSCrawlerSlave slave = new TLSCrawlerSlave(orchestrationProvider, persistenceProvider, scans);
        TLSCrawlerMaster master = new TLSCrawlerMaster(orchestrationProvider, persistenceProvider, scans);

        LOG.info("TLS-Crawler is running as a " + (options.isMaster ? "master" : "slave") + " node with id "
                + options.instanceId + ".");

        Scanner scanner = new Scanner(System.in);
        for (;;) {
            LOG.info("Starting command reception.");
            String input = scanner.next();

            LOG.debug(String.format("Received input: \"%s\"", input));

            switch (input) {
                case "test_scan":
                    List<String> chosenScans = new LinkedList<>();
                    chosenScans.add("null_scan");

                    List<String> targets = IpGenerator.fullRange();

                    List<Integer> ports = new ArrayList<>();
                    ports.add(32);
                    ports.add(34);
                    ports.add(89);
                    ports.add(254);
                    ports.add(754);
                    ports.add(8987);

                    master.crawl(chosenScans, targets, ports);
                    break;
                case "print":
                    ((InMemoryPersistenceProvider)persistenceProvider).printFirst(10);
                    break;
                default:
                    System.out.println("Did not understand. Try again.");
            }
        }
    }

    static CLOptions parseOptions(String[] args) throws OptionsParsingException {
        CLOptions result;

        OptionsParser parser = OptionsParser.newOptionsParser(CLOptions.class);
        usageInfo = parser.describeOptions(Collections.<String, String>emptyMap(), OptionsParser.HelpVerbosity.LONG);

        parser.parse(args);
        result = parser.getOptions(CLOptions.class);

        if (result != null && result.instanceId.equals("")) {
            result.instanceId = UUID.randomUUID().toString();
        }

        if (result != null && result.masterOnly && !result.isMaster) {
            LOG.warn("Overridden 'isMaster' to true due to 'masterOnly'.");
            result.isMaster = true;
        }

        if (result != null && result.testMode && !result.isMaster) {
            LOG.warn("Overridden 'isMaster' to true due to 'testMode' option.");
            result.isMaster = true;
        }

        if (result != null && result.testMode && !result.inMemoryOrchestration) {
            LOG.warn("Overridden 'inMemoryOrchestration' to true due to 'testMode' option.");
            result.inMemoryOrchestration = true;
        }

        return result;
    }
}
