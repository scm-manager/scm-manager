import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Me } from "@scm-manager/ui-types";
import { AvatarImage, AvatarWrapper, MailLink } from "@scm-manager/ui-components";

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
                <td>{me.name}</td>
              </tr>
              <tr>
                <th>{t("profile.displayName")}</th>
                <td>{me.displayName}</td>
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
