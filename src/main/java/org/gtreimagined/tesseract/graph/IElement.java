package org.gtreimagined.tesseract.graph;

import it.unimi.dsi.fastutil.Pair;
import org.gtreimagined.tesseract.graph.standard.StandardNetwork;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a pipe, hatch, multi, or any other machine in a factory pipe system.
 * You should create a new interface that extends this one, then specify your network and grid in the IElement
 * generics.
 */
public interface IElement<TSelf extends IElement<TSelf, TNotableElement, TRoutingInfo, TNetwork, TGrid>, TNotableElement extends INotableElement<TNotableElement, TRoutingInfo, TSelf, TNetwork, TGrid>, TRoutingInfo extends IRoutingInfo<TRoutingInfo>, TNetwork extends INetwork<TNetwork, TSelf, TNotableElement, TRoutingInfo, TGrid>, TGrid extends IGrid<TGrid, TSelf, TNotableElement, TRoutingInfo, TNetwork>> {

    /**
     * Detects all adjacent elements, regardless of what network they're on.
     */
    void getNeighbours(Collection<TSelf> neighbours);

    TNetwork getNetwork();

    void setNetwork(TNetwork network);

    default void onNeighbourAdded(TSelf neighbour) {
        onNeighbourChanged(neighbour);
    }

    default void onNeighbourRemoved(TSelf neighbour) {
        onNeighbourChanged(neighbour);
    }

    default void onNeighbourChanged(TSelf neighbour) {

    }

    /**
     * A component is an object provided by this element.
     * Generally the component implementation is just {@code this}, but it can be anything.
     * In a {@link StandardNetwork}, components are grouped by their interface and can be queried by the same
     * interface.
     * Components are useful if you want to expose something network-wide so that any element can find it.
     *
     * @return A list of component interfaces and their implementations.
     */
    default List<Pair<Class<?>, Object>> getComponents() {
        return Collections.emptyList();
    }
}
