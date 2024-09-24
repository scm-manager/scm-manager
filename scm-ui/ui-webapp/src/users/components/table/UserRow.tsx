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
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import { User } from "@scm-manager/ui-types";
import { createAttributesForTesting, Icon } from "@scm-manager/ui-components";
import classNames from "classnames";
import { useKeyboardIteratorTarget } from "@scm-manager/ui-shortcuts";

type Props = {
  user: User;
};

const UserRowLink = React.forwardRef<HTMLAnchorElement, { to: string; children: string }>(({ children, to }, ref) => (
  <Link ref={ref} to={to} {...createAttributesForTesting(children)}>
    {children}
  </Link>
));
const UserRow: FC<Props> = ({ user }) => {
  const ref = useKeyboardIteratorTarget();
  const [t] = useTranslation("users");
  const to = `/user/${user.name}`;
  const iconType = user.active ? (
    <Icon title={t("user.active")} name="user" />
  ) : (
    <Icon title={t("user.inactive")} name="user-slash" />
  );

  return (
    <tr className={user.active ? "border-is-green" : "border-is-yellow"}>
      <td className="is-word-break">
        {iconType}{" "}
        <UserRowLink ref={ref} to={to}>
          {user.name}
        </UserRowLink>
      </td>
      <td className={classNames("is-hidden-mobile", "is-word-break")}>
        <UserRowLink to={to}>{user.displayName}</UserRowLink>
      </td>
      <td className={classNames("is-hidden-mobile", "is-word-break")}>
        {user.mail ? <a href={`mailto:${user.mail}`}>{user.mail}</a> : null}
      </td>
    </tr>
  );
};

export default UserRow;
