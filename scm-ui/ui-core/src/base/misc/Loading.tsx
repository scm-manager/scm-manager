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

import React, { FC, HTMLAttributes, Ref } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import Image from "./Image";

const Wrapper = styled.div`
  min-height: 256px;
`;

const FixedSizedImage = styled(Image)`
  width: 128px;
  height: 128px;
`;

type Props = HTMLAttributes<HTMLDivElement> & { message?: string; ref?: Ref<HTMLDivElement> };

const Loading: FC<Props> = ({ message, children, className, ref, ...props }) => {
  const [t] = useTranslation("commons");
  return (
    <Wrapper
      className={classNames(
        "is-flex",
        "is-flex-direction-column",
        "is-justify-content-center",
        "is-align-items-center",
        className
      )}
      {...props}
      ref={ref}
    >
      <FixedSizedImage className="mb-3" src="/images/loading.svg" alt={t("loading.alt")} />
      <p className="has-text-centered">
        {message}
        {children}
      </p>
    </Wrapper>
  );
};

export default Loading;
