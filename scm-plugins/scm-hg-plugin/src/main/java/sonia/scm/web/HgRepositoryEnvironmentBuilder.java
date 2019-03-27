package sonia.scm.web;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.HgEnvironment;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.security.CipherUtil;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.cgi.EnvList;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Base64;
import java.util.Map;

public class HgRepositoryEnvironmentBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(HgRepositoryEnvironmentBuilder.class);

  private static final String ENV_REPOSITORY_NAME = "REPO_NAME";
  private static final String ENV_REPOSITORY_PATH = "SCM_REPOSITORY_PATH";
  private static final String ENV_REPOSITORY_ID = "SCM_REPOSITORY_ID";
  private static final String ENV_PYTHON_HTTPS_VERIFY = "PYTHONHTTPSVERIFY";
  private static final String ENV_HTTP_POST_ARGS = "SCM_HTTP_POST_ARGS";
  private static final String SCM_CREDENTIALS = "SCM_CREDENTIALS";

  private final HgRepositoryHandler handler;
  private final HgHookManager hookManager;

  @Inject
  public HgRepositoryEnvironmentBuilder(HgRepositoryHandler handler, HgHookManager hookManager) {
    this.handler = handler;
    this.hookManager = hookManager;
  }

  void buildFor(Repository repository, HttpServletRequest request, EnvList environment) {
    File directory = handler.getDirectory(repository.getId());

    environment.set(ENV_REPOSITORY_NAME, repository.getNamespace() + "/" + repository.getName());
    environment.set(ENV_REPOSITORY_ID, repository.getId());
    environment.set(ENV_REPOSITORY_PATH,
      directory.getAbsolutePath());

    // add hook environment
    Map<String, String> environmentMap = environment.asMutableMap();
    if (handler.getConfig().isDisableHookSSLValidation()) {
      // disable ssl validation
      // Issue 959: https://goo.gl/zH5eY8
      environmentMap.put(ENV_PYTHON_HTTPS_VERIFY, "0");
    }

    // enable experimental httppostargs protocol of mercurial
    // Issue 970: https://goo.gl/poascp
    environmentMap.put(ENV_HTTP_POST_ARGS, String.valueOf(handler.getConfig().isEnableHttpPostArgs()));

    HgEnvironment.prepareEnvironment(
      environmentMap,
      handler,
      hookManager,
      request
    );

    addCredentials(environment, request);
  }

  private void addCredentials(EnvList env, HttpServletRequest request)
  {
    String authorization = request.getHeader(HttpUtil.HEADER_AUTHORIZATION);

    if (!Strings.isNullOrEmpty(authorization))
    {
      if (authorization.startsWith(HttpUtil.AUTHORIZATION_SCHEME_BASIC))
      {
        String encodedUserInfo =
          authorization.substring(
            HttpUtil.AUTHORIZATION_SCHEME_BASIC.length()).trim();
        // TODO check encoding of user-agent ?
        String userInfo = new String(Base64.getDecoder().decode(encodedUserInfo));

        env.set(SCM_CREDENTIALS, CipherUtil.getInstance().encode(userInfo));
      }
      else
      {
        LOG.warn("unknown authentication scheme used");
      }
    }
    else
    {
      LOG.trace("no authorization header found");
    }
  }
}
