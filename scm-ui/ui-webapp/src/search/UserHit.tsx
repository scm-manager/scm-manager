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
  useDateHitFieldValue,
  useStringHitFieldValue,
  TextHitField,
  Hit,
  HitProps,
} from "@scm-manager/ui-components";

const UserHit: FC<HitProps> = ({ hit }) => {
  const name = useStringHitFieldValue(hit, "name");
  const lastModified = useDateHitFieldValue(hit, "lastModified");
  const creationDate = useDateHitFieldValue(hit, "creationDate");
  const date = lastModified || creationDate;

  return (
    <Hit>
      <Hit.Content>
        <Link to={`/user/${name}`}>
          <Hit.Title>
            <TextHitField hit={hit} field="name" />
          </Hit.Title>
        </Link>
        <p>
          <TextHitField hit={hit} field="displayName" /> &lt;
          <TextHitField hit={hit} field="mail" />
          &gt;
        </p>
      </Hit.Content>
      <Hit.Right>
        <DateFromNow date={date} />
      </Hit.Right>
    </Hit>
  );
};

export default UserHit;
