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
import { User } from "@scm-manager/ui-types";
import UserRow from "./UserRow";
import { KeyboardIterator } from "@scm-manager/ui-shortcuts";

type Props = {
  users: User[];
};

const UserTable: FC<Props> = ({ users }) => {
  const [t] = useTranslation("users");

  return (
    <KeyboardIterator>
      <table className="card-table table is-hoverable is-fullwidth">
        <thead>
          <tr>
            <th>{t("user.name")}</th>
            <th>{t("user.displayName")}</th>
            <th>{t("user.mail")}</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user, index) => {
            return <UserRow key={index} user={user} />;
          })}
        </tbody>
      </table>
    </KeyboardIterator>
  );
};

export default UserTable;
