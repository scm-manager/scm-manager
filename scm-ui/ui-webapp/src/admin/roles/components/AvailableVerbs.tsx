import React from "react";
import { translate } from "react-i18next";
import { RepositoryRole } from "@scm-manager/ui-types";

type Props = {
  role: RepositoryRole;

  // context props
  t: (p: string) => string;
};

class AvailableVerbs extends React.Component<Props> {
  render() {
    const { role, t } = this.props;

    let verbs = null;
    if (role.verbs.length > 0) {
      verbs = (
        <tr>
          <td className="is-paddingless">
            <ul>
              {role.verbs.map(verb => {
                return <li>{t("verbs.repository." + verb + ".displayName")}</li>;
              })}
            </ul>
          </td>
        </tr>
      );
    }
    return verbs;
  }
}

export default translate("plugins")(AvailableVerbs);
