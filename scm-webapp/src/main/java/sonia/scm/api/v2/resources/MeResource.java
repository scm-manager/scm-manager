package sonia.scm.api.v2.resources;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shiro.SecurityUtils;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path(MeResource.ME_PATH_V2)
public class MeResource {
  static final String ME_PATH_V2 = "v2/me/";

  @GET
  @Produces(VndMediaType.ME)
  public Response get() {
    MeDto meDto = new MeDto((String) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal());
    return Response.ok(meDto).build();
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Setter
  class MeDto {
    String username;
  }

}
