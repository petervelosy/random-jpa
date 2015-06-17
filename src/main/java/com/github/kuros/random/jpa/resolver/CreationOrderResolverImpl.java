package com.github.kuros.random.jpa.resolver;

import com.github.kuros.random.jpa.cache.PreconditionCache;
import com.github.kuros.random.jpa.definition.HierarchyGraph;
import com.github.kuros.random.jpa.exception.RandomJPAException;
import com.github.kuros.random.jpa.types.CreationOrder;
import com.github.kuros.random.jpa.types.Entity;
import com.github.kuros.random.jpa.types.Plan;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

/*
 * Copyright (c) 2015 Kumar Rohit
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License or any
 *    later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public final class CreationOrderResolverImpl implements CreationOrderResolver {

    private HierarchyGraph hierarchyGraph;
    private Plan plan;


    private CreationOrderResolverImpl(final HierarchyGraph hierarchyGraph, final Plan plan) {
        this.hierarchyGraph = hierarchyGraph;
        this.plan = plan;
    }

    public static CreationOrderResolver newInstance(final HierarchyGraph hierarchyGraph, final Plan plan) {
        return new CreationOrderResolverImpl(hierarchyGraph, plan);
    }

    public CreationOrder getCreationOrder() {
        final CreationOrder creationOrder = CreationOrder.newInstance(hierarchyGraph);
        final List<Entity> entities = plan.getEntities();
        for (Entity entity : entities) {
            final Class type = entity.getType();
            addCreationCount(creationOrder, entity);
            try {
                generateCreationOrder(creationOrder, type);
                applyPrecondition(creationOrder);
            } catch (final ClassNotFoundException e) {
                throw new RandomJPAException("Class Not Found", e);
            }
        }

        return creationOrder;
    }

    private void applyPrecondition(final CreationOrder creationOrder) throws ClassNotFoundException {

        final Set<Class<?>> identifiers = PreconditionCache.getInstance().getIdentifiers();
        for (Class<?> identifier : identifiers) {

            if (!creationOrder.contains(identifier)) {
                continue;
            }

            final Plan preConditionPlan = PreconditionCache.getInstance().getPlan(identifier);
            for (Entity entity : preConditionPlan.getEntities()) {

                final CreationOrder tempCreationOrder = CreationOrder.newInstance(hierarchyGraph);
                generateCreationOrder(tempCreationOrder, entity.getType());
                final List<Class<?>> newOrder = tempCreationOrder.getOrder();
                final List<Class<?>> createdOrder = creationOrder.getOrder();
                final int minIndex = getMinIndex(createdOrder, newOrder);

                Class<?> location = null;
                int i = minIndex;
                while (i > 0) {
                    final Class<?> aClass = createdOrder.get(--i);
                    if (!newOrder.contains(aClass)) {
                        location = aClass;
                        break;
                    }
                }

                createdOrder.removeAll(newOrder);

                i = minIndex;
                if (location != null) {
                    i = createdOrder.indexOf(location);
                }
                createdOrder.addAll(i + 1, newOrder);
            }
        }
    }

    private void addCreationCount(final CreationOrder creationOrder, final Entity entity) {
        creationOrder.addCreationCount(entity.getType(), entity.getCount());
    }

    private void generateCreationOrder(final CreationOrder creationOrder, final Class<?> type) throws ClassNotFoundException {

        final Queue<String> queue = new PriorityQueue<String>();
        queue.offer(type.getName());
        final Stack<Class<?>> stack = new Stack<Class<?>>();
        stack.push(type);

        while (!queue.isEmpty()) {
            final Class<?> polledClass = Class.forName(queue.poll());
            final Set<Class<?>> parents = hierarchyGraph.getParents(polledClass);
            Integer index = null;
            for (Class<?> parent : parents) {
                if (!stack.contains(parent)) {
                    queue.offer(parent.getName());
                } else {
                    if (index == null || index > stack.indexOf(parent)) {
                        index = stack.indexOf(parent);
                    }
                }
            }

            if (index != null) {
                stack.add(index, polledClass);
            } else if (!stack.contains(polledClass)) {
                stack.push(polledClass);
            }
        }

        while (!stack.isEmpty()) {
            final Class<?> pop = stack.pop();
            if (!creationOrder.contains(pop)) {
                creationOrder.add(pop);
            }
        }
    }

    private int getMinIndex(final List<Class<?>> from, final List<Class<?>> order) {
        int index = from.size();

        for (Class<?> type : order) {
            final int i = from.indexOf(type);
            if (index > i && i != -1) {
                index = i;
            }
        }

        return index;
    }
}
