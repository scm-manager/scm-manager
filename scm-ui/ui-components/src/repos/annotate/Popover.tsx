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

import React, { FC, useState, useRef, useLayoutEffect, Dispatch } from "react";
import styled from "styled-components";
import { Link } from "react-router-dom";
import DateFromNow from "../../DateFromNow";
import { SingleContributor } from "../changesets";
import { DateInput } from "../../useDateFormatter";
import { Repository, AnnotatedLine } from "@scm-manager/ui-types";
import AuthorImage from "./AuthorImage";
import { Action } from "./actions";
import { useTranslation } from "react-i18next";

const PopoverContainer = styled.div`
  left: 2.25em;
`;

const PopoverHeading = styled.div`
  height: 1.5em;
`;

const PopoverDescription = styled.p`
  overflow-wrap: break-word;
`;

const shortRevision = (revision: string) => {
  if (revision.length > 7) {
    return revision.substring(0, 7);
  }
  return revision;
};

type PopoverProps = {
  annotation: AnnotatedLine;
  offsetTop?: number;
  repository: Repository;
  baseDate?: DateInput;
  dispatch: Dispatch<Action>;
};

const Popover: FC<PopoverProps> = ({ annotation, offsetTop, repository, baseDate, dispatch }) => {
  const [t] = useTranslation("repos");
  const [height, setHeight] = useState(125);
  const ref = useRef<HTMLDivElement>(null);
  useLayoutEffect(() => {
    if (ref.current) {
      setHeight(ref.current.clientHeight);
    }
  }, [ref]);

  const onMouseEnter = () => {
    dispatch({
      type: "enter-popover"
    });
  };

  const OnMouseLeave = () => {
    dispatch({
      type: "leave-popover"
    });
  };

  const top = (offsetTop || 0) - height - 5;
  return (
    <PopoverContainer
      ref={ref}
      onMouseEnter={onMouseEnter}
      onMouseLeave={OnMouseLeave}
      className="box popover"
      style={{ top: `${top}px` }}
    >
      <PopoverHeading className="is-clearfix">
        <span className="is-pulled-left">
          <AuthorImage person={annotation.author} />
          <SingleContributor person={annotation.author} displayTextOnly={true} />
        </span>
        <DateFromNow className="is-pulled-right" date={annotation.when} baseDate={baseDate} />
      </PopoverHeading>
      <hr className="my-2" />
      <p>
        {t("changeset.label") + " "}
        <Link to={`/repo/${repository.namespace}/${repository.name}/code/changeset/${annotation.revision}`}>
          {shortRevision(annotation.revision)}
        </Link>
      </p>
      <PopoverDescription className="content mt-2">{annotation.description}</PopoverDescription>
    </PopoverContainer>
  );
};

export default Popover;
