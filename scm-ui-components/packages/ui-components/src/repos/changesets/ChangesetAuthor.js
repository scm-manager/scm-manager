//@flow
import React from "react";
import type { Changeset } from "@scm-manager/ui-types";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { translate } from "react-i18next";

type Props = {
  changeset: Changeset,

  // context props
  t: string => string
};

class ChangesetAuthor extends React.Component<Props> {
  render() {
    const { changeset } = this.props;
    if (!changeset.author) {
      return null;
    }

    const { name, mail } = changeset.author;
    if (mail) {
      return this.withExtensionPoint(this.renderWithMail(name, mail));
    }
    return this.withExtensionPoint(<>{name}</>);
  }

  renderWithMail(name: string, mail: string) {
    const { t } = this.props;
    return (
      <a
        href={"mailto:" + mail}
        title={t("changeset.author.mailto") + " " + mail}
      >
        {name}
      </a>
    );
  }

  withExtensionPoint(child: any) {
    const { t } = this.props;
    return (
      <>
        {t("changeset.author.prefix")} {child}
        <ExtensionPoint
          name="changesets.author.suffix"
          props={{ changeset: this.props.changeset }}
          renderAll={true}
        />
      </>
    );
  }
}

export default translate("repos")(ChangesetAuthor);
