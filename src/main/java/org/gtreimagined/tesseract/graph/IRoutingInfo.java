package org.gtreimagined.tesseract.graph;

public interface IRoutingInfo<TSelf extends IRoutingInfo<TSelf>> {
    /** Merges the distances/etc of two routing infos and returns a copy. */
    TSelf merge(TSelf other);
}
