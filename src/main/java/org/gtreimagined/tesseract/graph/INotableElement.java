package org.gtreimagined.tesseract.graph;

import java.util.List;

public interface INotableElement<TSelf extends INotableElement<TSelf, TRoutingInfo, TElement, TNetwork, TGrid> & IElement<TElement, TSelf, TRoutingInfo, TNetwork, TGrid>, TRoutingInfo extends IRoutingInfo<TRoutingInfo>, TElement extends IElement<TElement, TSelf, TRoutingInfo, TNetwork, TGrid>, TNetwork extends INetwork<TNetwork, TElement, TSelf, TRoutingInfo, TGrid>, TGrid extends IGrid<TGrid, TElement, TSelf, TRoutingInfo, TNetwork>> extends IElement<TElement, TSelf, TRoutingInfo, TNetwork, TGrid> {
    /** Exactly the same as getConnections, but with some routing metadata. */
    List<RoutedNode<TSelf, TRoutingInfo>> getRoutedNeighbours();

    boolean isActuallyNode();
}
