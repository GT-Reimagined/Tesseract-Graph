package org.gtreimagined.tesseract.graph;

import org.gtreimagined.tesseract.graph.standard.StandardNetwork;

import java.util.Collection;

/**
 * A factory network is a logical group of factory elements.
 * You usually want to extend {@link StandardNetwork}, not this.
 */
public interface INetwork<TSelf extends INetwork<TSelf, TElement, TNotableElement, TRoutingInfo, TGrid>, TElement extends IElement<TElement, TNotableElement, TRoutingInfo, TSelf, TGrid>, TNotableElement extends INotableElement<TNotableElement, TRoutingInfo, TElement, TSelf, TGrid>, TRoutingInfo extends IRoutingInfo<TRoutingInfo>, TGrid extends IGrid<TGrid, TElement, TNotableElement, TRoutingInfo, TSelf>> {

    public void addElement(TElement element);

    public void removeElement(TElement element);

    public default void onNetworkRemoved() {

    }

    public Collection<TElement> getElements();

    IRouteTracker<TRoutingInfo, TNotableElement, TElement, TSelf, TGrid> getTracker();

    Class<TElement> getElementClass();

    Class<TNotableElement> getNotableElementClass();
}
