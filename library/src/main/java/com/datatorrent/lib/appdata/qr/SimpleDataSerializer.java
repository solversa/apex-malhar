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

import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;

public class SimpleDataSerializer implements CustomDataSerializer
{
  private ObjectMapper om = new ObjectMapper();

  public SimpleDataSerializer()
  {
  }

  @Override
  public String serialize(Result result)
  {
    try {
      return om.writeValueAsString(result);
    }
    catch(IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }
}
