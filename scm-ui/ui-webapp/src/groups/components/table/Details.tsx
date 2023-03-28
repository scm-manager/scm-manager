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
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Group } from "@scm-manager/ui-types";
import { Checkbox, DateFromNow, InfoTable } from "@scm-manager/ui-components";
import GroupMember from "./GroupMember";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";

type Props = WithTranslation & {
  group: Group;
};

class Details extends React.Component<Props> {
  render() {
    const { group, t } = this.props;
    return (
      <InfoTable className="content">
        <tbody>
          <tr>
            <th>{t("group.name")}</th>
            <td>{group.name}</td>
          </tr>
          <tr>
            <th>{t("group.description")}</th>
            <td>{group.description}</td>
          </tr>
          <tr>
            <th>{t("group.external")}</th>
            <td>
              <Checkbox checked={group.external} readOnly={true} />
            </td>
          </tr>
          <tr>
            <th>{t("group.type")}</th>
            <td>{group.type}</td>
          </tr>
          <tr>
            <th>{t("group.creationDate")}</th>
            <td>
              <DateFromNow date={group.creationDate} />
            </td>
          </tr>
          <tr>
            <th>{t("group.lastModified")}</th>
            <td>
              <DateFromNow date={group.lastModified} />
            </td>
          </tr>
          {this.renderMembers()}
          <ExtensionPoint<extensionPoints.GroupInformationTableBottom> name="group.information.table.bottom" props={{group}} renderAll={true} />
        </tbody>
      </InfoTable>
    );
  }

  renderMembers() {
    const { group, t } = this.props;

    let member = null;
    if (group.members.length > 0) {
      member = (
        <tr>
          <th>{t("group.members")}</th>
          <td className="p-0">
            <ul className="ml-4">
              {group._embedded.members.map((member, index) => {
                return <GroupMember key={index} member={member} />;
              })}
            </ul>
          </td>
        </tr>
      );
    }
    return member;
  }
}

export default withTranslation("groups")(Details);
