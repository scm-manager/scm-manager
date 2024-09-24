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
  useDateHitFieldValue,
  useStringHitFieldValue,
  TextHitField,
  HitProps,
} from "@scm-manager/ui-components";
import { CardList } from "@scm-manager/ui-layout";
import { useKeyboardIteratorTarget } from "@scm-manager/ui-shortcuts";

const UserHit: FC<HitProps> = ({ hit }) => {
  const ref = useKeyboardIteratorTarget();
  const name = useStringHitFieldValue(hit, "name");
  const mail = useStringHitFieldValue(hit, "mail");
  const lastModified = useDateHitFieldValue(hit, "lastModified");
  const creationDate = useDateHitFieldValue(hit, "creationDate");
  const date = lastModified || creationDate;

  return (
    <CardList.Card key={name}>
      <CardList.Card.Row>
        <CardList.Card.Title>
          <Link ref={ref} to={`/user/${name}`}>
            <TextHitField hit={hit} field="name" />
          </Link>
        </CardList.Card.Title>
      </CardList.Card.Row>
      <CardList.Card.Row className="is-size-7 has-text-secondary">
        <TextHitField hit={hit} field="displayName" />
        {mail && (
          <>
            {" "}
            &lt;
            <TextHitField hit={hit} field="mail" />
            &gt;
          </>
        )}
      </CardList.Card.Row>
      <CardList.Card.Row className="is-size-7 has-text-secondary">
        <DateFromNow date={date} />
      </CardList.Card.Row>
    </CardList.Card>
  );
};

export default UserHit;
