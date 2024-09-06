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

// https://bulma.io/documentation/overview/responsiveness/

export type Device = {
  width: number;
};

export const devices = {
  mobile: {
    width: 768,
  },
  tablet: {
    width: 769,
  },
  desktop: {
    width: 1024,
  },
  widescreen: {
    width: 1216,
  },
  fullhd: {
    width: 1408,
  },
};

export type DeviceType = keyof typeof devices;
