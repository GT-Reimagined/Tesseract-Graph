package org.gtreimagined.tesseract.graph;

import org.gtreimagined.tesseract.graph.standard.StandardGrid;

/**
 * A factory grid is the global coordinator for your factory pipe system.
 * Grids will create and destroy networks as elements join or leave the world.
 * You usually want to extend {@link StandardGrid}, not this.
 */
public interface IGrid<TSelf extends IGrid<TSelf, TElement, TNotableElement, TRoutingInfo, TNetwork>, TElement extends IElement<TElement, TNotableElement, TRoutingInfo, TNetwork, TSelf>, TNotableElement extends INotableElement<TNotableElement, TRoutingInfo, TElement, TNetwork, TSelf>, TRoutingInfo extends IRoutingInfo<TRoutingInfo>, TNetwork extends INetwork<TNetwork, TElement, TNotableElement, TRoutingInfo, TSelf>> {

    /**
     * Adds an element and does potentially expensive network topology updates.
     * Also acts like a hard reset in case the given element has changed its neighbours.
     */
    public void addElement(TElement element);

    /**
     * Adds an element but does not do any network topology updates.
     * Use with caution.
     */
    public void addElementQuietly(TNetwork network, TElement element);

    /**
     * Removes an element and does potentially expensive network topology updates.
     */
    public void removeElement(TElement element);

    /**
     * Removes an element but does not do any network topology updates.
     * Use with caution.
     */
    public void removeElementQuietly(TElement element);

    /**
     * Subsumes a network into another one.
     * You generally shouldn't call this unless you have a good reason since network subsuming/splitting is handled
     * automatically.
     */
    public void subsume(TNetwork dest, TNetwork source);
}
