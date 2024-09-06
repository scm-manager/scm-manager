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

package sonia.scm.event;


import com.github.legman.EventBus;

/**
 *
 * @since 2.0.0
 */
public class ScmTestEventBus extends ScmEventBus
{
  private final EventBus eventBus = new EventBus("testing");

  @Override
  public void post(Object event)
  {
    eventBus.post(event);
  }


  @Override
  public void register(Object subscriber)
  {
    eventBus.register(subscriber);
  }


  @Override
  public void unregister(Object subscriber)
  {
    eventBus.unregister(subscriber);
  }

}
