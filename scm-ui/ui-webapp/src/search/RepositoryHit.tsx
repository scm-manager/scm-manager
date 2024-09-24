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
import { Link } from "react-router-dom";
import {
  DateFromNow,
  HitProps,
  RepositoryAvatar,
  TextHitField,
  useDateHitFieldValue,
  useStringHitFieldValue,
} from "@scm-manager/ui-components";
import { CardList } from "@scm-manager/ui-layout";
import classNames from "classnames";
import styled from "styled-components";
import { HighlightedHitField, ValueHitField } from "@scm-manager/ui-types";
import { useKeyboardIteratorTarget } from "@scm-manager/ui-shortcuts";

const StyledLink = styled(Link)`
  gap: 0.5rem;
`;

const RepositoryHit: FC<HitProps> = ({ hit }) => {
  const ref = useKeyboardIteratorTarget();

  const namespace = useStringHitFieldValue(hit, "namespace");
  const name = useStringHitFieldValue(hit, "name");
  const type = useStringHitFieldValue(hit, "type");
  const lastModified = useDateHitFieldValue(hit, "lastModified");
  const creationDate = useDateHitFieldValue(hit, "creationDate");
  const date = lastModified || creationDate;
  const description = hit.fields["description"];
  const title = `${namespace}/${name}`;

  // the embedded repository is only a subset of the repository (RepositoryCoordinates),
  // so we should use the fields to get more information
  const repository = hit._embedded?.repository;
  if (!namespace || !name || !type || !repository) {
    return null;
  }

  return (
    <CardList.Card key={title}>
      <CardList.Card.Row>
        <CardList.Card.Title>
          <StyledLink
            ref={ref}
            to={`/repo/${namespace}/${name}`}
            className={classNames("is-flex", "is-justify-content-flex-start", "is-align-items-center")}
          >
            <RepositoryAvatar repository={repository} size={16} /> {title}
          </StyledLink>
        </CardList.Card.Title>
      </CardList.Card.Row>
      {description && ((description as ValueHitField).value || (description as HighlightedHitField).fragments) ? (
        <CardList.Card.Row className="is-size-7 has-text-secondary">
          <TextHitField hit={hit} field="description" />
        </CardList.Card.Row>
      ) : null}
      <CardList.Card.Row className="is-size-7 has-text-secondary">
        <DateFromNow date={date} />
      </CardList.Card.Row>
    </CardList.Card>
  );
};

export default RepositoryHit;
