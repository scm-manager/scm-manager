import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import { Group } from "@scm-manager/ui-types";
import { Icon } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  group: Group;
};

class GroupRow extends React.Component<Props> {
  renderLink(to: string, label: string) {
    return <Link to={to}>{label}</Link>;
  }

  render() {
    const { group, t } = this.props;
    const to = `/group/${group.name}`;
    const iconType = group.external ? (
      <Icon title={t("group.external")} name="globe-americas" />
    ) : (
      <Icon title={t("group.internal")} name="home" />
    );

    return (
      <tr>
        <td>
          {iconType} {this.renderLink(to, group.name)}
        </td>
        <td className="is-hidden-mobile">{group.description}</td>
      </tr>
    );
  }
}

export default withTranslation("groups")(GroupRow);
