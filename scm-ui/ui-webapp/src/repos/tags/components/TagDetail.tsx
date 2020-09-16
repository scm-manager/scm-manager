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
import { useTranslation } from "react-i18next";
import { Repository, Tag } from "@scm-manager/ui-types";
import { DateFromNow } from "@scm-manager/ui-components";
import styled from "styled-components";
import TagButtonGroup from "./TagButtonGroup";

type Props = {
  repository: Repository;
  tag?: Tag;
};

const Created = styled.div`
  margin-top: 0.5rem;
`;

const Date = styled(DateFromNow)`
font-size: 1rem;
`;

const TagDetail: FC<Props> = ({ tag, repository }) => {
  const [t] = useTranslation("repos");

  if (!tag) {
    return null;
  }

  return (
    <div className="media">
      <div className="media-content subtitle">
        <strong>{t("tag.name") + ":"}</strong> {tag?.name}
        <Created className="is-ellipsis-overflow">
          <strong>{t("tags.overview.created") + ":"}</strong> <Date date={tag.date} className="has-text-grey" />
        </Created>
      </div>
      <div className="media-right">
        <TagButtonGroup repository={repository} tag={tag} />
      </div>
    </div>
  );
};

export default TagDetail;
