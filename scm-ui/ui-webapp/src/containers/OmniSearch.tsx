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
import React, { FC, KeyboardEvent, MouseEvent, useCallback, useState, useEffect } from "react";
import { Hit, Links, ValueField } from "@scm-manager/ui-types";
import styled from "styled-components";
import { useSearch } from "@scm-manager/ui-api";
import classNames from "classnames";
import { Link, useHistory } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ErrorNotification, Notification } from "@scm-manager/ui-components";

const Field = styled.div`
  margin-bottom: 0 !important;
`;

type Props = {
  links: Links;
};

const namespaceAndName = (hit: Hit) => {
  const namespace = (hit.fields["namespace"] as ValueField).value as string;
  const name = (hit.fields["name"] as ValueField).value as string;
  return `${namespace}/${name}`;
};

type HitsProps = {
  hits: Hit[];
  index: number;
  onClick: (hit: Hit) => void;
};

// TODO relabel old search from search to filter

const QuickSearchNotification: FC = ({ children }) => <div className="dropdown-content p-4">{children}</div>;

const EmptyHits = () => {
  const [t] = useTranslation("commons");
  return (
    <QuickSearchNotification>
      <Notification type="info">{t("search.quickSearch.noResults")}</Notification>
    </QuickSearchNotification>
  );
};

type ErrorProps = {
  error: Error;
};

const SearchErrorNotification: FC<ErrorProps> = ({ error }) => (
  <QuickSearchNotification>
    <ErrorNotification error={error} />
  </QuickSearchNotification>
);

const ResultHeading = styled.div`
  border-bottom: 1px solid lightgray;
  margin: 0 0.5rem;
  padding: 0.375rem 0.5rem;
`;

const Hits: FC<HitsProps> = ({ hits, index, onClick }) => {
  const id = useCallback(namespaceAndName, hits);
  const [t] = useTranslation("commons");

  if (hits.length === 0) {
    return <EmptyHits />;
  }

  return (
    <div className="dropdown-content">
      <ResultHeading className="dropdown-item">{t("search.quickSearch.resultHeading")}</ResultHeading>
      {hits.map((hit, idx) => (
        <div key={id(hit)} onClick={() => onClick(hit)}>
          <Link
            className={classNames("dropdown-item", "has-text-weight-medium", { "is-active": idx === index })}
            to={`/repo/${id(hit)}`}
          >
            {id(hit)}
          </Link>
        </div>
      ))}
    </div>
  );
};

const useKeyBoardNavigation = (onSelect: (hit: Hit) => void, hits?: Array<Hit>) => {
  const [index, setIndex] = useState(-1);
  const history = useHistory();
  useEffect(() => {
    setIndex(-1);
  }, [hits]);

  const onKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (!hits) {
      return;
    }
    // We use e.which, because ie 11 does not support e.code
    // https://caniuse.com/keyboardevent-code
    switch (e.which) {
      case 40: // e.code: ArrowDown
        setIndex((idx) => {
          if (idx + 1 < hits.length) {
            return idx + 1;
          }
          return idx;
        });
        break;
      case 38: // e.code: ArrowUp:
        setIndex((idx) => {
          if (idx > 0) {
            return idx - 1;
          }
          return idx;
        });
        break;
      case 13: // e.code: Enter:
        if (index >= 0) {
          const hit = hits[index];
          history.push(`/repo/${namespaceAndName(hit)}`);
          onSelect(hit);
        }
        break;
    }
  };

  return {
    onKeyDown,
    index,
  };
};

const useDebounce = (value: string, delay: number) => {
  const [debouncedValue, setDebouncedValue] = useState(value);
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);
    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);
  return debouncedValue;
};

const useShowResultsOnFocus = () => {
  const [showResults, setShowResults] = useState(false);

  useEffect(() => {
    const hideResults = () => {
      setShowResults(false);
    };
    window.addEventListener("click", hideResults);
    return () => window.removeEventListener("click", hideResults);
  }, []);

  return {
    showResults,
    onClick: (e: MouseEvent<HTMLInputElement>) => e.stopPropagation(),
    onFocus: () => setShowResults(true),
  };
};

const OmniSearch: FC = () => {
  const [query, setQuery] = useState("");
  const debouncedQuery = useDebounce(query, 250);
  const { showResults, ...handlers } = useShowResultsOnFocus();

  const onHitSelect = () => {
    setQuery("");
  };

  const { data, isLoading, error } = useSearch(debouncedQuery, { pageSize: 5 });
  const { onKeyDown, index } = useKeyBoardNavigation(onHitSelect, data?._embedded.hits);

  return (
    <Field className="navbar-item field">
      <div
        className={classNames("control", "has-icons-right", {
          "is-loading": isLoading,
        })}
      >
        <div className={classNames("dropdown", { "is-active": (!!data || error) && showResults })}>
          <div className="dropdown-trigger">
            <input
              className="input"
              type="text"
              placeholder="Search ..."
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={onKeyDown}
              value={query}
              {...handlers}
            />
            {isLoading ? null : (
              <span className="icon is-right">
                <i className="fas fa-search" />
              </span>
            )}
          </div>
          <div className="dropdown-menu">
            {error ? <SearchErrorNotification error={error} /> : null}
            {!error && data ? <Hits onClick={onHitSelect} index={index} hits={data._embedded.hits} /> : null}
          </div>
        </div>
      </div>
    </Field>
  );
};

const OmniSearchGuard: FC<Props> = ({ links }) => {
  if (!links.search) {
    return null;
  }
  return <OmniSearch />;
};

export default OmniSearchGuard;
