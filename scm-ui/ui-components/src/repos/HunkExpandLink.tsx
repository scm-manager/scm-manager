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

import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";

type Props = {
  icon: string;
  text: string;
  onClick: () => Promise<any>;
};

const HunkExpandLink: FC<Props> = ({ icon, text, onClick }) => {
  const [t] = useTranslation("repos");
  const [loading, setLoading] = useState(false);

  const onClickWithLoadingMarker = () => {
    if (loading) {
      return;
    }
    setLoading(true);
    onClick().then(() => setLoading(false));
  };

  return (
    <span className="is-clickable" onClick={onClickWithLoadingMarker}>
      <i className={classNames("fa", icon)} /> {loading ? t("diff.expanding") : text}
    </span>
  );
};

export default HunkExpandLink;
