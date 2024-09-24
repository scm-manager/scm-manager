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

public final class RepositoryTestData {

  public static final String NAMESPACE = "hitchhiker";
  public static final String MAIL_DOMAIN = "@hitchhiker.com";

  private RepositoryTestData() {
  }

  public static Repository create42Puzzle() {
    return create42Puzzle(DummyRepositoryHandler.TYPE_NAME);
  }

  public static Repository create42Puzzle(String type) {
    return new RepositoryBuilder()
      .type(type)
      .contact("douglas.adams" + MAIL_DOMAIN)
      .name("42Puzzle")
      .namespace(NAMESPACE)
      .description("The 42 Puzzle")
      .build();
  }

  public static Repository createHappyVerticalPeopleTransporter() {
    return createHappyVerticalPeopleTransporter(
      DummyRepositoryHandler.TYPE_NAME);
  }

  public static Repository createHappyVerticalPeopleTransporter(String type) {
    return new RepositoryBuilder()
      .type(type)
      .contact("zaphod.beeblebrox" + MAIL_DOMAIN)
      .name("happyVerticalPeopleTransporter")
      .namespace(NAMESPACE)
      .description("Happy Vertical People Transporter")
      .build();
  }

  public static Repository createHeartOfGold() {
    return createHeartOfGold(DummyRepositoryHandler.TYPE_NAME);
  }

  public static Repository createHeartOfGold(String type) {
    return new RepositoryBuilder()
      .type(type)
      .contact("zaphod.beeblebrox" + MAIL_DOMAIN)
      .name("HeartOfGold")
      .namespace(NAMESPACE)
      .description(
        "Heart of Gold is the first prototype ship to successfully utilise the revolutionary Infinite Improbability Drive")
      .build();
  }

  public static Repository createRestaurantAtTheEndOfTheUniverse() {
    return createRestaurantAtTheEndOfTheUniverse(
      DummyRepositoryHandler.TYPE_NAME);
  }

  public static Repository createRestaurantAtTheEndOfTheUniverse(String type) {
    return new RepositoryBuilder()
      .type(type)
      .contact("douglas.adams" + MAIL_DOMAIN)
      .name("RestaurantAtTheEndOfTheUniverse")
      .namespace(NAMESPACE)
      .description("The Restaurant at the End of the Universe")
      .build();
  }
}
