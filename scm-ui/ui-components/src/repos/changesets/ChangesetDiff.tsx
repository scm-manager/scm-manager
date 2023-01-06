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
import { Changeset, HalRepresentation, Link } from "@scm-manager/ui-types";
import LoadingDiff from "../LoadingDiff";
import Notification from "../../Notification";
import { WithTranslation, withTranslation } from "react-i18next";
import { FileControlFactory } from "../DiffTypes";

type Props = WithTranslation & {
  changeset: Changeset;
  defaultCollapse?: boolean;
  fileControlFactory?: FileControlFactory;
};

export const isDiffSupported = (changeset: HalRepresentation) => {
  return !!changeset._links.diff || !!changeset._links.diffParsed;
};

export const createUrl = (changeset: HalRepresentation) => {
  if (changeset._links.diffParsed) {
    return (changeset._links.diffParsed as Link).href;
  } else if (changeset._links.diff) {
    return (changeset._links.diff as Link).href + "?format=GIT";
  }
  throw new Error("diff link is missing");
};

class ChangesetDiff extends React.Component<Props> {
  render() {
    const { changeset, fileControlFactory, defaultCollapse, t } = this.props;
    if (!isDiffSupported(changeset)) {
      return <Notification type="danger">{t("changeset.diffNotSupported")}</Notification>;
    } else {
      const url = createUrl(changeset);
      return (
        <LoadingDiff
          url={url}
          defaultCollapse={defaultCollapse}
          sideBySide={false}
          fileControlFactory={fileControlFactory}
          stickyHeader={true}
        />
      );
    }
  }
}

export default withTranslation("repos")(ChangesetDiff);
