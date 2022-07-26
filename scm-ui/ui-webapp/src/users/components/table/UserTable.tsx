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
import { User } from "@scm-manager/ui-types";
import { Notification } from "@scm-manager/ui-components";
import UserRow from "./UserRow";

type Props = {
  users?: User[];
};

const UserTable: FC<Props> = ({ users }) => {
  const [t] = useTranslation("users");

  if (!users || users.length === 0) {
    return <Notification type="info">{t("users.noUsers")}</Notification>;
  }

  return (
    <table className="card-table table is-hoverable is-fullwidth">
      <thead>
        <tr>
          <th>{t("user.name")}</th>
          <th className="is-hidden-mobile">{t("user.displayName")}</th>
          <th className="is-hidden-mobile">{t("user.mail")}</th>
        </tr>
      </thead>
      <tbody>
        {users.map((user, index) => {
          return <UserRow key={index} user={user} />;
        })}
      </tbody>
    </table>
  );
};

export default UserTable;
