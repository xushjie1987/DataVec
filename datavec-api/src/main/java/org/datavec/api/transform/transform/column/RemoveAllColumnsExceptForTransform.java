/*
 *  * Copyright 2016 Skymind, Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 */

package org.datavec.api.transform.transform.column;

import org.nd4j.shade.jackson.annotation.JsonIgnoreProperties;
import org.nd4j.shade.jackson.annotation.JsonProperty;
import org.datavec.api.transform.metadata.ColumnMetaData;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.transform.transform.BaseTransform;
import org.datavec.api.writable.Writable;

import java.util.*;

/**
 * Transform that removes all columns except for those that are explicitly specified as ones to keep
 * To specify only the columns to remove, use {@link RemoveColumnsTransform}
 *
 * @author Alex Black
 */
@JsonIgnoreProperties({"inputSchema", "columnsToKeepIdx", "indicesToKeep"})
public class RemoveAllColumnsExceptForTransform extends BaseTransform {

    private int[] columnsToKeepIdx;
    private String[] columnsToKeep;
    private Set<Integer> indicesToKeep;

    public RemoveAllColumnsExceptForTransform(@JsonProperty("columnsToKeep") String... columnsToKeep) {
        this.columnsToKeep = columnsToKeep;
    }

    @Override
    public void setInputSchema(Schema schema) {
        super.setInputSchema(schema);

        indicesToKeep = new HashSet<>();

        int i = 0;
        columnsToKeepIdx = new int[columnsToKeep.length];
        for (String s : columnsToKeep) {
            int idx = schema.getIndexOfColumn(s);
            if (idx < 0) throw new RuntimeException("Column \"" + s + "\" not found");
            columnsToKeepIdx[i++] = idx;
            indicesToKeep.add(idx);
        }
    }

    @Override
    public Schema transform(Schema schema) {
        List<String> origNames = schema.getColumnNames();
        List<ColumnMetaData> origMeta = schema.getColumnMetaData();

        Set<String> keepSet = new HashSet<>();
        Collections.addAll(keepSet, columnsToKeep);


        List<ColumnMetaData> newMeta = new ArrayList<>(columnsToKeep.length);

        Iterator<String> namesIter = origNames.iterator();
        Iterator<ColumnMetaData> metaIter = origMeta.iterator();

        while (namesIter.hasNext()) {
            String n = namesIter.next();
            ColumnMetaData t = metaIter.next();
            if (keepSet.contains(n)) {
                newMeta.add(t);
            }
        }

        return schema.newSchema(newMeta);
    }

    @Override
    public List<Writable> map(List<Writable> writables) {
        if (writables.size() != inputSchema.numColumns()) {
            throw new IllegalStateException("Cannot execute transform: input writables list length (" + writables.size() + ") does not " +
                    "match expected number of elements (schema: " + inputSchema.numColumns() + "). Transform = " + toString());
        }

        List<Writable> outList = new ArrayList<>(columnsToKeep.length);

        int i = 0;
        for (Writable w : writables) {
            if (!indicesToKeep.contains(i++)) continue;
            outList.add(w);
        }
        return outList;
    }

    @Override
    public String toString() {
        return "RemoveAllColumnsExceptForTransform(" + Arrays.toString(columnsToKeep) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoveAllColumnsExceptForTransform o2 = (RemoveAllColumnsExceptForTransform) o;

        return Arrays.equals(columnsToKeep, o2.columnsToKeep);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(columnsToKeep);
    }
}