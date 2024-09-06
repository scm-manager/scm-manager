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

import React, { FC } from "react";
import classNames from "classnames";
import styled from "styled-components";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { Changeset, File, Repository } from "@scm-manager/ui-types";
import ChangesetButtonGroup from "./ChangesetButtonGroup";
import SingleChangeset from "./SingleChangeset";
import { useKeyboardIteratorTarget } from "@scm-manager/ui-shortcuts";

type Props = {
  repository: Repository;
  changeset: Changeset;
  file?: File;
};

const Wrapper = styled.div`
  // & references parent rule
  // have a look at https://cssinjs.org/jss-plugin-nested?v=v10.0.0-alpha.9
  & + & {
    margin-top: 1rem;
    padding-top: 1rem;
    border-top: 1px solid rgba(219, 219, 219, 0.5);
  }
`;

const ChangesetRow: FC<Props> = ({ repository, changeset, file }) => {
  const ref = useKeyboardIteratorTarget();
  return (
    <Wrapper>
      <div className={classNames("columns", "is-variable", "is-1-mobile", "is-0-tablet")}>
        <div className={classNames("column", "is-three-fifths", "is-full-mobile")}>
          <SingleChangeset repository={repository} changeset={changeset} />
        </div>
        <div className={classNames("column", "is-flex", "is-justify-content-flex-end", "is-align-items-center")}>
          <ChangesetButtonGroup ref={ref} repository={repository} changeset={changeset} file={file} />
          <ExtensionPoint<extensionPoints.ChangesetRight>
            name="changeset.right"
            props={{
              repository,
              changeset,
            }}
            renderAll={true}
          />
        </div>
      </div>
    </Wrapper>
  );
};

export default ChangesetRow;
