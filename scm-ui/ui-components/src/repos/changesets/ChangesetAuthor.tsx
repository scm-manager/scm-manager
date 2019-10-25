import React from "react";
import { Changeset } from "@scm-manager/ui-types";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { WithTranslation, withTranslation } from "react-i18next";

type Props = WithTranslation & {
  changeset: Changeset;
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
      <a href={"mailto:" + mail} title={t("changeset.author.mailto") + " " + mail}>
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
          props={{
            changeset: this.props.changeset
          }}
          renderAll={true}
        />
      </>
    );
  }
}

export default withTranslation("repos")(ChangesetAuthor);
