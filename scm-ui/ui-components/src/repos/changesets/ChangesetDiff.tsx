/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React from "react";
import { Changeset, HalRepresentation, Link } from "@scm-manager/ui-types";
import LoadingDiff from "../LoadingDiff";
import Notification from "../../Notification";
import { WithTranslation, withTranslation } from "react-i18next";
import { FileControlFactory } from "../DiffTypes";

type Props = WithTranslation & {
  changeset: Changeset;
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
    const { changeset, fileControlFactory, t } = this.props;

    if (!isDiffSupported(changeset)) {
      return <Notification type="danger">{t("changeset.diffNotSupported")}</Notification>;
    } else {
      const url = createUrl(changeset);
      return (
          <LoadingDiff
            url={url}
            sideBySide={false}
            fileControlFactory={fileControlFactory}
            stickyHeader={true}
          />
      );
    }
  }
}

export default withTranslation("repos")(ChangesetDiff);
