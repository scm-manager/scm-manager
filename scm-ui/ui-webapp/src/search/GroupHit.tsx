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
import {
  HitProps,
  DateFromNow,
  TextHitField,
  useDateHitFieldValue,
  useStringHitFieldValue,
} from "@scm-manager/ui-components";
import { Link } from "react-router-dom";
import { CardList } from "@scm-manager/ui-layout";
import { useKeyboardIteratorTarget } from "@scm-manager/ui-shortcuts";
import { HighlightedHitField, ValueHitField } from "@scm-manager/ui-types";

const GroupHit: FC<HitProps> = ({ hit }) => {
  const ref = useKeyboardIteratorTarget();
  const name = useStringHitFieldValue(hit, "name");
  const lastModified = useDateHitFieldValue(hit, "lastModified");
  const creationDate = useDateHitFieldValue(hit, "creationDate");
  const date = lastModified || creationDate;
  const description = hit.fields["description"];

  return (
    <CardList.Card key={name}>
      <CardList.Card.Row>
        <CardList.Card.Title>
          <Link ref={ref} to={`/group/${name}`}>
            <TextHitField hit={hit} field="name" />
          </Link>
        </CardList.Card.Title>
      </CardList.Card.Row>
      {((description as ValueHitField).value || (description as HighlightedHitField).fragments) && (
        <CardList.Card.Row className="is-size-7 has-text-secondary">
          <TextHitField hit={hit} field="description" />
        </CardList.Card.Row>
      )}
      <CardList.Card.Row className="is-size-7 has-text-secondary">
        <DateFromNow date={date} />
      </CardList.Card.Row>
    </CardList.Card>
  );
};

export default GroupHit;
