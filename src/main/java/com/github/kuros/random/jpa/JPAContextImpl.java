package com.github.kuros.random.jpa;

import com.github.kuros.random.jpa.cache.Cache;
import com.github.kuros.random.jpa.cleanup.Cleaner;
import com.github.kuros.random.jpa.cleanup.CleanerImpl;
import com.github.kuros.random.jpa.definition.HierarchyGraph;
import com.github.kuros.random.jpa.definition.MinimumHierarchyGenerator;
import com.github.kuros.random.jpa.persistor.EntityPersistorImpl;
import com.github.kuros.random.jpa.persistor.Persistor;
import com.github.kuros.random.jpa.persistor.model.ResultMap;
import com.github.kuros.random.jpa.persistor.model.ResultMapImpl;
import com.github.kuros.random.jpa.random.Randomize;
import com.github.kuros.random.jpa.random.RandomizeImpl;
import com.github.kuros.random.jpa.random.generator.Generator;
import com.github.kuros.random.jpa.random.generator.RandomGenerator;
import com.github.kuros.random.jpa.resolver.CreationOrderResolver;
import com.github.kuros.random.jpa.resolver.CreationOrderResolverImpl;
import com.github.kuros.random.jpa.resolver.PersistedEntityResolver;
import com.github.kuros.random.jpa.types.AttributeIndexValue;
import com.github.kuros.random.jpa.types.AttributeValue;
import com.github.kuros.random.jpa.types.ClassIndex;
import com.github.kuros.random.jpa.types.CreationOrder;
import com.github.kuros.random.jpa.types.CreationPlan;
import com.github.kuros.random.jpa.types.CreationPlanImpl;
import com.github.kuros.random.jpa.types.DeletionOrder;
import com.github.kuros.random.jpa.types.Entity;
import com.github.kuros.random.jpa.types.EntityHelper;
import com.github.kuros.random.jpa.types.Plan;
import com.github.kuros.random.jpa.util.MergeUtil;
import com.github.kuros.random.jpa.v1.resolver.CreationPlanResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
public final class JPAContextImpl implements JPAContext {

    private RandomGenerator generator;
    private Cache cache;

    private JPAContextImpl(final Cache cache, final Generator generator) {
        this.generator = RandomGenerator.newInstance(cache, generator);
        this.cache = cache;
    }

    public static JPAContext newInstance(final Cache cache,
                                         final Generator generator) {
        return new JPAContextImpl(cache, generator);
    }

    @Override
    public CreationPlan create(final Entity... entities) {
        return create(Plan.of(entities));
    }

    @Override
    public ResultMap persist(final CreationPlan creationPlan) {
        final CreationPlanImpl creationPlanImpl = (CreationPlanImpl) creationPlan;
        final Persistor persistor = EntityPersistorImpl.newInstance(cache, creationPlanImpl.getHierarchyGraph(), creationPlanImpl.getRandomize());
        return ResultMapImpl.newInstance(persistor.persist(creationPlan));
    }

    private Randomize getRandomizer() {
        return RandomizeImpl.newInstance(cache, generator);
    }

    @Override
    public ResultMap createAndPersist(final Entity... entities) {
        return createAndPersist(Plan.of(entities));
    }

    @Override
    public <T, V> DeletionOrder getDeletionOrder(final Class<T> type, final V... ids) {
        final Cleaner cleaner = CleanerImpl.newInstance(cache);
        return cleaner.getDeletionOrder(type, ids);
    }

    @Override
    public void remove(final DeletionOrder deletionOrder) {
        final Cleaner cleaner = CleanerImpl.newInstance(cache);
        cleaner.delete(deletionOrder);
    }

    @Override
    public void remove(final Class<?> type) {
        final Cleaner cleaner = CleanerImpl.newInstance(cache);
        cleaner.truncate(type);
    }

    @Override
    public void removeAll() {
        final Cleaner cleaner = CleanerImpl.newInstance(cache);
        cleaner.truncateAll();
    }

    @Override
    public <T, V> void remove(final Class<T> type, final V... ids) {
        final Cleaner cleaner = CleanerImpl.newInstance(cache);
        cleaner.delete(type, ids);
    }

    private ResultMap createAndPersist(final Plan plan) {
        return persist(create(plan));
    }

    public RandomGenerator getGenerator() {
        return generator;
    }

    public Cache getCache() {
        return cache;
    }

    private CreationPlan create(final Plan plan) {

        final List<Entity> entities = plan.getEntities();
        final HierarchyGraph hierarchyGraph = MinimumHierarchyGenerator.generate(getCache().getHierarchyGraph(), entities);

        final CreationOrderResolver creationOrderResolver = CreationOrderResolverImpl.newInstance(hierarchyGraph);

        final List<CreationOrder> creationOrders = new ArrayList<>();
        for (Entity entity : entities) {
                final CreationOrder creationOrder = creationOrderResolver.getCreationOrder(entity);
                creationOrders.add(creationOrder);

        }

        final Collection<CreationOrder> values = MergeUtil.merge(creationOrders);
        sort(values);
        final CreationPlanResolver creationPlanResolver = CreationPlanResolver.newInstance(
                getRandomizer(), toArray(values));
        final CreationPlan creationPlan = creationPlanResolver.with(hierarchyGraph).create();
        addAttributeValues(creationPlan, entities);
        return creationPlan;
    }

    @SuppressWarnings("unchecked")
    void addAttributeValues(final CreationPlan creationPlan, final List<Entity> entities) {
        for (Entity entity : entities) {
            ((List<AttributeValue>) EntityHelper.getAttributeValues(entity))
                    .forEach(attributeValue -> creationPlan.set(PersistedEntityResolver.DEFAULT_INDEX, attributeValue.getAttribute(), attributeValue.getValue()));

            ((List<AttributeIndexValue>) EntityHelper.getAttributeIndexValues(entity))
                    .forEach(e -> creationPlan.set(e.getIndex(), e.getAttribute(), e.getValue()));

            ((List<ClassIndex>)EntityHelper.getClassIndices(entity))
                    .forEach(e -> creationPlan.deleteItem(e.getType(), e.getIndex()));

        }
    }

    private void sort(final Collection<CreationOrder> values) {
        for (CreationOrder value : values) {
            value.getOrder().sort((o1, o2) -> -1 * Integer.compare(o1.getDepth(), o2.getDepth()));
        }
    }

    private CreationOrder[] toArray(final Collection<CreationOrder> values) {
        final CreationOrder[] array = new CreationOrder[values.size()];
        return values.toArray(array);
    }
}
