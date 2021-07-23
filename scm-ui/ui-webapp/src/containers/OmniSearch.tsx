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
import { BackendError, useSearch } from "@scm-manager/ui-api";
import classNames from "classnames";
import { Link, useHistory } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Button, ErrorNotification, Notification } from "@scm-manager/ui-components";

const Field = styled.div`
  margin-bottom: 0 !important;
`;

const Input = styled.input`
  border-radius: 4px !important;
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
  gotoDetailSearch: () => void;
  clear: () => void;
};

const QuickSearchNotification: FC = ({ children }) => <div className="dropdown-content p-4">{children}</div>;

type GotoProps = {
  gotoDetailSearch: () => void;
};

const EmptyHits: FC<GotoProps> = ({ gotoDetailSearch }) => {
  const [t] = useTranslation("commons");
  return (
    <QuickSearchNotification>
      <Notification type="info">{t("search.quickSearch.noResults")}</Notification>
      <MoreResults gotoDetailSearch={gotoDetailSearch} />
    </QuickSearchNotification>
  );
};

type ErrorProps = {
  error: Error;
};

const ParseErrorNotification: FC = () => {
  const [t] = useTranslation("commons");
  // TODO add link to query syntax page/modal
  return (
    <QuickSearchNotification>
      <Notification type="warning">{t("search.quickSearch.parseError")}</Notification>
    </QuickSearchNotification>
  );
};

const isBackendError = (error: Error | BackendError): error is BackendError => {
  return (error as BackendError).errorCode !== undefined;
};

const SearchErrorNotification: FC<ErrorProps> = ({ error }) => {
  // 5VScek8Xp1 is the id of sonia.scm.search.QueryParseException
  if (isBackendError(error) && error.errorCode === "5VScek8Xp1") {
    return <ParseErrorNotification />;
  }
  return (
    <QuickSearchNotification>
      <ErrorNotification error={error} />
    </QuickSearchNotification>
  );
};

const ResultHeading = styled.h3`
  border-bottom: 1px solid lightgray;
  margin: 0 0.5rem;
  padding: 0.375rem 0.5rem;
  font-weight: bold;
`;

const DropdownMenu = styled.div`
  max-width: 20rem;
`;

const ResultFooter = styled.div`
  border-top: 1px solid lightgray;
  margin: 0 0.5rem;
  padding: 0.375rem 0.5rem;
`;

const MoreResults: FC<GotoProps> = ({ gotoDetailSearch }) => {
  const [t] = useTranslation("commons");
  return (
    <ResultFooter className="dropdown-item has-text-centered">
      <Button action={gotoDetailSearch} color="primary">
        {t("search.quickSearch.moreResults")}
      </Button>
    </ResultFooter>
  );
};

const Hits: FC<HitsProps> = ({ hits, index, clear, gotoDetailSearch }) => {
  const id = useCallback(namespaceAndName, [hits]);
  const [t] = useTranslation("commons");

  if (hits.length === 0) {
    return <EmptyHits gotoDetailSearch={gotoDetailSearch} />;
  }

  return (
    <div aria-expanded="true" role="listbox" className="dropdown-content">
      <ResultHeading className="dropdown-item">{t("search.quickSearch.resultHeading")}</ResultHeading>
      {hits.map((hit, idx) => (
        <div key={id(hit)} onMouseDown={(e) => e.preventDefault()} onClick={clear}>
          <Link
            className={classNames("dropdown-item", "has-text-weight-medium", "is-ellipsis-overflow", {
              "is-active": idx === index,
            })}
            title={id(hit)}
            to={`/repo/${id(hit)}`}
            role="option"
          >
            {id(hit)}
          </Link>
        </div>
      ))}
      <MoreResults gotoDetailSearch={gotoDetailSearch} />
    </div>
  );
};

const useKeyBoardNavigation = (gotoDetailSearch: () => void, clear: () => void, hits?: Array<Hit>) => {
  const [index, setIndex] = useState(-1);
  const history = useHistory();
  useEffect(() => {
    setIndex(-1);
  }, [hits]);

  const onKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    // We use e.which, because ie 11 does not support e.code
    // https://caniuse.com/keyboardevent-code
    switch (e.which) {
      case 40: // e.code: ArrowDown
        if (hits) {
          setIndex((idx) => {
            if (idx + 1 < hits.length) {
              return idx + 1;
            }
            return idx;
          });
        }
        break;
      case 38: // e.code: ArrowUp
        if (hits) {
          setIndex((idx) => {
            if (idx > 0) {
              return idx - 1;
            }
            return idx;
          });
        }
        break;
      case 13: // e.code: Enter
        if (hits && index >= 0) {
          const hit = hits[index];
          history.push(`/repo/${namespaceAndName(hit)}`);
          clear();
        } else {
          e.preventDefault();
          gotoDetailSearch();
        }
        break;
      case 27: // e.code: Escape
        clear();
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
  return {
    showResults,
    onClick: (e: MouseEvent<HTMLInputElement>) => {
      e.stopPropagation();
      setShowResults(true);
    },
    onKeyPress: () => setShowResults(true),
    onFocus: () => setShowResults(true),
    onBlur: () => setShowResults(false),
    hideResults: () => setShowResults(false),
  };
};

const OmniSearch: FC = () => {
  const [query, setQuery] = useState("");
  const debouncedQuery = useDebounce(query, 250);
  const { data, isLoading, error } = useSearch(debouncedQuery, { type: "repository", pageSize: 5 });
  const { showResults, hideResults, ...handlers } = useShowResultsOnFocus();
  const history = useHistory();

  const clearQuery = () => {
    setQuery("");
  };

  const gotoDetailSearch = () => {
    history.push(`/search/repository/?q=${query}`);
    hideResults();
  };

  const { onKeyDown, index } = useKeyBoardNavigation(gotoDetailSearch, clearQuery, data?._embedded.hits);

  return (
    <Field className="navbar-item field">
      <div
        className={classNames("control", "has-icons-right", {
          "is-loading": isLoading,
        })}
      >
        <div className={classNames("dropdown", { "is-active": (!!data || error) && showResults })}>
          <div className="dropdown-trigger">
            <Input
              className="input is-small"
              type="text"
              placeholder="Search ..."
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={onKeyDown}
              value={query}
              role="combobox"
              aria-autocomplete="list"
              {...handlers}
            />
            {isLoading ? null : (
              <span className="icon is-right">
                <i className="fas fa-search" />
              </span>
            )}
          </div>
          <DropdownMenu className="dropdown-menu" onMouseDown={(e) => e.preventDefault()}>
            {error ? <SearchErrorNotification error={error} /> : null}
            {!error && data ? (
              <Hits gotoDetailSearch={gotoDetailSearch} clear={clearQuery} index={index} hits={data._embedded.hits} />
            ) : null}
          </DropdownMenu>
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
