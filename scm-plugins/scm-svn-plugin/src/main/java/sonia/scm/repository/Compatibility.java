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

package sonia.scm.repository;


public enum Compatibility
{
  NONE(false, false, false, false, false),
  PRE14(true, true, true, true, false), PRE15(false, true, true, true, false),
  PRE16(false, false, true, true, false),
  PRE17(false, false, false, true, false),
  WITH17(false, false, false, false, true);

  private boolean pre14Compatible;

  private boolean pre15Compatible;

  private boolean pre16Compatible;

  private boolean pre17Compatible;

  private boolean with17Compatible;

  private Compatibility(boolean pre14Compatible, boolean pre15Compatible,
                        boolean pre16Compatible, boolean pre17Compatible,
                        boolean with17Compatible)
  {
    this.pre14Compatible = pre14Compatible;
    this.pre15Compatible = pre15Compatible;
    this.pre16Compatible = pre16Compatible;
    this.pre17Compatible = pre17Compatible;
    this.with17Compatible = with17Compatible;
  }


  
  public boolean isPre14Compatible()
  {
    return pre14Compatible;
  }

  
  public boolean isPre15Compatible()
  {
    return pre15Compatible;
  }

  
  public boolean isPre16Compatible()
  {
    return pre16Compatible;
  }

  
  public boolean isPre17Compatible()
  {
    return pre17Compatible;
  }

  
  public boolean isWith17Compatible()
  {
    return with17Compatible;
  }

}
