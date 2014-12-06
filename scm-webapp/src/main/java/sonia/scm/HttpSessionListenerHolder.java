
package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.Extension;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Dispatcher for {@link HttpSessionEvent}. The {@link HttpSessionListenerHolder}
 * loads all registered {@link HttpSessionListener}s from the {@link Injector}
 * and delegates the events to the them. {@link HttpSessionListener} can be
 * registered with the {@link Extension} annotation.
 *
 * @author Sebastian Sdorra <sebastian.sdorra@gmail.com>
 * @since 1.42
 */
public class HttpSessionListenerHolder implements HttpSessionListener
{

  /** key type of the session listeners */
  private static final Key<Set<HttpSessionListener>> KEY =
    Key.get(new TypeLiteral<Set<HttpSessionListener>>() {}
  );

  /** logger for HttpSessionListenerHolder */
  private static final Logger logger =
    LoggerFactory.getLogger(HttpSessionListenerHolder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new HttpSessionListenerHolder.
   *
   */
  public HttpSessionListenerHolder()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create instance of {}",
        HttpSessionListenerHolder.class.getName());
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Delegates the create session event to all registered
   * {@ĺink HttpSessionListener}s.
   *
   *
   * @param event session event
   */
  @Override
  public void sessionCreated(HttpSessionEvent event)
  {
    if (listenerSet == null)
    {
      listenerSet = loadListeners(event);
    }

    dispatch(event, true);
  }

  /**
   * Delegates the destroy session event to all registered
   * {@ĺink HttpSessionListener}s.
   *
   *
   * @param event session event
   */
  @Override
  public void sessionDestroyed(HttpSessionEvent event)
  {
    dispatch(event, false);
  }

  /**
   * Dispatch session events.
   *
   *
   * @param event session event
   * @param create {@code true} if the event is a create event
   */
  private void dispatch(HttpSessionEvent event, boolean create)
  {
    if (listenerSet != null)
    {
      for (HttpSessionListener listener : listenerSet)
      {
        if (create)
        {
          listener.sessionCreated(event);
        }
        else
        {
          listener.sessionDestroyed(event);
        }
      }
    }
    else
    {
      logger.warn(
        "could not dispatch session event, because holder is not initialized");
    }
  }

  /**
   * Load listeners from {@link Injector} which is stored in the
   * {@link ServletContext}.
   *
   *
   * @param event session event
   *
   * @return set of session listeners
   */
  private synchronized Set<HttpSessionListener> loadListeners(
    HttpSessionEvent event)
  {
    Set<HttpSessionListener> listeners = null;
    HttpSession session = event.getSession();

    if (session != null)
    {
      Injector injector = (Injector) session.getServletContext().getAttribute(
                            Injector.class.getName());

      if (injector != null)
      {
        logger.debug("load HttpSessionListeners from injector");
        listeners = injector.getInstance(KEY);
      }
      else
      {
        logger.error("could not find injector in servletContext");
      }

      if (listeners == null)
      {
        listeners = Sets.newHashSet();
      }
    }
    else
    {
      logger.warn("received session event without session");
    }

    return listeners;
  }

  //~--- fields ---------------------------------------------------------------

  /** listener set */
  private Set<HttpSessionListener> listenerSet;
}
