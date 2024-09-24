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
import BranchDetail from "./BranchDetail";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { Branch, Repository } from "@scm-manager/ui-types";
import BranchDangerZone from "../containers/BranchDangerZone";

type Props = {
  repository: Repository;
  branch: Branch;
};

class BranchView extends React.Component<Props> {
  render() {
    const { repository, branch } = this.props;
    return (
      <>
        <BranchDetail repository={repository} branch={branch} />
        <hr />
        <div className="content">
          <ExtensionPoint<extensionPoints.ReposBranchDetailsInformation>
            name="repos.branch-details.information"
            renderAll={true}
            props={{
              repository,
              branch
            }}
          />
        </div>
        <BranchDangerZone repository={repository} branch={branch} />
      </>
    );
  }
}

export default BranchView;
