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
import React, { FC, KeyboardEvent as ReactKeyboardEvent, MouseEvent, useCallback, useEffect, useState } from "react";
import { Hit, Links, ValueHitField } from "@scm-manager/ui-types";
import styled from "styled-components";
import { useSearch } from "@scm-manager/ui-api";
import classNames from "classnames";
import { Link, useHistory, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
  Button,
  devices,
  HitProps,
  Notification,
  RepositoryAvatar,
  useStringHitFieldValue
} from "@scm-manager/ui-components";
import SyntaxHelp from "../search/SyntaxHelp";
import SyntaxModal from "../search/SyntaxModal";
import SearchErrorNotification from "../search/SearchErrorNotification";
import queryString from "query-string";

const Input = styled.input`
  border-radius: 4px !important;
`;

type Props = {
  links: Links;
};

const namespaceAndName = (hit: Hit) => {
  const namespace = (hit.fields["namespace"] as ValueHitField).value as string;
  const name = (hit.fields["name"] as ValueHitField).value as string;
  return `${namespace}/${name}`;
};

type HitsProps = {
  hits: Hit[];
  index: number;
  showHelp: () => void;
  gotoDetailSearch: () => void;
  clear: () => void;
};

const QuickSearchNotification: FC = ({ children }) => <div className="dropdown-content p-4">{children}</div>;

type GotoProps = {
  gotoDetailSearch: () => void;
};

const EmptyHits: FC = () => {
  const [t] = useTranslation("commons");
  return (
    <Notification className="m-4" type="info">
      {t("search.quickSearch.noResults")}
    </Notification>
  );
};

const ResultHeading = styled.h3`
  border-bottom: 1px solid lightgray;
`;

const DropdownMenu = styled.div`
  max-width: 20rem;
`;

const ResultFooter = styled.div`
  border-top: 1px solid lightgray;
`;

const SearchInput = styled(Input)`
  @media screen and (max-width: ${devices.mobile.width}px) {
    width: 9rem;
  }
`;

const AvatarSection: FC<HitProps> = ({ hit }) => {
  const namespace = useStringHitFieldValue(hit, "namespace");
  const name = useStringHitFieldValue(hit, "name");
  const type = useStringHitFieldValue(hit, "type");

  const repository = hit._embedded?.repository;
  if (!namespace || !name || !type || !repository) {
    return null;
  }

  return (
    <span className="mr-2">
      <RepositoryAvatar repository={repository} size={24} />
    </span>
  );
};

const MoreResults: FC<GotoProps> = ({ gotoDetailSearch }) => {
  const [t] = useTranslation("commons");
  return (
    <ResultFooter className={classNames("dropdown-item", "has-text-centered", "mx-2", "px-2", "py-1")}>
      <Button action={gotoDetailSearch} color="primary" data-omnisearch="true">
        {t("search.quickSearch.moreResults")}
      </Button>
    </ResultFooter>
  );
};

const HitsList: FC<HitsProps> = ({ hits, index, clear, gotoDetailSearch }) => {
  const id = useCallback(namespaceAndName, [hits]);
  if (hits.length === 0) {
    return <EmptyHits />;
  }
  return (
    <ul id="omni-search-results" aria-expanded="true" role="listbox">
      {hits.map((hit, idx) => (
        <li
          key={id(hit)}
          onMouseDown={e => e.preventDefault()}
          onClick={clear}
          role="option"
          aria-selected={idx === index}
          id={idx === index ? "omni-search-selected-option" : undefined}
        >
          <Link
            className={classNames("is-flex", "dropdown-item", "has-text-weight-medium", "is-ellipsis-overflow", {
              "is-active": idx === index
            })}
            title={id(hit)}
            to={`/repo/${id(hit)}`}
            data-omnisearch="true"
          >
            <AvatarSection hit={hit} />
            {id(hit)}
          </Link>
        </li>
      ))}
    </ul>
  );
};

type ScreenReaderHitSummaryProps = {
  hits: Hit[];
};

const ScreenReaderHitSummary: FC<ScreenReaderHitSummaryProps> = ({ hits }) => {
  const [t] = useTranslation("commons");
  const key = hits.length > 0 ? "screenReaderHint" : "screenReaderHintNoResult";
  return (
    <span aria-live="assertive" className="is-sr-only">
      {t(`search.quickSearch.${key}`, { count: hits.length })}
    </span>
  );
};

const Hits: FC<HitsProps> = ({ showHelp, gotoDetailSearch, hits, ...rest }) => {
  const [t] = useTranslation("commons");

  return (
    <>
      <div className="dropdown-content">
        <ScreenReaderHitSummary hits={hits} />
        <ResultHeading
          className={classNames(
            "dropdown-item",
            "is-flex",
            "is-justify-content-space-between",
            "is-align-items-center",
            "mx-2",
            "px-2",
            "py-1",
            "has-text-weight-bold"
          )}
        >
          <span>{t("search.quickSearch.resultHeading")}</span>
          <SyntaxHelp onClick={showHelp} />
        </ResultHeading>
        <HitsList showHelp={showHelp} gotoDetailSearch={gotoDetailSearch} hits={hits} {...rest} />
        <MoreResults gotoDetailSearch={gotoDetailSearch} />
      </div>
    </>
  );
};

const useKeyBoardNavigation = (gotoDetailSearch: () => void, clear: () => void, hits?: Array<Hit>) => {
  const [index, setIndex] = useState(-1);
  const history = useHistory();
  useEffect(() => {
    setIndex(-1);
  }, [hits]);

  const onKeyDown = (e: ReactKeyboardEvent<HTMLInputElement>) => {
    // We use e.which, because ie 11 does not support e.code
    // https://caniuse.com/keyboardevent-code
    switch (e.which) {
      case 40: // e.code: ArrowDown
        if (hits) {
          setIndex(idx => {
            if (idx + 1 < hits.length) {
              return idx + 1;
            }
            return idx;
          });
        }
        break;
      case 38: // e.code: ArrowUp
        if (hits) {
          setIndex(idx => {
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
        if (index >= 0) {
          setIndex(-1);
        } else {
          clear();
        }
        break;
    }
  };

  return {
    onKeyDown,
    index
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

const isMoreResultsButton = (element: Element) => {
  return element.tagName.toLocaleLowerCase("en") === "button" && element.className.includes("is-primary");
};

const isOnmiSearchElement = (element: Element) => {
  return element.getAttribute("data-omnisearch") || isMoreResultsButton(element);
};

const useShowResultsOnFocus = () => {
  const [showResults, setShowResults] = useState(false);
  useEffect(() => {
    if (showResults) {
      const close = () => {
        setShowResults(false);
      };

      const onKeyUp = (e: KeyboardEvent) => {
        if (e.which === 9) {
          // tab
          const element = document.activeElement;
          if (!element || !isOnmiSearchElement(element)) {
            close();
          }
        }
      };

      window.addEventListener("click", close);
      window.addEventListener("keyup", onKeyUp);
      return () => {
        window.removeEventListener("click", close);
        window.removeEventListener("keyup", onKeyUp);
      };
    }
  }, [showResults]);
  return {
    showResults,
    onClick: (e: MouseEvent<HTMLInputElement>) => {
      e.stopPropagation();
      setShowResults(true);
    },
    onKeyPress: () => setShowResults(true),
    onFocus: () => setShowResults(true),
    hideResults: () => setShowResults(false)
  };
};

const useSearchParams = () => {
  const location = useLocation();
  const pathname = location.pathname;

  let searchType = "repository";
  let initialQuery = "";
  if (pathname.startsWith("/search/")) {
    const path = pathname.substring("/search/".length);
    const index = path.indexOf("/");
    if (index > 0) {
      searchType = path.substring(0, index);
    } else {
      searchType = path;
    }

    const queryParams = queryString.parse(location.search);
    const q = queryParams.q;
    if (Array.isArray(q)) {
      initialQuery = q[0] || "";
    } else {
      initialQuery = q || "";
    }
  }

  return {
    searchType,
    initialQuery
  };
};

const OmniSearch: FC = () => {
  const [t] = useTranslation("commons");
  const { searchType, initialQuery } = useSearchParams();
  const [query, setQuery] = useState(initialQuery);
  const debouncedQuery = useDebounce(query, 250);
  const { data, isLoading, error } = useSearch(debouncedQuery, { type: "repository", pageSize: 5 });
  const { showResults, hideResults, ...handlers } = useShowResultsOnFocus();
  const [showHelp, setShowHelp] = useState(false);
  const history = useHistory();

  const openHelp = () => setShowHelp(true);
  const closeHelp = () => setShowHelp(false);
  const clearQuery = () => setQuery("");

  const gotoDetailSearch = () => {
    if (query.length > 1) {
      history.push(`/search/${searchType}/?q=${query}`);
      hideResults();
    }
  };

  const { onKeyDown, index } = useKeyBoardNavigation(gotoDetailSearch, clearQuery, data?._embedded?.hits);

  return (
    <div className={classNames("navbar-item", "field", "mb-0")}>
      {showHelp ? <SyntaxModal close={closeHelp} /> : null}
      <div
        className={classNames("control", "has-icons-right", {
          "is-loading": isLoading
        })}
      >
        <div className={classNames("dropdown", { "is-active": (!!data || error) && showResults })}>
          <div className="dropdown-trigger">
            <SearchInput
              className="input is-small"
              type="text"
              placeholder={t("search.placeholder")}
              onChange={e => setQuery(e.target.value)}
              onKeyDown={onKeyDown}
              value={query}
              role="combobox"
              aria-autocomplete="both"
              data-omnisearch="true"
              aria-expanded={query.length > 2}
              aria-label={t("search.ariaLabel")}
              aria-owns="omni-search-results"
              aria-activedescendant={index >= 0 ? "omni-search-selected-option" : undefined}
              {...handlers}
            />
            {isLoading ? null : (
              <span className="icon is-right">
                <i className="fas fa-search" />
              </span>
            )}
          </div>
          <DropdownMenu className="dropdown-menu" onMouseDown={e => e.preventDefault()}>
            {error ? (
              <QuickSearchNotification>
                <SearchErrorNotification error={error} showHelp={openHelp} />
              </QuickSearchNotification>
            ) : null}
            {!error && data ? (
              <Hits
                showHelp={openHelp}
                gotoDetailSearch={gotoDetailSearch}
                clear={clearQuery}
                index={index}
                hits={data._embedded?.hits || []}
              />
            ) : null}
          </DropdownMenu>
        </div>
      </div>
    </div>
  );
};

const OmniSearchGuard: FC<Props> = ({ links }) => {
  if (!links.search) {
    return null;
  }
  return <OmniSearch />;
};

export default OmniSearchGuard;
