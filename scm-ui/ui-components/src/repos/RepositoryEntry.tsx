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
import { DateFromNow } from "@scm-manager/ui-components";
import RepositoryAvatar from "./RepositoryAvatar";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import GroupEntry from "../layout/GroupEntry";
import RepositoryFlags from "./RepositoryFlags";

type DateProp = Date | string;

type Props = {
  repository: Repository;
  // @VisibleForTesting
  // the baseDate is only to avoid failing snapshot tests
  baseDate?: DateProp;
};

const RepositoryEntry: FC<Props> = ({ repository, baseDate }) => {
  const createLink = () => {
    return `/repo/${repository.namespace}/${repository.name}`;
  };

  const createActions = () => {
    return (
      <small className="level-item">
        <DateFromNow baseDate={baseDate} date={repository.lastModified || repository.creationDate} />
      </small>
    );
  };

  const repositoryLink = createLink();
  const actions = createActions();
  const name = (
    <>
      <ExtensionPoint name="repository.card.beforeTitle" props={{ repository }} />
      <strong>{repository.name}</strong>{" "}
    </>
  );

  return (
    <>
      <GroupEntry
        avatar={<RepositoryAvatar repository={repository} />}
        name={name}
        description={repository.description}
        link={repositoryLink}
        contentLeft={<RepositoryFlags repository={repository} />}
        contentRight={actions}
      />
    </>
  );
};

export default RepositoryEntry;
