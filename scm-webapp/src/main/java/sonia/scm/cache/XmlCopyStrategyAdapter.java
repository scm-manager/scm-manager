/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.cache;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;


public class XmlCopyStrategyAdapter extends XmlAdapter<String, CopyStrategy>
{


  @Override
  public String marshal(CopyStrategy v) throws Exception
  {
    return v.getConfigName();
  }


  @Override
  public CopyStrategy unmarshal(String v) throws Exception
  {
    return CopyStrategy.fromString(v);
  }
}
