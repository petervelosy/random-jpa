package com.github.kuros.random.jpa.provider.mssql;

import com.github.kuros.random.jpa.annotation.VisibleForTesting;
import com.github.kuros.random.jpa.provider.base.AbstractRelationshipProvider;

import javax.persistence.EntityManager;

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
public final class MSSQLRelationshipProvider extends AbstractRelationshipProvider {

    private static final String QUERY = "SELECT\n" +
            "  tp.name 'parent_table',\n" +
            "  cp.name 'parent_attribute',\n" +
            "  tr.name 'referenced_table',\n" +
            "  cr.name 'referenced_attribute'--,\n" +
            "FROM\n" +
            "  sys.foreign_keys fk\n" +
            "  INNER JOIN\n" +
            "  sys.tables tp ON fk.parent_object_id = tp.object_id\n" +
            "  INNER JOIN\n" +
            "  sys.tables tr ON fk.referenced_object_id = tr.object_id\n" +
            "  INNER JOIN\n" +
            "  sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id\n" +
            "  INNER JOIN\n" +
            "  sys.columns cp ON fkc.parent_column_id = cp.column_id AND fkc.parent_object_id = cp.object_id\n" +
            "  INNER JOIN\n" +
            "  sys.columns cr ON fkc.referenced_column_id = cr.column_id AND fkc.referenced_object_id = cr.object_id\n" +
            "  ORDER BY\n" +
            "  tp.name, cp.column_id";

    @VisibleForTesting
    MSSQLRelationshipProvider(final EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    protected String getQuery() {
        return QUERY;
    }

    public static MSSQLRelationshipProvider newInstance(final EntityManager entityManager) {
        return new MSSQLRelationshipProvider(entityManager);
    }

}
