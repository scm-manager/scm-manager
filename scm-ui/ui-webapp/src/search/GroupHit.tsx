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
