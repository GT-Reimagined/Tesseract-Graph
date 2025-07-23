package org.gtreimagined.tesseract.graph.standard;

import org.gtreimagined.tesseract.graph.IElement;
import org.gtreimagined.tesseract.graph.IGrid;
import org.gtreimagined.tesseract.graph.INetwork;
import org.gtreimagined.tesseract.graph.INotableElement;
import org.gtreimagined.tesseract.graph.IRouteTracker;
import org.gtreimagined.tesseract.graph.IRoutingInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A pretty basic factory network.
 * This doesn't do much beyond tracking elements and components.
 */
public abstract class StandardNetwork<TSelf extends StandardNetwork<TSelf, TElement, TNotableElement, TRoutingInfo, TGrid>, TElement extends IElement<TElement, TNotableElement, TRoutingInfo, TSelf, TGrid>, TNotableElement extends INotableElement<TNotableElement, TRoutingInfo, TElement, TSelf, TGrid>, TRoutingInfo extends IRoutingInfo<TRoutingInfo>, TGrid extends IGrid<TGrid, TElement, TNotableElement, TRoutingInfo, TSelf>>
        implements INetwork<TSelf, TElement, TNotableElement, TRoutingInfo, TGrid> {

    public final HashSet<TElement> elements = new HashSet<>();
    public final HashMap<Class<?>, Collection<Object>> components = new HashMap<>();
    private final Class<TElement> elementClass;
    private final Class<TNotableElement> notableElementClass;

    public IRouteTracker<TRoutingInfo, TNotableElement, TElement, TSelf, TGrid> routeTracker;

    private boolean networkChanged = false;

    protected StandardNetwork(Class<TElement> elementClass, Class<TNotableElement> notableElementClass) {
        this.elementClass = elementClass;
        this.notableElementClass = notableElementClass;
        this.routeTracker = createRouteTracker();
    }

    protected abstract IRouteTracker<TRoutingInfo, TNotableElement, TElement, TSelf, TGrid> createRouteTracker();

    @Override
    public Class<TElement> getElementClass() {
        return elementClass;
    }

    @Override
    public Class<TNotableElement> getNotableElementClass() {
        return notableElementClass;
    }

    @Override
    public void addElement(TElement element) {
        elements.add(element);

        routeTracker.onElementAdded(element);
        networkChanged = true;
        for (var component : element.getComponents()) {
            addComponentImpl(component.left(), component.right());
        }
    }

    @Override
    public void removeElement(TElement element) {
        elements.remove(element);
        routeTracker.onElementRemoved(element);
        networkChanged = true;
        if (element != null && element.getNetwork() == this) {
            for (var component : element.getComponents()) {
                removeComponentImpl(component.left(), component.right());
            }
        }
    }

    private void addComponentImpl(Class<?> iface, Object impl) {
        components.computeIfAbsent(iface, x -> new HashSet<>())
                .add(impl);
    }

    public <TIface, TImpl extends TIface> void addComponent(Class<TIface> iface, TImpl impl) {
        addComponentImpl(iface, impl);
    }

    public void tick(){
        if (networkChanged) {
            networkChanged = false;
            routeTracker.updateEdges();
        }
    }

    private void removeComponentImpl(Class<?> iface, Object impl) {
        Collection<Object> s = components.get(iface);

        if (s != null) {
            s.remove(impl);

            if (s.isEmpty()) {
                components.remove(iface);
            }
        }
    }

    public <TIface, TImpl extends TIface> void removeComponent(Class<TIface> iface, TImpl impl) {
        removeComponentImpl(iface, impl);
    }

    @SuppressWarnings("unchecked")
    public <TIface> Collection<TIface> getComponents(Class<TIface> iface) {
        return (Collection<TIface>) components.getOrDefault(iface, Collections.emptyList());
    }

    @Override
    public Collection<TElement> getElements() {
        return elements;
    }

    @Override
    public IRouteTracker<TRoutingInfo, TNotableElement, TElement, TSelf, TGrid> getTracker() {
        return routeTracker;
    }
}
