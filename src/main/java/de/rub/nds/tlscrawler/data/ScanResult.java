/**
 * TLS Crawler
 *
 * Licensed under Apache 2.0
 *
 * Copyright 2017 Ruhr-University Bochum
 */
package de.rub.nds.tlscrawler.data;

import de.rub.nds.tlscrawler.utility.Tuple;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of the IScanResult interface.
 *
 * @author janis.fliegenschmidt@rub.de
 */
public class ScanResult implements IScanResult {
    private static String ID_KEY = "_result_id";

    Collection<Tuple> elements;

    public ScanResult() {
        elements = new LinkedList<>();
    }

    public ScanResult(String identifier) {
        elements = new LinkedList<>();

        this.setResultIdentifier(identifier);
    }

    @Override
    public void setResultIdentifier(String identifier) {
        this.checkedAdd(Tuple.create(ScanResult.ID_KEY, identifier));
    }

    @Override
    public void addString(String key, String value) {
        this.checkedAdd(Tuple.create(key, value));
    }

    @Override
    public void addStringArray(String key, List<String> value) {
        this.checkedAdd(Tuple.create(key, value));
    }

    @Override
    public void addLong(String key, long value) {
        this.checkedAdd(Tuple.create(key, value));
    }

    @Override
    public void addLongArray(String key, List<Long> value) {
        this.checkedAdd(Tuple.create(key, value));
    }

    @Override
    public void addDouble(String key, double value) {
        this.checkedAdd(Tuple.create(key, value));
    }

    @Override
    public void addDoubleArray(String key, List<Double> value) {
        this.checkedAdd(Tuple.create(key, value));
    }

    @Override
    public void addTimestamp(String key, Instant value) {
        this.checkedAdd(Tuple.create(key, value));
    }

    @Override
    public void addBinaryData(String key, List<Byte> data) {
        this.checkedAdd(Tuple.create(key, data));
    }

    @Override
    public void addSubResult(String key, IScanResult substructure) {
        this.checkedAdd(Tuple.create(key, substructure));
    }

    private void checkedAdd(Tuple newData) {
        assert newData.getFirst() instanceof String;

        String newDataKey = (String)newData.getFirst();

        if (newDataKey == null || newDataKey.length() == 0) {
            throw new IllegalArgumentException("Key must neither be null nor empty.");
        }

        for (Tuple data : this.elements) {
            if (data.getFirst().equals(newData.getFirst())) {
                throw new IllegalArgumentException("Added data with duplicate key.");
            }
        }

        this.elements.add(newData);
    }
}
