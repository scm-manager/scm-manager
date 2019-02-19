//@flow
import React from "react";
import { translate } from "react-i18next";
import type { Repository } from "@scm-manager/ui-types";

type Props = {
  url: string,
  repository: Repository,

  // context props
  t: (string) => string
};

class CloneInformation extends React.Component<Props> {

  render() {
    const { url, repository, t } = this.props;

    return (
      <div>
        <h4>{t("scm-git-plugin.information.clone")}</h4>
        <pre>
          <code>git clone {url}</code>
        </pre>
        <h4>{t("scm-git-plugin.information.create")}</h4>
        <pre>
          <code>
            git init {repository.name}
            <br />
            echo "# {repository.name}" > README.md
            <br />
            git add README.md
            <br />
            git commit -m "added readme"
            <br />
            git remote add origin {url}
            <br />
            git push -u origin master
            <br />
          </code>
        </pre>
        <h4>{t("scm-git-plugin.information.replace")}</h4>
        <pre>
          <code>
            git remote add origin {url}
            <br />
            git push -u origin master
            <br />
          </code>
        </pre>
      </div>
    );
  }

}

export default translate("plugins")(CloneInformation);
