/**
 * TLS Crawler
 *
 * Licensed under Apache 2.0
 *
 * Copyright 2017 Ruhr-University Bochum
 */
package de.rub.nds.tlscrawler.core;

import de.rub.nds.tlscrawler.data.*;
import de.rub.nds.tlscrawler.scans.IScan;
import de.rub.nds.tlscrawler.orchestration.IOrchestrationProvider;
import de.rub.nds.tlscrawler.persistence.IPersistenceProvider;
import de.rub.nds.tlscrawler.utility.IAddressIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * Implements Scan Task Creation.
 *
 * @author janis.fliegenschmidt@rub.de
 */
public class TlsCrawlerMaster extends TlsCrawler {
    private static Logger LOG = LoggerFactory.getLogger(TlsCrawlerMaster.class);
    private static long TIME_TO_WAIT = 1;

    private Map<String, Thread> taskGeneratorThreadList;

    /**
     * TLS-Crawler master constructor.
     *
     * @param instanceId The identifier of this instance.
     * @param orchestrationProvider A non-null orchestration provider.
     * @param persistenceProvider A non-null persistence provider.
     * @param scans A neither null nor empty list of available scans.
     */
    public TlsCrawlerMaster(String instanceId,
                            IOrchestrationProvider orchestrationProvider,
                            IPersistenceProvider persistenceProvider,
                            List<IScan> scans) {
        super(instanceId, orchestrationProvider, persistenceProvider, scans);

        this.taskGeneratorThreadList = new HashMap<>();
    }

    public void crawl(List<String> scans, IAddressIterator targets, List<Integer> ports, String scanId) {
        LOG.info("Crawling started");
        if (areNotValidArgs(scans, targets, ports)) {
            LOG.error("Crawling task has not been established due to invalid arguments.");
        }

        // TODO: This should be parallelized.

        int ctr = 0;
        Collection<IScanTask> bulk = new ArrayList<>(1000);

        while (targets.iterator().hasNext()) {
            String target = targets.iterator().next();
            String taskId = UUID.randomUUID().toString();

            bulk.add(new ScanTask(
                    taskId,
                    scanId,
                    this.getInstanceId(),
                    Instant.now(),
                    null,
                    null,
                    null,
                    target,
                    ports,
                    scans)
            );

            if (++ctr >= 1000 || targets.iterator().hasNext() == false) {
                setUp(this, bulk);
                ctr = 0;
                bulk = new ArrayList<>(1000);
            }
        }

        try {
            while (this.getOrchestrationProvider().getNumberOfTasks() != 0) {
                TimeUnit.SECONDS.sleep(TIME_TO_WAIT);
            }
        } catch (Exception e) {
            LOG.debug("Oops! Exception");
            e.printStackTrace();
        } finally {
            targets.remove();
        }
        LOG.info("All ScanTasks have been scheduled");
    }
    
    private static void setUp(IOrganizer org, Collection<IScanTask> tasks) {
            org.getPersistenceProvider().setUpScanTasks(tasks);

            Collection<String> tids = tasks.stream()
                    .map(x -> x.getId())
                    .collect(Collectors.toList());

            org.getOrchestrationProvider().addScanTasks(tids);
    }

    public IMasterStats getStats() {
        LOG.trace("getStats()");

        IPersistenceProviderStats ppStats = this.getPersistenceProvider().getStats();

        return new MasterStats(ppStats.getTotalTasks(),
                ppStats.getFinishedTasks(),
                ppStats.getEarliestCompletionTimestamp(),
                ppStats.getEarliestCreatedTimestamp());
    }

    private boolean areNotValidArgs(List<String> scans, IAddressIterator targets, List<Integer> ports) {
        LOG.trace("areNotValidArgs()");

        List<String> invalidScans = scans.stream().filter(x -> !this.getScanNames().contains(x)).collect(Collectors.toList());
        List<Integer> invalidPorts = ports.stream().filter(x -> x < 1 || x > 65535).collect(Collectors.toList());

        boolean allScansValid = invalidScans.isEmpty();
        boolean allPortsValid = invalidPorts.isEmpty();

        if (!allScansValid) {
            String invalidScanList = invalidScans.stream().map(item -> "'" + item + "'").collect(joining(" "));
            LOG.error(String.format("Invalid Scans: %s", invalidScanList));
        }

        if (!allPortsValid) {
            String invalidPortsList = invalidPorts.stream().map(item -> "'" + item + "'").collect(joining(" "));
            LOG.error(String.format("Invalid Ports: %s", invalidPortsList));
        }

        return !(allScansValid && allPortsValid);
    }

    private static final String IP_ADDRESS_STRING =
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))";
    public static final Pattern IP_ADDRESS = Pattern.compile(IP_ADDRESS_STRING);

    public static boolean isValidIp(String ip) {
        return IP_ADDRESS.matcher(ip).matches();
    }
}