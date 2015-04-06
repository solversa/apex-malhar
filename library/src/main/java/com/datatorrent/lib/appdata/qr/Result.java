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
package com.datatorrent.lib.appdata.qr;

import com.google.common.base.Preconditions;

public class Result
{
  public static final String FIELD_ID = "id";
  public static final String FIELD_TYPE = "type";
  public static final String FIELD_DATA = "data";

  private String id;
  private String type;
  private Query query;

  public Result()
  {
  }

  public Result(Query query)
  {
    setQuery(query);
  }

  private void setQuery(Query query)
  {
    Preconditions.checkNotNull(query);
    this.query = query;
  }

  public Query getQuery()
  {
    return query;
  }

  public String getId()
  {
    return query.getId();
  }

  /**
   * @return the type
   */
  public String getType()
  {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type)
  {
    this.type = type;
  }
}
