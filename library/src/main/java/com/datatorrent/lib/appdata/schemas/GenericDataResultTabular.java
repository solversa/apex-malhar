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

import com.datatorrent.lib.appdata.gpo.GPOMutable;
import com.datatorrent.lib.appdata.qr.DataSerializerInfo;
import com.datatorrent.lib.appdata.qr.DataType;
import com.datatorrent.lib.appdata.qr.Result;
import com.google.common.base.Preconditions;

import java.util.List;

@DataType(type=GenericDataQuery.TYPE)
@DataSerializerInfo(clazz=GenericDataResultTabularSerializer.class)
public class GenericDataResultTabular extends Result
{
  public static final String TYPE = "dataResult";
  public static final String FIELD_COUNTDOWN = "countdown";

  private List<GPOMutable> values;
  private long countdown;
  private boolean oneTime;

  public GenericDataResultTabular()
  {
  }

  public GenericDataResultTabular(GenericDataQueryTabular query,
                                  List<GPOMutable> values)
  {
    super(query);

    setValues(values);

    this.oneTime = true;
  }

  public GenericDataResultTabular(GenericDataQueryTabular query,
                                  List<GPOMutable> values,
                                  long countdown)
  {
    super(query);

    setValues(values);
    setCountdown(countdown);

    this.oneTime = false;
  }

  private void setValues(List<GPOMutable> values)
  {
    Preconditions.checkNotNull(values);
    this.values = values;
  }

  @Override
  public GenericDataQueryTabular getQuery()
  {
    return (GenericDataQueryTabular) super.getQuery();
  }

  public List<GPOMutable> getValues()
  {
    return values;
  }

  private void setCountdown(long countdown)
  {
    this.countdown = countdown;
  }

  public long getCountdown()
  {
    return countdown;
  }

  /**
   * @return the oneTime
   */
  public boolean isOneTime()
  {
    return oneTime;
  }
}
