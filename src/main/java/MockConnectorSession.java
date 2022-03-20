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

import com.facebook.presto.common.ConnectorId;
import com.facebook.presto.common.TransactionId;
import com.facebook.presto.common.function.SqlFunctionProperties;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.function.SqlFunctionId;
import com.facebook.presto.spi.function.SqlInvokedFunction;
import com.facebook.presto.spi.security.ConnectorIdentity;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.facebook.presto.common.TransactionId.create;

public class MockConnectorSession
        implements ConnectorSession
{
    @Override
    public String getQueryId()
    {
        return "test-id";
    }

    @Override
    public Optional<String> getSource()
    {
        return Optional.empty();
    }

    @Override
    public String getUser()
    {
        return ConnectorSession.super.getUser();
    }

    @Override
    public ConnectorIdentity getIdentity()
    {
        return null;
    }

    @Override
    public Locale getLocale()
    {
        return Locale.ENGLISH;
    }

    @Override
    public Optional<String> getTraceToken()
    {
        return Optional.empty();
    }

    @Override
    public Optional<String> getClientInfo()
    {
        return Optional.empty();
    }

    @Override
    public Set<String> getClientTags()
    {
        return ImmutableSet.of();
    }

    @Override
    public long getStartTime()
    {
        return 0;
    }

    @Override
    public SqlFunctionProperties getSqlFunctionProperties()
    {
        return SqlFunctionProperties.builder().build();
    }

    @Override
    public Map<SqlFunctionId, SqlInvokedFunction> getSessionFunctions()
    {
        return ImmutableMap.of();
    }

    @Override
    public <T> T getProperty(String name, Class<T> type)
    {
        return null;
    }

    @Override
    public Optional<String> getSchema()
    {
        return Optional.of("tiny");
    }

    @Override
    public ConnectorId getConnectorId()
    {
        return new ConnectorId("tpch");
    }

    @Override
    public TransactionId getTransactionId()
    {
        return create();
    }

    @Override
    public Optional<String> getCatalog()
    {
        return Optional.of("tpch");
    }
}
