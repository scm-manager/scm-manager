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

import { validation } from "@scm-manager/ui-components";
import { isNameValid as isUserNameValid } from "../../../users/components/userValidation";

// eslint-disable-next-line
const nameRegex = /(?!^\.\.$)(?!^\.$)(?!.*[.]git$)(?!.*[\\\[\]])^[A-Za-z0-9\.][A-Za-z0-9\.\-_]*$/;
const namespaceExceptionsRegex = /^(([0-9]{1,3})|(create)|(import))$/;

export const isNamespaceValid = (name: string) => {
  return isUserNameValid(name) && !namespaceExceptionsRegex.test(name);
};

export const isNameValid = (name: string) => {
  return nameRegex.test(name);
};

export function isContactValid(mail: string) {
  return "" === mail || validation.isMailValid(mail);
}
