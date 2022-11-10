/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
