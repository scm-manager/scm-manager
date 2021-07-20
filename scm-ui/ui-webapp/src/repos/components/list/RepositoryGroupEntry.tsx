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
import { Link } from "react-router-dom";
import { Icon, RepositoryEntry } from "@scm-manager/ui-components";
import { RepositoryGroup } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import GroupEntries from "../../../../../ui-components/src/layout/GroupEntries";

type Props = {
  group: RepositoryGroup;
};

const RepositoryGroupEntry: FC<Props> = ({ group }) => {
  const [t] = useTranslation("namespaces");

  const settingsLink = group.namespace?._links?.permissions && (
    <Link to={`/namespace/${group.name}/settings`}>
      <Icon color="is-link" name="cog" title={t("repositoryOverview.settings.tooltip")} className={"is-size-5"} />
    </Link>
  );
  const namespaceHeader = (
    <>
      <Link to={`/repos/${group.name}/`} className={"has-text-dark"}>
        {group.name}
      </Link>{" "}
      {settingsLink}
    </>
  );
  const entries = group.repositories.map((repository, index) => {
    return <RepositoryEntry repository={repository} key={index} />;
  });
  return <GroupEntries name={namespaceHeader} elements={entries} />;
};

export default RepositoryGroupEntry;
