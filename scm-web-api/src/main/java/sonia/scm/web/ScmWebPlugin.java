/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Module;

//~--- JDK imports ------------------------------------------------------------

import java.io.InputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public interface ScmWebPlugin
{

public void contextInitialized( ScmWebPluginContext context );

public void contextDestroyed( ScmWebPluginContext context );
}
