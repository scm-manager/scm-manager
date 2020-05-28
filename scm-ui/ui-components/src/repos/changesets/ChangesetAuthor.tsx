/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

  checkIfCoAuthorsExists() {
    console.log(this.props.changeset.trailerPersons.filter(p => p.trailerType === "Co-authored-by").length > 0);
    return this.props.changeset.trailerPersons.filter(p => p.trailerType === "Co-authored-by").length > 0;
  }

  renderCoAuthors() {
    const { t } = this.props;

    return <>{t("changeset.author.prefix")}</>;
  }

  withExtensionPoint(child: any) {
    const { t } = this.props;
    return (
      <>
        {t("changeset.author.prefix")} {child}
        {this.checkIfCoAuthorsExists() ? this.renderCoAuthors() : null}
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
