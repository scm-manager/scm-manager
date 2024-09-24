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
import { Group } from "@scm-manager/ui-types";
import GroupRow from "./GroupRow";
import { KeyboardIterator } from "@scm-manager/ui-shortcuts";

type Props = {
  groups: Group[];
};

const GroupTable: FC<Props> = ({ groups }) => {
  const [t] = useTranslation("groups");

  return (
    <KeyboardIterator>
      <table className="card-table table is-hoverable is-fullwidth">
        <thead>
          <tr>
            <th>{t("group.name")}</th>
            <th className="is-hidden-mobile">{t("group.description")}</th>
          </tr>
        </thead>
        <tbody>
          {groups.map((group, index) => {
            return <GroupRow key={index} group={group} />;
          })}
        </tbody>
      </table>
    </KeyboardIterator>
  );
};

export default GroupTable;
