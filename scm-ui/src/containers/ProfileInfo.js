// @flow
import React from "react";
import type { Me } from "@scm-manager/ui-types";
import {
  MailLink,
  AvatarWrapper,
  AvatarImage
} from "@scm-manager/ui-components";
import { compose } from "redux";
import { translate } from "react-i18next";
import injectSheet from "react-jss";

type Props = {
  me: Me,

  // Context props
  classes: any,
  t: string => string
};

const styles = {
  spacing: {
    padding: "0 !important"
  }
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
    const { me, t, classes } = this.props;

    let groups = null;
    if (me.groups.length > 0) {
      groups = (
        <tr>
          <th>{t("profile.groups")}</th>
          <td className={classes.spacing}>
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

export default compose(
  injectSheet(styles),
  translate("commons")
)(ProfileInfo);
