package org.gtreimagined.tesseract.graph.standard;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.gtreimagined.tesseract.graph.IElement;
import org.gtreimagined.tesseract.graph.IGrid;
import org.gtreimagined.tesseract.graph.INetwork;
import org.gtreimagined.tesseract.graph.INotableElement;
import org.gtreimagined.tesseract.graph.IRouteTracker;
import org.gtreimagined.tesseract.graph.IRoutingInfo;
import org.gtreimagined.tesseract.graph.RoutedNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public abstract class StandardRouteTracker<TRoutingInfo extends IRoutingInfo<TRoutingInfo>, TNotableElement extends INotableElement<TNotableElement, TRoutingInfo, TElement, TNetwork, TGrid>, TElement extends IElement<TElement, TNotableElement, TRoutingInfo, TNetwork, TGrid>, TNetwork extends INetwork<TNetwork, TElement, TNotableElement, TRoutingInfo, TGrid>, TGrid extends IGrid<TGrid, TElement, TNotableElement, TRoutingInfo, TNetwork>> implements IRouteTracker<TRoutingInfo, TNotableElement, TElement, TNetwork, TGrid> {
    Map<TNotableElement, List<RoutedNode<TNotableElement, TRoutingInfo>>> edges = new Object2ObjectOpenHashMap<>();

    public final HashSet<TNotableElement> notableElements = new HashSet<>();

    @Override
    public List<RoutedNode<TNotableElement, TRoutingInfo>> getPaths(TNotableElement source) {
        if (edges.containsKey(source)) {
            return edges.get(source);
        }
        return List.of();
    }

    @Override
    public void onElementAdded(TElement source) {
        TNotableElement notableElement;
        if (getNotableElementClass().isInstance(source) && (notableElement = getNotableElementClass().cast(source)).isActuallyNode()){
            notableElements.add(notableElement);
        }
    }

    @Override
    public void onElementRemoved(TElement element) {
        TNotableElement notableElement;
        if (getNotableElementClass().isInstance(element) && (notableElement = getNotableElementClass().cast(element)).isActuallyNode()){
            notableElements.remove(notableElement);
        }
    }

    @Override
    public void updateEdges() {
        edges.clear();

        for (TNotableElement notableElement : notableElements) {
            List<RoutedNode<TNotableElement, TRoutingInfo>> paths = makePaths(notableElement);
            if (!paths.isEmpty()) {
                edges.put(notableElement, paths);
            }
        }
        ObjectOpenHashSet<RoutedNode<TNotableElement, TRoutingInfo>> uniqueNodes = new ObjectOpenHashSet<>();
        for (TNotableElement notableElement : notableElements){
            if (edges.containsKey(notableElement)){
                List<RoutedNode<TNotableElement, TRoutingInfo>> list = edges.get(notableElement);
                for (int j = 0; j < list.size(); j++) {
                    RoutedNode<TNotableElement, TRoutingInfo> node = list.get(j);
                    if (!uniqueNodes.add(list.get(j))){
                        list.set(j, uniqueNodes.get(node));
                    }
                }
            }
        }
    }

    public abstract int sort(RoutedNode<TNotableElement, TRoutingInfo> a, RoutedNode<TNotableElement, TRoutingInfo> b);

    public abstract Class<TNotableElement> getNotableElementClass();

    private List<RoutedNode<TNotableElement, TRoutingInfo>> makePaths(TNotableElement source) {
        List<RoutedNode<TNotableElement, TRoutingInfo>> sourcePaths = new ArrayList<>(source.getRoutedNeighbours());
        sourcePaths.sort(this::sort);
        return sourcePaths;
    }
}
