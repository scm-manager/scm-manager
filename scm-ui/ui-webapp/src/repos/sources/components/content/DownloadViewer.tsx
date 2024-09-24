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
import { WithTranslation, withTranslation } from "react-i18next";
import { File, Link, Repository } from "@scm-manager/ui-types";
import { DownloadButton } from "@scm-manager/ui-components";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";

type Props = WithTranslation & {
  repository: Repository;
  file: File;
};

class DownloadViewer extends React.Component<Props> {
  render() {
    const { t, repository, file } = this.props;

    return (
      <div className="has-text-centered">
        <ExtensionPoint<extensionPoints.RepositorySourcesContentDownloadButton>
          name="repos.sources.content.downloadButton"
          props={{ repository, file }}
        >
          <DownloadButton url={(file._links.self as Link).href} displayName={t("sources.content.downloadButton")} />
        </ExtensionPoint>
      </div>
    );
  }
}

export default withTranslation("repos")(DownloadViewer);
