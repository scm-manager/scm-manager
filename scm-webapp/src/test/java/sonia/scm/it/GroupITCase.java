/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.it;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import sonia.scm.group.Group;

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class GroupITCase extends AbstractAdminITCaseBase
{

  /**
   * Method description
   *
   */
  @Test
  public void create()
  {
    Group group = new Group();

    group.setName("group-a");
    group.setDescription("group a");

    List<String> members = new ArrayList<String>();

    members.add("slarti");
    members.add("marvin");
    members.add("dent");
    group.setMembers(members);
    createGroup(group);
  }

  /**
   * Method description
   *
   */
  @Test
  public void delete()
  {
    Group group = new Group();

    group.setName("group-b");
    group.setDescription("group b");

    List<String> members = new ArrayList<String>();

    members.add("slarti");
    members.add("dent");
    group.setMembers(members);
    createGroup(group);
    deleteGroup(group.getName());
  }

  /**
   * Method description
   *
   */
  @Test
  public void modify()
  {
    Group group = new Group();

    group.setName("group-d");
    group.setDescription("group d");
    createGroup(group);
    group = getGroup(group.getName());
    group.setDescription("GROUP D");

    WebResource wr = createResource(client, "groups/group-d");
    ClientResponse response = wr.put(ClientResponse.class, group);

    assertNotNull(response);
    assertTrue(response.getStatus() == 204);
    response.close();

    Group other = getGroup("group-d");

    assertEquals(group.getName(), other.getName());
    assertEquals(group.getDescription(), other.getDescription());
    assertNotNull(other.getLastModified());
    deleteGroup(other.getName());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void getAll()
  {
    Group group = new Group();

    group.setName("group-c");
    createGroup(group);

    WebResource wr = createResource(client, "groups");
    ClientResponse response = wr.get(ClientResponse.class);
    Collection<Group> groups =
      response.getEntity(new GenericType<Collection<Group>>() {}
    );

    response.close();
    assertNotNull(groups);
    assertFalse(groups.isEmpty());

    Group groupC = null;

    for (Group g : groups)
    {
      if (g.getName().equals("group-c"))
      {
        groupC = g;
      }
    }

    assertNotNull(groupC);
    assertNotNull(groupC.getCreationDate());
    assertNotNull(groupC.getType());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param group
   */
  private void createGroup(Group group)
  {
    WebResource wr = createResource(client, "groups");
    ClientResponse response = wr.post(ClientResponse.class, group);

    assertNotNull(response);
    assertTrue(response.getStatus() == 201);
    response.close();

    Group other = getGroup(group.getName());

    assertNotNull(other);
    assertNotNull(other.getType());
    assertEquals(group.getName(), other.getName());
    assertEquals(group.getDescription(), other.getDescription());
    assertArrayEquals(group.getMembers().toArray(new String[0]),
                      other.getMembers().toArray(new String[0]));
    assertNotNull(other.getCreationDate());
  }

  /**
   * Method description
   *
   *
   * @param name
   */
  private void deleteGroup(String name)
  {
    WebResource wr = createResource(client, "groups/".concat(name));
    ClientResponse response = wr.delete(ClientResponse.class);

    assertNotNull(response);
    assertTrue(response.getStatus() == 204);
    response.close();
    wr = createResource(client, "groups/".concat(name));
    response = wr.get(ClientResponse.class);
    assertNotNull(response);
    assertTrue(response.getStatus() == 404);
    response.close();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param groupname
   *
   * @return
   */
  private Group getGroup(String groupname)
  {
    WebResource wr = createResource(client, "groups/".concat(groupname));
    ClientResponse response = wr.get(ClientResponse.class);

    assertNotNull(response);

    Group group = response.getEntity(Group.class);

    response.close();
    assertNotNull(group);
    assertEquals(group.getName(), groupname);

    return group;
  }
}
