/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.agent.resources;

//~--- JDK imports ------------------------------------------------------------

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("/agent")
public class AgentResource
{

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  @Produces("text/plain")
  public String hello()
  {
    return "Hello from Agent";
  }
}
