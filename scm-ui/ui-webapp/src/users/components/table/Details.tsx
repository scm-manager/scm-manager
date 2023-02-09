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
import React, { FC, useState } from "react";
import { useTranslation, WithTranslation } from "react-i18next";
import { User } from "@scm-manager/ui-types";
import {
  Checkbox,
  createAttributesForTesting,
  DateFromNow,
  Help,
  InfoTable,
  MailLink
} from "@scm-manager/ui-components";
import { Icon } from "@scm-manager/ui-components";
import PermissionOverview from "../PermissionOverview";

type Props = WithTranslation & {
  user: User;
};

const Details: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  const [collapsed, setCollapsed] = useState(true);
  const toggleCollapse = () => setCollapsed(!collapsed);

  let permissionOverview;
  if (user._links.permissionOverview) {
    let icon = <Icon name="angle-right" color="inherit" alt={t("diff.showContent")} />;
    if (!collapsed) {
      icon = <Icon name="angle-down" color="inherit" alt={t("diff.hideContent")} />;
    }
    permissionOverview = (
      <div className="content">
        <h3 className="is-clickable" onClick={toggleCollapse}>
          {icon} {t("permissionOverview.title")} <Help message={t("permissionOverview.help")} />
        </h3>
        {!collapsed && <PermissionOverview user={user} />}
      </div>
    );
  }

  return (
    <>
      <InfoTable>
        <tbody>
          <tr>
            <th>{t("user.name")}</th>
            <td {...createAttributesForTesting(user.name)}>{user.name}</td>
          </tr>
          <tr>
            <th>{t("user.displayName")}</th>
            <td {...createAttributesForTesting(user.displayName)}>{user.displayName}</td>
          </tr>
          <tr>
            <th>{t("user.mail")}</th>
            <td>
              <MailLink address={user.mail} />
            </td>
          </tr>
          <tr>
            <th>{t("user.active")}</th>
            <td>
              <Checkbox checked={user.active} readOnly={true} />
            </td>
          </tr>
          <tr>
            <th>{t("user.externalFlag")}</th>
            <td>
              <Checkbox checked={user.external} readOnly={true} />
            </td>
          </tr>
          <tr>
            <th>{t("user.creationDate")}</th>
            <td>
              <DateFromNow date={user.creationDate} />
            </td>
          </tr>
          <tr>
            <th>{t("user.lastModified")}</th>
            <td>
              <DateFromNow date={user.lastModified} />
            </td>
          </tr>
        </tbody>
      </InfoTable>
      {permissionOverview}
    </>
  );
};

export default Details;
