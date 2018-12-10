//@flow
import React from "react";
import type { Repository } from "@scm-manager/ui-types";
import { translate } from "react-i18next";

type Props = {
  repository: Repository,
  target: string,
  source: string,
  t: string => string
};

class GitMergeInformation extends React.Component<Props> {
  render() {
    const { source, target, t } = this.props;

    return (
      <div>
        <h4>{t("scm-git-plugin.information.merge.heading")}</h4>
        {t("scm-git-plugin.information.merge.checkout")}
        <pre>
          <code>git checkout {target}</code>
        </pre>
        {t("scm-git-plugin.information.merge.update")}
        <pre>
          <code>
            git pull
          </code>
        </pre>
        {t("scm-git-plugin.information.merge.merge")}
        <pre>
          <code>
            git merge {source}
          </code>
        </pre>
        {t("scm-git-plugin.information.merge.resolve")}
        <pre>
          <code>
            git add &lt;conflict file&gt;
          </code>
        </pre>
        {t("scm-git-plugin.information.merge.commit")}
        <pre>
          <code>
            git commit -m "Merge {source} into {target}"
          </code>
        </pre>
      </div>
    );
  }
}

export default translate("plugins")(GitMergeInformation);
