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
import { Link as RouterLink } from "react-router-dom";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { Link, Tag } from "@scm-manager/ui-types";
import { DateFromNow } from "@scm-manager/ui-components";
import { useKeyboardIteratorTarget } from "@scm-manager/ui-shortcuts";
import { encodePart } from "../../sources/components/content/FileLink";
import { Menu } from "@scm-manager/ui-overlays";
import { CardList } from "@scm-manager/ui-layout";
import { Icon } from "@scm-manager/ui-buttons";

type Props = {
  tag: Tag;
  baseUrl: string;
  onDelete: (tag: Tag) => void;
  // deleting: boolean;
};

const TagRow: FC<Props> = ({ tag, baseUrl, onDelete }) => {
  const [t] = useTranslation("repos");
  const ref = useKeyboardIteratorTarget();

  const to = `${baseUrl}/${encodePart(tag.name)}/info`;
  return (
    <CardList.Card
      key={tag.name}
      action={
        (tag?._links?.delete as Link)?.href ? (
          <Menu>
            <Menu.Button onSelect={() => onDelete(tag)}>
              <Icon>trash</Icon>
              {t("tag.delete.button")}
            </Menu.Button>
          </Menu>
        ) : undefined
      }
    >
      <CardList.Card.Row>
        <CardList.Card.Title>
          <RouterLink ref={ref} to={to}>
            {tag.name}
          </RouterLink>
        </CardList.Card.Title>
      </CardList.Card.Row>
      <CardList.Card.Row className={classNames("is-size-7", "has-text-secondary")}>
        {t("tags.overview.created")} <DateFromNow className="is-relative" date={tag.date} />
      </CardList.Card.Row>
    </CardList.Card>
  );
};

export default TagRow;
