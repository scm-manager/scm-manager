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

import "./enzyme";

import { createMount, createShallow } from "enzyme-context";
import { routerContext } from "enzyme-context-react-router-4";

const plugins = {
  history: routerContext(),
};

// TODO Enzyme is not going to be supported in React 19+.: https://testing-library.com/docs/react-testing-library/migrate-from-enzyme/
export const mount = createMount(plugins);
export const shallow = createShallow(plugins);
