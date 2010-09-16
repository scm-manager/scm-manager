/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sonia.scm;

import com.google.inject.Module;
import java.io.InputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmWebPluginAdapter implements ScmWebPlugin {

  @Override
  public Module[] getModules()
  {
    return new Module[0];
  }

  @Override
  public InputStream getScript()
  {
    return null;
  }

}
