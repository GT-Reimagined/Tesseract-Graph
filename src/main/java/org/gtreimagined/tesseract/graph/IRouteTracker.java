package org.gtreimagined.tesseract.graph;

import java.util.List;

public interface IRouteTracker<TRoutingInfo extends IRoutingInfo<TRoutingInfo>, TElement extends IElement<TElement, TRoutingInfo, TNetwork, TGrid>, TNetwork extends INetwork<TNetwork, TElement, TRoutingInfo, TGrid>, TGrid extends IGrid<TGrid, TElement, TRoutingInfo, TNetwork>> {
    List<RoutedNode<TElement, TRoutingInfo>> getPaths(TElement source);

    void onElementAdded(TElement element);

    void onElementRemoved(TElement element);

    void updateEdges();
}
