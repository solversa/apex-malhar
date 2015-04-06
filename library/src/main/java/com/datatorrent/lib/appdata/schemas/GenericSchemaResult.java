/* 
 * Copyright (c) 2015 DataTorrent, Inc. ALL Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datatorrent.lib.appdata.schemas;

import com.datatorrent.lib.appdata.qr.DataType;
import com.datatorrent.lib.appdata.qr.Result;
import com.datatorrent.lib.appdata.qr.DataSerializerInfo;
import com.google.common.base.Preconditions;

@DataType(type=GenericSchemaResult.TYPE)
@DataSerializerInfo(clazz=GenericSchemaSerializer.class)
public class GenericSchemaResult extends Result
{
  public static final String TYPE = "schemaData";

  private GenericSchema genericSchema;

  public GenericSchemaResult(SchemaQuery schemaQuery,
                      GenericSchema genericSchema)
  {
    super(schemaQuery);
    setGenericSchema(genericSchema);
  }

  private void setGenericSchema(GenericSchema genericSchema)
  {
    Preconditions.checkNotNull(genericSchema);
    this.genericSchema = genericSchema;
  }

  public GenericSchema getGenericSchema()
  {
    return genericSchema;
  }
}
