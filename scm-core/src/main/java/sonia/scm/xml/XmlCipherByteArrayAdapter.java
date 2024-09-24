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

package sonia.scm.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import sonia.scm.security.CipherUtil;

/**
 * @since 2.19.0
 */
public class XmlCipherByteArrayAdapter extends XmlAdapter<byte[], byte[]> {

  @Override
  public byte[] marshal(byte[] v) throws Exception {
    return CipherUtil.getInstance().encode(v);
  }

  @Override
  public byte[] unmarshal(byte[] v) throws Exception {
    return CipherUtil.getInstance().decode(v);
  }
}
