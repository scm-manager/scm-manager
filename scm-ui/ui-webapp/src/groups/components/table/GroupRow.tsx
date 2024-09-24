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
import { Group } from "@scm-manager/ui-types";
import { Icon } from "@scm-manager/ui-components";
import classNames from "classnames";
import { useKeyboardIteratorTarget } from "@scm-manager/ui-shortcuts";

type Props = {
  group: Group;
};

const GroupRow: FC<Props> = ({ group }) => {
  const ref = useKeyboardIteratorTarget();
  const [t] = useTranslation("groups");
  const to = `/group/${group.name}`;
  const iconType = group.external ? (
    <Icon title={t("group.external")} name="globe-americas" />
  ) : (
    <Icon title={t("group.internal")} name="home" />
  );

  return (
    <tr>
      <td className="is-word-break">
        {iconType}{" "}
        {
          <Link ref={ref} to={to}>
            {group.name}
          </Link>
        }
      </td>
      <td className={classNames("is-hidden-mobile", "is-word-break")}>{group.description}</td>
    </tr>
  );
};

export default GroupRow;
