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

public abstract class StandardRouteTracker<TRoutingInfo extends IRoutingInfo<TRoutingInfo>, TElement extends IElement<TElement, TRoutingInfo, TNetwork, TGrid>, TNetwork extends INetwork<TNetwork, TElement, TRoutingInfo, TGrid>, TGrid extends IGrid<TGrid, TElement, TRoutingInfo, TNetwork>> implements IRouteTracker<TRoutingInfo, TElement, TNetwork, TGrid> {
    Map<TElement, List<RoutedNode<TElement, TRoutingInfo>>> edges = new Object2ObjectOpenHashMap<>();

    public final HashSet<TElement> notableElements = new HashSet<>();

    @Override
    public List<RoutedNode<TElement, TRoutingInfo>> getPaths(TElement source) {
        if (edges.containsKey(source)) {
            return edges.get(source);
        }
        return List.of();
    }

    @Override
    public void onElementAdded(TElement source) {
        if (source.isNode()){
            notableElements.add(source);
        }
    }

    @Override
    public void onElementRemoved(TElement element) {
        if (element.isNode()){
            notableElements.remove(element);
        }
    }

    @Override
    public void updateEdges() {
        edges.clear();

        for (TElement notableElement : notableElements) {
            List<RoutedNode<TElement, TRoutingInfo>> paths = makePaths(notableElement);
            if (!paths.isEmpty()) {
                edges.put(notableElement, paths);
            }
        }
        ObjectOpenHashSet<RoutedNode<TElement, TRoutingInfo>> uniqueNodes = new ObjectOpenHashSet<>();
        for (TElement notableElement : notableElements){
            if (edges.containsKey(notableElement)){
                List<RoutedNode<TElement, TRoutingInfo>> list = edges.get(notableElement);
                for (int j = 0; j < list.size(); j++) {
                    RoutedNode<TElement, TRoutingInfo> node = list.get(j);
                    if (!uniqueNodes.add(list.get(j))){
                        list.set(j, uniqueNodes.get(node));
                    }
                }
            }
        }
    }

    public abstract int sort(RoutedNode<TElement, TRoutingInfo> a, RoutedNode<TElement, TRoutingInfo> b);

    public abstract Class<TElement> getNotableElementClass();

    private List<RoutedNode<TElement, TRoutingInfo>> makePaths(TElement source) {
        List<RoutedNode<TElement, TRoutingInfo>> sourcePaths = new ArrayList<>(source.getRoutedNeighbours());
        sourcePaths.sort(this::sort);
        return sourcePaths;
    }
}
