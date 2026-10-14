package org.gtreimagined.tesseract.graph;

import org.gtreimagined.tesseract.graph.standard.StandardNetwork;

import java.util.Collection;

/**
 * A factory network is a logical group of factory elements.
 * You usually want to extend {@link StandardNetwork}, not this.
 */
public interface INetwork<TSelf extends INetwork<TSelf, TElement, TRoutingInfo, TGrid>, TElement extends IElement<TElement, TRoutingInfo, TSelf, TGrid>, TRoutingInfo extends IRoutingInfo<TRoutingInfo>, TGrid extends IGrid<TGrid, TElement, TRoutingInfo, TSelf>> {

    public void addElement(TElement element);

    public void removeElement(TElement element);

    public default void onNetworkRemoved() {

    }

    public Collection<TElement> getElements();

    IRouteTracker<TRoutingInfo, TElement, TSelf, TGrid> getTracker();

    Class<TElement> getElementClass();
}
