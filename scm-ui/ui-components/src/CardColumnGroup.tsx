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

import React, { ReactNode } from "react";
import { Link } from "react-router-dom";
import classNames from "classnames";
import Icon from "./Icon";
import { withTranslation, WithTranslation } from "react-i18next";

type Props = WithTranslation & {
  name: ReactNode;
  url?: string;
  elements: ReactNode[];
};

type State = {
  collapsed: boolean;
};

class CardColumnGroup extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      collapsed: false,
    };
  }

  toggleCollapse = () => {
    this.setState((prevState) => ({
      collapsed: !prevState.collapsed,
    }));
  };

  isLastEntry = (array: ReactNode[], index: number) => {
    return index === array.length - 1;
  };

  isLengthOdd = (array: ReactNode[]) => {
    return array.length % 2 !== 0;
  };

  isFullSize = (array: ReactNode[], index: number) => {
    return this.isLastEntry(array, index) && this.isLengthOdd(array);
  };

  render() {
    const { name, url, elements, t } = this.props;
    const { collapsed } = this.state;

    let icon = <Icon name="angle-right" color="inherit" alt={t("cardColumnGroup.showContent")} />;
    let content = null;
    if (!collapsed) {
      icon = <Icon name="angle-down" color="inherit" alt={t("cardColumnGroup.hideContent")} />;
      content = elements.map((entry, index) => {
        const fullColumnWidth = this.isFullSize(elements, index);
        const sizeClass = fullColumnWidth ? "is-full" : "is-half";
        return (
          <div
            className={classNames("box", "box-link-shadow", "column", "is-relative", "is-clipped", sizeClass)}
            key={index}
          >
            {entry}
          </div>
        );
      });
    }

    return (
      <div className="mb-4">
        <h3>
          <span className={classNames("is-size-4", "is-clickable")} onClick={this.toggleCollapse}>
            {icon}
          </span>{" "}
          {url ? (
            <Link to={url} className="has-text-secondary-more">
              {name}
            </Link>
          ) : (
            name
          )}
        </h3>
        <hr />
        <div className={classNames("columns", "card-columns", "is-multiline", "mx-3", "my-0")}>{content}</div>
        <div className="is-clearfix" />
      </div>
    );
  }
}

export default withTranslation("commons")(CardColumnGroup);
