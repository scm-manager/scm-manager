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
import { Me } from "@scm-manager/ui-types";
import {
  AvatarImage,
  AvatarWrapper,
  MailLink,
  createAttributesForTesting,
  replaceSpacesInTestId
} from "@scm-manager/ui-components";

type Props = WithTranslation & {
  me: Me;
};

class ProfileInfo extends React.Component<Props> {
  render() {
    const { me, t } = this.props;
    return (
      <div className="media">
        <AvatarWrapper>
          <figure className="media-left">
            <p className="image is-64x64">
              <AvatarImage person={me} />
            </p>
          </figure>
        </AvatarWrapper>
        <div className="media-content">
          <table className="table content">
            <tbody>
              <tr>
                <th>{t("profile.username")}</th>
                <td {...createAttributesForTesting(replaceSpacesInTestId(me.name))}>{me.name}</td>
              </tr>
              <tr>
                <th>{t("profile.displayName")}</th>
                <td {...createAttributesForTesting(replaceSpacesInTestId(me.displayName))}>{me.displayName}</td>
              </tr>
              <tr>
                <th>{t("profile.mail")}</th>
                <td>
                  <MailLink address={me.mail} />
                </td>
              </tr>
              {this.renderGroups()}
            </tbody>
          </table>
        </div>
      </div>
    );
  }

  renderGroups() {
    const { me, t } = this.props;

    let groups = null;
    if (me.groups.length > 0) {
      groups = (
        <tr>
          <th>{t("profile.groups")}</th>
          <td className="is-paddingless">
            <ul>
              {me.groups.map(group => {
                return <li>{group}</li>;
              })}
            </ul>
          </td>
        </tr>
      );
    }
    return groups;
  }
}

export default withTranslation("commons")(ProfileInfo);
