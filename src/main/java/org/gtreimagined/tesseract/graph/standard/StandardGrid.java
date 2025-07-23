package org.gtreimagined.tesseract.graph.standard;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gtreimagined.tesseract.graph.GraphUtils;
import org.gtreimagined.tesseract.graph.IElement;
import org.gtreimagined.tesseract.graph.IGrid;
import org.gtreimagined.tesseract.graph.INotableElement;
import org.gtreimagined.tesseract.graph.IRoutingInfo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This handles all network topology updates, and should be compatible with most pipe systems.
 * A factory element should always register itself with this grid, even when it's not connected to anything.
 */
public abstract class StandardGrid<TSelf extends StandardGrid<TSelf, TElement, TNotableElement, TRoutingInfo, TNetwork>, TElement extends IElement<TElement, TNotableElement, TRoutingInfo, TNetwork, TSelf>, TNotableElement extends INotableElement<TNotableElement, TRoutingInfo, TElement, TNetwork, TSelf>, TRoutingInfo extends IRoutingInfo<TRoutingInfo>, TNetwork extends StandardNetwork<TNetwork, TElement, TNotableElement, TRoutingInfo, TSelf>>
        implements IGrid<TSelf, TElement, TNotableElement, TRoutingInfo, TNetwork> {

    public static final Logger LOGGER = LogManager.getLogger("Standard Factory Network");

    public final HashSet<TNetwork> networks = new HashSet<>();
    public final HashSet<TElement> vertices = new HashSet<>();
    public final SetMultimap<TElement, TElement> edges = MultimapBuilder.hashKeys()
            .hashSetValues()
            .build();

    protected StandardGrid() {

    }

    @Override
    public void addElement(TElement element) {
        removeElement(element);

        vertices.add(element);
        updateNeighbours(element);

        HashSet<TElement> discovered = new HashSet<>();
        HashSet<TNetwork> networks = new HashSet<>();

        long pre = System.nanoTime();

        walkAdjacency(element, discovered, networks, false);

        long post = System.nanoTime();

        if (GraphUtils.INSTANCE.isDevEnvironment()) {
            LOGGER.info("Walked adjacent elements in " + (post - pre) / 1e3 + " us");
        }

        if (networks.isEmpty()) {
            // there are no neighbours, or the neighbours didn't have a network somehow (which is an illegal state!
            // boo!)
            TNetwork network = createNetwork();
            this.networks.add(network);

            for (TElement e : discovered) {
                if (e.getNetwork() != network) {
                    e.setNetwork(network);
                    network.addElement(e);
                }
            }
        } else if (networks.size() == 1) {
            // there was one network adjacent, so we can just add all discovered elements to it if they aren't already
            TNetwork network = networks.iterator()
                    .next();

            for (TElement e : discovered) {
                if (e.getNetwork() != network) {
                    e.setNetwork(network);
                    network.addElement(e);
                }
            }
        } else {
            // there were several adjacent networks; subsume all smaller networks into the biggest one
            Iterator<TNetwork> iter = networks.iterator();

            TNetwork biggestNetwork = iter.next();

            while (iter.hasNext()) {
                TNetwork network = iter.next();

                if (network.getElements()
                        .size()
                        > biggestNetwork.getElements()
                        .size())
                    biggestNetwork = network;
            }

            pre = System.nanoTime();

            for (TNetwork network : networks) {
                if (network != biggestNetwork) {
                    subsume(biggestNetwork, network);
                }
            }

            post = System.nanoTime();
            if (GraphUtils.INSTANCE.isDevEnvironment()) {
                LOGGER.info("Subsumed " + (networks.size() - 1) + " networks in " + (post - pre) / 1e3 + " us");
            }

            for (TElement e : discovered) {
                if (e.getNetwork() != biggestNetwork) {
                    e.setNetwork(biggestNetwork);
                    biggestNetwork.addElement(e);
                }
            }
        }
    }

    @Override
    public void addElementQuietly(TNetwork network, TElement element) {
        vertices.add(element);
        element.setNetwork(network);
        network.addElement(element);
    }

    public void tick(){
        for (TNetwork network : networks ) {
            network.tick();
        }
    }

    protected abstract TNetwork createNetwork();

    @Override
    public void removeElement(TElement element) {
        if (!vertices.contains(element)) return;

        vertices.remove(element);
        Set<TElement> neighbours = edges.removeAll(element);

        TNetwork network = element.getNetwork();

        network.removeElement(element);
        element.setNetwork(null);

        // the network doesn't have any elements left, there aren't any adjacent neighbours to fix
        if (network.getElements()
                .isEmpty()) {
            network.onNetworkRemoved();
            networks.remove(network);
            return;
        }

        for (TElement neighbour : neighbours) {
            updateNeighbours(neighbour);
        }

        // if there's only one neighbour, then this element is at the end of a chain and we can return early since we
        // definitely didn't split a network
        if (neighbours.size() <= 1) return;

        HashSet<HashSet<TElement>> neighbouringClumps = new HashSet<>();

        // the list of all discovered elements; if one is in here, it means we've visited it already and can skip
        // iterating its neighbours
        HashSet<TElement> discovered = new HashSet<>();

        long pre = System.nanoTime();

        for (TElement neighbour : neighbours) {
            if (discovered.contains(neighbour)) continue;

            // find all elements connected to this neighbour
            HashSet<TElement> clump = new HashSet<>();
            walkAdjacency(neighbour, clump, null, true);

            neighbouringClumps.add(clump);
            discovered.addAll(clump);
        }

        // if there's only one clump of neighbours then the network hasn't been split
        if (neighbouringClumps.size() <= 1) {
            return;
        }

        HashSet<TElement> biggestClump = null;

        // find the biggest clump of neighbours; we'll split the other clumps from it
        for (HashSet<TElement> nn : neighbouringClumps) {
            if (biggestClump == null || nn.size() > biggestClump.size()) biggestClump = nn;
        }

        for (HashSet<TElement> nn : neighbouringClumps) {
            if (nn != biggestClump) {
                for (TElement e : nn) {
                    network.removeElement(e);
                }

                TNetwork newNetwork = createNetwork();

                for (TElement e : nn) {
                    e.setNetwork(newNetwork);
                    newNetwork.addElement(e);
                }
                this.networks.add(newNetwork);
            }
        }

        long post = System.nanoTime();

        if (GraphUtils.INSTANCE.isDevEnvironment()) {
            LOGGER.info(
                    "Split network in " + (post - pre) / 1e3
                            + " us (added "
                            + (neighbouringClumps.size() - 1)
                            + " new networks)");
        }
    }

    @Override
    public void removeElementQuietly(TElement element) {
        if (!vertices.contains(element)) return;

        element.getNetwork()
                .removeElement(element);
        vertices.remove(element);
        element.setNetwork(null);

        for (TElement neighbour : edges.removeAll(element)) {
            updateNeighbours(neighbour);
        }
    }

    @Override
    public void subsume(TNetwork dest, TNetwork source) {
        for (TElement element : new ArrayList<>(source.getElements())) {
            source.removeElement(element);
            element.setNetwork(dest);
            dest.addElement(element);
        }

        source.onNetworkRemoved();
        this.networks.remove(source);
    }

    private void walkAdjacency(TElement start, HashSet<TElement> discovered, HashSet<TNetwork> networks,
                               boolean recurseIntoNetworked) {
        ArrayDeque<TElement> queue = new ArrayDeque<>();

        queue.add(start);

        while (!queue.isEmpty()) {
            TElement current = queue.removeFirst();

            if(!discovered.add(current)) continue;

            if (networks != null) networks.add(current.getNetwork());

            if (current == start || (recurseIntoNetworked || current.getNetwork() == null)) {
                queue.addAll(edges.get(current));
            }
        }

        if (networks != null) networks.remove(null);
    }

    public void updateNeighbours(TElement element) {
        updateNeighbours(element, new HashSet<>());
    }

    private void updateNeighbours(TElement element, HashSet<TElement> updated) {
        if (!updated.add(element)) return;

        HashSet<TElement> neighbours = new HashSet<>();

        element.getNeighbours(neighbours);

        Set<TElement> oldNeighbours = edges.removeAll(element);
        edges.putAll(element, neighbours);

        for (TElement oldNeighbour : oldNeighbours) {
            if (!neighbours.contains(oldNeighbour)) {
                updateNeighbours(oldNeighbour, updated);

                if (edges.containsEntry(oldNeighbour, element)) {
                    LOGGER.error(
                            "A factory element isn't following the graph adjacency contract. Edge B -> A was kept when edge A -> B was removed. A = "
                                    + element
                                    + ", B = "
                                    + oldNeighbour);
                }

                oldNeighbour.onNeighbourRemoved(element);
                element.onNeighbourRemoved(oldNeighbour);
            }
        }

        for (TElement currentNeighbour : neighbours) {
            if (!oldNeighbours.contains(currentNeighbour)) {
                updateNeighbours(currentNeighbour, updated);

                if (!edges.containsEntry(currentNeighbour, element)) {
                    LOGGER.error(
                            "A factory element isn't following the graph adjacency contract. Edge B -> A was not added when edge A -> B was added. A = "
                                    + element
                                    + ", B = "
                                    + currentNeighbour);
                }

                currentNeighbour.onNeighbourAdded(element);
                element.onNeighbourAdded(currentNeighbour);
            }
        }
    }
}
