/**
 * TLS Crawler
 *
 * Licensed under Apache 2.0
 *
 * Copyright 2017 Ruhr-University Bochum
 */
package de.rub.nds.tlscrawler.data;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

/**
 * Scan task implementation.
 *
 * @author janis.fliegenschmidt@rub.de
 */
public class ScanTask implements IScanTask {
    private UUID id;
    private Instant createdTimestamp;
    private Instant acceptedTimestamp;
    private Instant startedTimestamp;
    private Instant completedTimestamp;
    private String target;
    private Collection<Integer> ports;
    private Collection<String> scans;

    public ScanTask(UUID id,
                    Instant createdTimestamp,
                    Instant acceptedTimestamp,
                    Instant startedTimestamp,
                    Instant completedTimestamp,
                    String target,
                    Collection<Integer> ports,
                    Collection<String> scans) {
        this.id = id;
        this.createdTimestamp = createdTimestamp;
        this.acceptedTimestamp = acceptedTimestamp;
        this.startedTimestamp = startedTimestamp;
        this.completedTimestamp = completedTimestamp;
        this.target = target;
        this.ports = ports;
        this.scans = scans;
    }

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public Instant getCreatedTimestamp() {
        return this.createdTimestamp;
    }

    @Override
    public Instant getAcceptedTimestamp() {
        return this.acceptedTimestamp;
    }

    @Override
    public Instant getStartedTimestamp() {
        return this.startedTimestamp;
    }

    @Override
    public Instant getCompletedTimestamp() {
        return this.completedTimestamp;
    }

    @Override
    public String getTargetIp() {
        return this.target;
    }

    @Override
    public Collection<Integer> getPorts() {
        return this.ports;
    }

    @Override
    public Collection<String> getScans() {
        return this.scans;
    }

    @Override
    public IScanTarget getScanTarget() {
        return new ScanTarget(this.target, this.ports);
    }
}