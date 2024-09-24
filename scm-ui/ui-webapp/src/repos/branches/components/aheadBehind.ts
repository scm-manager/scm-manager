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

export const calculateBarLength = (changesets: number) => {
  if (changesets <= 10) {
    return changesets + 5;
  } else if (changesets <= 50) {
    return (changesets - 10) / 5 + 15;
  } else if (changesets <= 500) {
    return (changesets - 50) / 10 + 23;
  } else if (changesets <= 3700) {
    return (changesets - 500) / 100 + 68;
  } else {
    return 100;
  }
};
