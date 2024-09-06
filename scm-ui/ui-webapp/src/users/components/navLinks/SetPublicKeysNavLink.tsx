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

import React, { FC } from "react";
import { Link, User, Me } from "@scm-manager/ui-types";
import { NavLink } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  user: User | Me;
  publicKeyUrl: string;
};

const SetPublicKeyNavLink: FC<Props> = ({ user, publicKeyUrl }) => {
  const [t] = useTranslation("users");

  if ((user?._links?.publicKeys as Link)?.href) {
    return <NavLink to={publicKeyUrl} label={t("singleUser.menu.setPublicKeyNavLink")} />;
  }
  return null;
};

export default SetPublicKeyNavLink;
