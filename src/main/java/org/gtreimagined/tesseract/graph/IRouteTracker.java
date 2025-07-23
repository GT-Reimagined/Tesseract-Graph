package org.gtreimagined.tesseract.graph;

import java.util.List;

public interface IRouteTracker<TRoutingInfo extends IRoutingInfo<TRoutingInfo>, TNotableElement extends INotableElement<TNotableElement, TRoutingInfo, TElement, TNetwork, TGrid>, TElement extends IElement<TElement, TNotableElement, TRoutingInfo, TNetwork, TGrid>, TNetwork extends INetwork<TNetwork, TElement, TNotableElement, TRoutingInfo, TGrid>, TGrid extends IGrid<TGrid, TElement, TNotableElement, TRoutingInfo, TNetwork>> {
    List<RoutedNode<TNotableElement, TRoutingInfo>> getPaths(TNotableElement source);

    void onElementAdded(TElement element);

    void onElementRemoved(TElement element);

    void updateEdges();
}
