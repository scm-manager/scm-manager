import React from "react";
import { Changeset, Link, Collection } from "@scm-manager/ui-types";
import LoadingDiff from "../LoadingDiff";
import Notification from "../../Notification";
import { WithTranslation, withTranslation } from "react-i18next";

type Props = WithTranslation & {
  changeset: Changeset;
  defaultCollapse?: boolean;
};

export const isDiffSupported = (changeset: Collection) => {
  return !!changeset._links.diff || !!changeset._links.diffParsed;
};

export const createUrl = (changeset: Collection) => {
  if (changeset._links.diffParsed) {
    return (changeset._links.diffParsed as Link).href;
  } else if (changeset._links.diff) {
    return (changeset._links.diff as Link).href + "?format=GIT";
  }
  throw new Error("diff link is missing");
};

class ChangesetDiff extends React.Component<Props> {
  render() {
    const { changeset, defaultCollapse, t } = this.props;
    if (!isDiffSupported(changeset)) {
      return <Notification type="danger">{t("changeset.diffNotSupported")}</Notification>;
    } else {
      const url = createUrl(changeset);
      return <LoadingDiff url={url} defaultCollapse={defaultCollapse} sideBySide={false} />;
    }
  }
}

export default withTranslation("repos")(ChangesetDiff);
