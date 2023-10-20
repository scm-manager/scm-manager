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
