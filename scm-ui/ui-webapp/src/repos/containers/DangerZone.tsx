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
import { Repository } from "@scm-manager/ui-types";
import RenameRepository from "./RenameRepository";
import DeleteRepo from "./DeleteRepo";
import styled from "styled-components";
import { Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  repository: Repository;
};

const DangerZoneContainer = styled.div`
  padding: 1rem;
  border: 1px solid #ff6a88;
  border-radius: 5px;
  > *:not(:last-child) {
    padding-bottom: 1.5rem;
    border-bottom: solid 2px whitesmoke;
  }
`;

const DangerZone: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("repos");

  const dangerZone = [];
  if (repository?._links?.rename) {
    dangerZone.push(<RenameRepository repository={repository} renameNamespace={false} />);
  }
  if (repository?._links?.renameWithNamespace) {
    dangerZone.push(<RenameRepository repository={repository} renameNamespace={true} />);
  }
  if (repository?._links?.delete) {
    dangerZone.push(<DeleteRepo repository={repository} />);
  }

  if (dangerZone.length === 0) {
    return null;
  }

  return (
    <>
      <hr />
      <Subtitle subtitle={t("repositoryForm.dangerZone")} />
      <DangerZoneContainer>{dangerZone.map(entry => entry)}</DangerZoneContainer>
    </>
  );
};

export default DangerZone;
