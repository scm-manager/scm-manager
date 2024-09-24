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
import { Link as RouterLink } from "react-router-dom";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { Link, Tag } from "@scm-manager/ui-types";
import { DateFromNow } from "@scm-manager/ui-components";
import { useKeyboardIteratorTarget } from "@scm-manager/ui-shortcuts";
import { encodePart } from "../../sources/components/content/FileLink";
import { Menu } from "@scm-manager/ui-overlays";
import { CardList } from "@scm-manager/ui-layout";
import { Icon } from "@scm-manager/ui-core";

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
