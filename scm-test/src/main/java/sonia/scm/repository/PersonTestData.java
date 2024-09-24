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

/**
 *
 * @since 1.18
 */
public final class PersonTestData
{

  public static final Person ADAMS =
    Person.toPerson("Douglas Adams <douglas.adams@hitchhiker.com>");

  public static final Person DENT =
    Person.toPerson("Arthur Dent <arthur.dent@hitchhiker.com>");

  public static final Person MARVIN =
    Person.toPerson("Marvin <paranoid.android@hitchhiker.com>");

  public static final Person PERFECT =
    Person.toPerson("Ford Prefect <ford.perfect@hitchhiker.com>");

  public static final Person SLARTI =
    Person.toPerson("Slartibartfaß <slartibartfass@hitchhiker.com>");

  public static final Person TILLIAN =
    Person.toPerson("Tricia McMillan <tricia.mcmillan@hitchhiker.com>");

  public static final Person ZAPHOD =
    Person.toPerson("Zaphod Beeblebrox <zaphod.beeblebrox@hitchhiker.com>");


  private PersonTestData() {}
}
