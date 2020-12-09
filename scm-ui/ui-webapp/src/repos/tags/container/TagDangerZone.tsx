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
import { Repository, Tag } from "@scm-manager/ui-types";
import { Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { DangerZoneContainer } from "../../containers/RepositoryDangerZone";
import DeleteTag from "./DeleteTag";

type Props = {
  repository: Repository;
  tag: Tag;
};

const TagDangerZone: FC<Props> = ({ repository, tag }) => {
  const [t] = useTranslation("repos");

  const dangerZone = [];

  if (tag?._links?.delete) {
    dangerZone.push(<DeleteTag repository={repository} tag={tag} key={dangerZone.length} />);
  }

  if (dangerZone.length === 0) {
    return null;
  }

  return (
    <>
      <hr />
      <Subtitle subtitle={t("tag.dangerZone")} />
      <DangerZoneContainer>{dangerZone}</DangerZoneContainer>
    </>
  );
};

export default TagDangerZone;
