/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.user.xml;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.user.User;

//~--- JDK imports ------------------------------------------------------------

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Sebastian Sdorra
 */
public class XmlUserMapAdapter
        extends XmlAdapter<XmlUserList, Map<String, User>>
{

  /**
   * Method description
   *
   *
   * @param userMap
   *
   * @return
   *
   * @throws Exception
   */
  @Override
  public XmlUserList marshal(Map<String, User> userMap) throws Exception
  {
    return new XmlUserList(userMap);
  }

  /**
   * Method description
   *
   *
   * @param users
   *
   * @return
   *
   * @throws Exception
   */
  @Override
  public Map<String, User> unmarshal(XmlUserList users) throws Exception
  {
    Map<String, User> userMap = new LinkedHashMap<>();

    for (User user : users)
    {
      userMap.put(user.getName(), user);
    }

    return userMap;
  }
}
