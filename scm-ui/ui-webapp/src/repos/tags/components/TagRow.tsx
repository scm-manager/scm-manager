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
import { Link } from "react-router-dom";
import { Tag } from "@scm-manager/ui-types";
import styled from "styled-components";
import { DateFromNow } from "@scm-manager/ui-components";

type Props = {
  tag: Tag;
  baseUrl: string;
};

const Created = styled.span`
  margin-left: 1rem;
  font-size: 0.8rem;
`;

const TagRow: FC<Props> = ({ tag, baseUrl }) => {
  const [t] = useTranslation("repos");

  const to = `${baseUrl}/${encodeURIComponent(tag.name)}/info`;
  return (
    <tr>
      <td>
        <Link to={to} title={tag.name}>
          {tag.name}
          <Created className="has-text-grey is-ellipsis-overflow">
            {t("tags.overview.created")} <DateFromNow date={tag.date} />
          </Created>
        </Link>
      </td>
    </tr>
  );
};

export default TagRow;
