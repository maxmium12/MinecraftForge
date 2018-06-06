/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.common.toposort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Topological sort for mod loading
 *
 * Based on a variety of sources, including http://keithschwarz.com/interesting/code/?dir=topological-sort
 * @author cpw
 *
 */
public class TopologicalSort
{
    public static class DirectedGraph<T> implements Iterable<T>
    {
        private final Map<T, SortedSet<T>> graph = new HashMap<>();
        private List<T> orderedNodes = new ArrayList<>();

        public boolean addNode(T node)
        {
            // Ignore nodes already added
            if (graph.containsKey(node))
            {
                return false;
            }

            orderedNodes.add(node);
            graph.put(node, new TreeSet<>(Comparator.comparingInt(o -> orderedNodes.indexOf(o))));
            return true;
        }

        public void addEdge(T from, T to)
        {
            if (!(graph.containsKey(from) && graph.containsKey(to)))
            {
                throw new NoSuchElementException("Missing nodes from graph");
            }

            graph.get(from).add(to);
        }

        public void removeEdge(T from, T to)
        {
            if (!(graph.containsKey(from) && graph.containsKey(to)))
            {
                throw new NoSuchElementException("Missing nodes from graph");
            }

            graph.get(from).remove(to);
        }

        public boolean edgeExists(T from, T to)
        {
            if (!(graph.containsKey(from) && graph.containsKey(to)))
            {
                throw new NoSuchElementException("Missing nodes from graph");
            }

            return graph.get(from).contains(to);
        }

        public Set<T> edgesFrom(T from)
        {
            if (!graph.containsKey(from))
            {
                throw new NoSuchElementException("Missing node from graph");
            }

            return Collections.unmodifiableSortedSet(graph.get(from));
        }
        @Override
        public Iterator<T> iterator()
        {
            return orderedNodes.iterator();
        }

        public int size()
        {
            return graph.size();
        }

        public boolean isEmpty()
        {
            return graph.isEmpty();
        }

        @Override
        public String toString()
        {
            return graph.toString();
        }
    }

    /**
     * Sort the input graph into a topologically sorted list
     *
     * Uses the reverse depth first search as outlined in ...
     * @param graph
     * @return The sorted mods list.
     */
    public static <T> List<T> topologicalSort(DirectedGraph<T> graph)
    {
        DirectedGraph<T> rGraph = reverse(graph);
        List<T> sortedResult = new ArrayList<>();
        Set<T> visitedNodes = new HashSet<>();
        // A list of "fully explored" nodes. Leftovers in here indicate cycles in the graph
        Set<T> expandedNodes = new HashSet<>();

        for (T node : rGraph)
        {
            explore(node, rGraph, sortedResult, visitedNodes, expandedNodes);
        }

        return sortedResult;
    }

    public static <T> DirectedGraph<T> reverse(DirectedGraph<T> graph)
    {
        DirectedGraph<T> result = new DirectedGraph<>();

        for (T node : graph)
        {
            result.addNode(node);
        }

        for (T from : graph)
        {
            for (T to : graph.edgesFrom(from))
            {
                result.addEdge(to, from);
            }
        }

        return result;
    }

    private static <T> void explore(T node, DirectedGraph<T> graph, List<T> sortedResult, Set<T> visitedNodes, Set<T> expandedNodes)
    {
        // Have we been here before?
        if (visitedNodes.contains(node))
        {
            // And have completed this node before
            if (expandedNodes.contains(node))
            {
                // Then we're fine
                return;
            }

            throw new TopoSortException(node, sortedResult, visitedNodes, expandedNodes);
        }

        // Visit this node
        visitedNodes.add(node);

        // Recursively explore inbound edges
        for (T inbound : graph.edgesFrom(node))
        {
            explore(inbound, graph, sortedResult, visitedNodes, expandedNodes);
        }

        // Add ourselves now
        sortedResult.add(node);
        // And mark ourselves as explored
        expandedNodes.add(node);
    }

    static class TopoSortException extends RuntimeException {
        private final Object node;
        private final List<?> sortedResult;
        private final Set<?> visitedNodes;
        private final Set<?> expandedNodes;

        public TopoSortException(Object node, List<?> sortedResult, Set<?> visitedNodes, Set<?> expandedNodes)
        {
            this.node = node;
            this.sortedResult = sortedResult;
            this.visitedNodes = visitedNodes;
            this.expandedNodes = expandedNodes;
        }

        public Object getNode()
        {
            return node;
        }

        public List<?> getSortedResult()
        {
            return sortedResult;
        }

        public Set<?> getVisitedNodes()
        {
            return visitedNodes;
        }

        public Set<?> getExpandedNodes()
        {
            return expandedNodes;
        }
    }
}
