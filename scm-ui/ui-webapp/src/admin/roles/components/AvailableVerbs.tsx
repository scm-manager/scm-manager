import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { RepositoryRole } from "@scm-manager/ui-types";

type Props = WithTranslation & {
  role: RepositoryRole;
};

class AvailableVerbs extends React.Component<Props> {
  render() {
    const { role, t } = this.props;

    let verbs = null;
    if (role.verbs.length > 0) {
      verbs = (
        <td className="is-paddingless">
          <ul>
            {role.verbs.map((verb, key) => {
              return <li key={key}>{t("verbs.repository." + verb + ".displayName")}</li>;
            })}
          </ul>
        </td>
      );
    }
    return verbs;
  }
}

export default withTranslation("plugins")(AvailableVerbs);
