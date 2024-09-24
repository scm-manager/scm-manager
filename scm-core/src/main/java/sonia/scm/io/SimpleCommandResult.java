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

package sonia.scm.io;


public class SimpleCommandResult implements CommandResult
{
  private String output;

  private int returnCode;

  public SimpleCommandResult(String output, int returnCode)
  {
    this.output = output;
    this.returnCode = returnCode;
  }

  @Override
  public String getOutput()
  {
    return output;
  }

  
  @Override
  public int getReturnCode()
  {
    return returnCode;
  }

  
  @Override
  public boolean isSuccessfull()
  {
    return returnCode == 0;
  }

}
