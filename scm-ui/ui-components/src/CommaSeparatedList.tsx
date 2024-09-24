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
import { useTranslation } from "react-i18next";

const CommaSeparatedList: FC = ({ children }) => {
  const [t] = useTranslation("commons");
  const childArray = React.Children.toArray(children);
  return (
    <>
      {childArray.map((p, i) => {
        if (i === 0) {
          return <React.Fragment key={i}>{p}</React.Fragment>;
        } else if (i + 1 === childArray.length) {
          return (
            <React.Fragment key={i}>
              {" "}
              {t("commaSeparatedList.lastDivider")} {p}{" "}
            </React.Fragment>
          );
        } else {
          return <React.Fragment key={i}>, {p}</React.Fragment>;
        }
      })}
    </>
  );
};

export default CommaSeparatedList;
