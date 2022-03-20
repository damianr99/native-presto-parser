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

import com.facebook.presto.common.type.Type;
import com.facebook.presto.common.type.TypeManager;
import com.facebook.presto.common.type.TypeSignature;
import com.facebook.presto.common.type.TypeSignatureParameter;
import com.facebook.presto.sql.analyzer.TypeCoercer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.facebook.presto.common.type.BigintType.BIGINT;
import static com.facebook.presto.common.type.BooleanType.BOOLEAN;
import static com.facebook.presto.common.type.DateType.DATE;
import static com.facebook.presto.common.type.DoubleType.DOUBLE;
import static com.facebook.presto.common.type.HyperLogLogType.HYPER_LOG_LOG;
import static com.facebook.presto.common.type.IntegerType.INTEGER;
import static com.facebook.presto.common.type.IntervalDayTimeType.INTERVAL_DAY_TIME;
import static com.facebook.presto.common.type.IntervalYearMonthType.INTERVAL_YEAR_MONTH;
import static com.facebook.presto.common.type.JsonType.JSON;
import static com.facebook.presto.common.type.KHyperLogLogType.K_HYPER_LOG_LOG;
import static com.facebook.presto.common.type.P4HyperLogLogType.P4_HYPER_LOG_LOG;
import static com.facebook.presto.common.type.RealType.REAL;
import static com.facebook.presto.common.type.SetDigestType.SET_DIGEST;
import static com.facebook.presto.common.type.SmallintType.SMALLINT;
import static com.facebook.presto.common.type.TimeType.TIME;
import static com.facebook.presto.common.type.TimeWithTimeZoneType.TIME_WITH_TIME_ZONE;
import static com.facebook.presto.common.type.TimestampType.TIMESTAMP;
import static com.facebook.presto.common.type.TimestampWithTimeZoneType.TIMESTAMP_WITH_TIME_ZONE;
import static com.facebook.presto.common.type.TinyintType.TINYINT;
import static com.facebook.presto.common.type.UnknownType.UNKNOWN;
import static com.facebook.presto.common.type.VarbinaryType.VARBINARY;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

public class MockTypeManager
        implements TypeManager
{
    private final ConcurrentMap<TypeSignature, Type> types = new ConcurrentHashMap<>();
    private final TypeCoercer typeCoercer;

    public MockTypeManager()
    {
        this.typeCoercer = new TypeCoercer(this);
        addType(UNKNOWN);
        addType(BOOLEAN);
        addType(BIGINT);
        addType(INTEGER);
        addType(SMALLINT);
        addType(TINYINT);
        addType(DOUBLE);
        addType(REAL);
        addType(VARBINARY);
        addType(DATE);
        addType(TIME);
        addType(TIME_WITH_TIME_ZONE);
        addType(TIMESTAMP);
        addType(TIMESTAMP_WITH_TIME_ZONE);
        addType(INTERVAL_YEAR_MONTH);
        addType(INTERVAL_DAY_TIME);
        addType(HYPER_LOG_LOG);
        addType(SET_DIGEST);
        addType(K_HYPER_LOG_LOG);
        addType(P4_HYPER_LOG_LOG);
        addType(JSON);
    }

    public void addType(Type type)
    {
        requireNonNull(type, "type is null");
        Type existingType = types.putIfAbsent(type.getTypeSignature(), type);
        checkState(existingType == null || existingType.equals(type), "Type %s is already registered", type);
    }

    @Override
    public Type getType(TypeSignature signature)
    {
        return types.get(signature);
    }

    @Override
    public Type getParameterizedType(String baseTypeName, List<TypeSignatureParameter> typeParameters)
    {
        throw new RuntimeException("we don't support parametrized type in the prototype");
    }

    @Override
    public boolean canCoerce(Type actualType, Type expectedType)
    {
        return typeCoercer.canCoerce(actualType, expectedType);
    }
}
