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
import { Me } from "@scm-manager/ui-types";
import {
  AvatarImage,
  AvatarWrapper,
  createAttributesForTesting,
  InfoTable,
  MailLink,
} from "@scm-manager/ui-components";
import { useDocumentTitle } from "@scm-manager/ui-core";

type Props = {
  me: Me;
};

const ProfileInfo: FC<Props> = ({ me }) => {
  const [t] = useTranslation("commons");
  useDocumentTitle(t("profile.subtitle"), me.displayName);
  const renderGroups = () => {
    let groups = null;
    if (me.groups.length > 0) {
      groups = (
        <tr>
          <th>{t("profile.groups")}</th>
          <td className="p-0">
            <ul>
              {me.groups.map((group) => {
                return <li>{group}</li>;
              })}
            </ul>
          </td>
        </tr>
      );
    }
    return groups;
  };

  return (
    <div className="media is-flex-wrap-wrap">
      <AvatarWrapper>
        <figure className="media-left">
          <p className="image is-64x64">
            <AvatarImage person={me} />
          </p>
        </figure>
      </AvatarWrapper>
      <div className="media-content">
        <InfoTable className="content">
          <tbody>
            <tr>
              <th>{t("profile.username")}</th>
              <td {...createAttributesForTesting(me.name)}>{me.name}</td>
            </tr>
            <tr>
              <th>{t("profile.displayName")}</th>
              <td {...createAttributesForTesting(me.displayName)}>{me.displayName}</td>
            </tr>
            <tr>
              <th>{t("profile.mail")}</th>
              <td>
                <MailLink address={me.mail} />
              </td>
            </tr>
            {renderGroups()}
          </tbody>
        </InfoTable>
      </div>
    </div>
  );
};

export default ProfileInfo;
