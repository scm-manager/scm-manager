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
import React, { FC } from "react";
import classNames from "classnames";
import styled from "styled-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { Changeset, File, Repository } from "@scm-manager/ui-types";
import ChangesetButtonGroup from "./ChangesetButtonGroup";
import SingleChangeset from "./SingleChangeset";

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
  return (
    <Wrapper>
      <div className={classNames("columns", "is-gapless", "is-mobile")}>
        <div className={classNames("column", "is-three-fifths")}>
          <SingleChangeset repository={repository} changeset={changeset} />
        </div>
        <div className={classNames("column", "is-flex", "is-justify-content-flex-end", "is-align-items-center")}>
          <ChangesetButtonGroup repository={repository} changeset={changeset} file={file} />
          <ExtensionPoint
            name="changeset.right"
            props={{
              repository,
              changeset
            }}
            renderAll={true}
          />
        </div>
      </div>
    </Wrapper>
  );
};

export default ChangesetRow;
