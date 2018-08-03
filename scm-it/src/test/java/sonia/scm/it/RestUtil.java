package sonia.scm.it;

import com.google.common.io.Resources;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

import static java.util.Arrays.asList;

public class RestUtil {

  public static final String BASE_URL = "http://localhost:8081/scm/";
  public static final String REST_BASE_URL = BASE_URL.concat("api/rest/v2/");

  public static Response lastResponse;

  public static URI createResourceUrl(String url)
  {
    return URI.create(REST_BASE_URL).resolve(url);
  }

  public static String readJson(String jsonFileName) throws IOException {
    URL url = Resources.getResource(jsonFileName);
    return Resources.toString(url, Charset.forName("UTF-8"));
  }

  public static RequestSpecification given(String mediaType) {
    RequestSpecification requestSpecification = RestAssured.given()
      .contentType(mediaType)
      .accept(mediaType)
      .auth().preemptive().basic("scmadmin", "scmadmin");
    return wrapRequestSpecification(requestSpecification);
  }

  private static RequestSpecification wrapRequestSpecification(RequestSpecification requestSpecification) {
    return (RequestSpecification) Proxy.newProxyInstance(RestUtil.class.getClassLoader(), new Class[]{RequestSpecification.class}, new RequestSpecificationWrapper(requestSpecification));
  }

  private static class RequestSpecificationWrapper implements InvocationHandler {

    private final RequestSpecification delegate;

    private RequestSpecificationWrapper(RequestSpecification delegate) {
      this.delegate = delegate;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
      if (asList("get", "put", "post", "delete").contains(method.getName())) {
        lastResponse = (Response) method.invoke(delegate, args);
        return lastResponse;
      } else if (method.getReturnType().equals(RequestSpecification.class)) {
        return wrapRequestSpecification((RequestSpecification) method.invoke(delegate, args));
      } else {
        return method.invoke(delegate, args);
      }
    }
  }
}
