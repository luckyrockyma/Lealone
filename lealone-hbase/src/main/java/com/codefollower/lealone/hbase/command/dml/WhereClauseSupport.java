/*
 * Copyright 2011 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codefollower.lealone.hbase.command.dml;

import com.codefollower.lealone.dbobject.table.TableFilter;
import com.codefollower.lealone.hbase.dbobject.table.HBaseTable;
import com.codefollower.lealone.result.SearchRow;
import com.codefollower.lealone.value.Value;

public class WhereClauseSupport {
    private TableFilter tableFilter;
    private byte[] tableNameAsBytes;
    private String regionName;
    private String[] localRegionNames;

    public WhereClauseSupport() {
    }

    public void setTableFilter(TableFilter tableFilter) {
        this.tableFilter = tableFilter;
        tableNameAsBytes = ((HBaseTable) tableFilter.getTable()).getTableNameAsBytes();
    }

    public byte[] getTableNameAsBytes() {
        return tableNameAsBytes;
    }

    public Value getStartRowKeyValue() {
        SearchRow start = tableFilter.getStartSearchRow();
        if (start != null)
            return start.getRowKey();
        return null;
    }

    public Value getEndRowKeyValue() {
        SearchRow end = tableFilter.getEndSearchRow();
        if (end != null)
            return end.getRowKey();
        return null;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String[] getLocalRegionNames() {
        return localRegionNames;
    }

    public void setLocalRegionNames(String[] localRegionNames) {
        this.localRegionNames = localRegionNames;
    }

    //    @Override
    //    public void setLocal(boolean local) {
    //        this.local = local;
    //    }
    //
    //    @Override
    //    public boolean isLocal() {
    //        return local;
    //    }
}