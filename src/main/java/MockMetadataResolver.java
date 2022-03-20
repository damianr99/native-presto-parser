/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//package com.facebook.presto.sql.analyzer;

import com.facebook.presto.common.ColumnHandle;
import com.facebook.presto.common.ColumnMetadata;
import com.facebook.presto.common.ConnectorId;
import com.facebook.presto.common.ConnectorTableHandle;
import com.facebook.presto.common.ConnectorTableMetadata;
import com.facebook.presto.common.ConnectorTransactionHandle;
import com.facebook.presto.common.QualifiedObjectName;
import com.facebook.presto.common.SchemaTableName;
import com.facebook.presto.common.TableHandle;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.sql.analyzer.MetadataResolver;
import com.facebook.presto.sql.analyzer.TableMetadata;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.facebook.presto.common.type.DateType.DATE;
import static com.facebook.presto.common.type.IntegerType.INTEGER;
import static com.facebook.presto.common.type.RealType.REAL;
import static com.facebook.presto.common.type.VarcharType.VARCHAR;
import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

public class MockMetadataResolver
        implements MetadataResolver {
    private final TableHandle testTable = new TableHandle(
            new ConnectorId("tpch"),
            new ConnectorTableHandle() {},
            new ConnectorTransactionHandle() {},
            Optional.empty());

    private final List<ColumnMetadata> columns = ImmutableList.of(
            new ColumnMetadata("orderkey", INTEGER),
            new ColumnMetadata("custkey", INTEGER),
            new ColumnMetadata("orderstatus", VARCHAR),
            new ColumnMetadata("totalprice", REAL),
            new ColumnMetadata("orderdate", DATE),
            new ColumnMetadata("orderpriority", VARCHAR),
            new ColumnMetadata("clerk", VARCHAR),
            new ColumnMetadata("shippriority", INTEGER),
            new ColumnMetadata("comment", VARCHAR),
            new ColumnMetadata("dummy", VARCHAR));

    @Override
    public Optional<TableHandle> getTableHandle(ConnectorSession session, QualifiedObjectName tableName)
    {
        // we just support a single table in our prototype
        checkArgument(tableName.getObjectName().equals("orders"), format("table %s not found in metastore", tableName));
        return Optional.of(testTable);
    }

    @Override
    public TableMetadata getTableMetadata(ConnectorSession session, TableHandle tableHandle)
    {
        checkArgument(tableHandle.equals(testTable));
        return new TableMetadata(
                new ConnectorId("tpch"),
                new ConnectorTableMetadata(
                        new SchemaTableName("tiny", "orders"),
                        columns));
    }

    @Override
    public Map<String, ColumnHandle> getColumnHandles(ConnectorSession session, TableHandle tableHandle)
    {
        return columns.stream().collect(Collectors.toMap(ColumnMetadata::getName, x -> new ColumnHandle() {}));
    }
}
