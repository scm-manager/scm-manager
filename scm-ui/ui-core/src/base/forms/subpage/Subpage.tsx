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

import React, { ComponentProps, FC, ReactNode, useEffect } from "react";
import styled from "styled-components";
import { useTranslation } from "react-i18next";
import { Button, Icon } from "../../buttons";
import { useAriaId } from "../../helpers";
import { Level } from "../../misc";
import { ErrorNotification } from "../../notifications";
import { useScmFormContext } from "../ScmFormContext";
import { useSubpageContext } from "./SubpageContext";
import { ScmNestedFormPathContextProvider } from "../FormPathContext";

const ButtonsContainer = styled.div`
  display: flex;
  gap: 0.75rem;
`;

const PaddinglessButton = styled(Button)`
  && {
    padding: 0;
  }
`;

const HeaderLevel = styled(Level)`
`;

/**
 * A horizontal divider line for structuring the content of a `SubpageForm`/`Subpage`.
 * Renders as an `<hr>` and can be spaced via utility classes (e.g. `className="my-2"`).
 */
const Divider = styled.hr`
  background-color: var(--scm-border-color, #dbdbdb);
  border: none;
  height: 1px;
  margin: 1.5rem 0;
`;

type SubpageProps = {
  name: string;
  title: string;
  path?: string;
  children?: ReactNode;
};

/**
 * A single subpage of a `SubpageForm`. It is only rendered while it is the active subpage.
 * It renders its own *Save* and *Cancel* buttons; saving submits the *whole* configuration object,
 * canceling returns to the overview and discards unsaved changes.
 *
 * @beta
 * @since 4.0.0
 */
const Subpage: FC<SubpageProps> = ({ name, title, path, children }) => {
  const { activeSubpage, registerSubpage, closeSubpage, saveActiveSubpage, error } = useSubpageContext();
  const { formState, readOnly, formId } = useScmFormContext();
  const { t } = useTranslation("commons");

  useEffect(() => registerSubpage(name, title), [name, title, registerSubpage]);

  if (activeSubpage !== name) {
    return null;
  }

  const { isDirty, isValid, isSubmitting } = formState;

  return (
    <>
      {path ? <ScmNestedFormPathContextProvider path={path}>{children}</ScmNestedFormPathContextProvider> : children}
      {error ? <ErrorNotification error={error} /> : null}
      {!readOnly ? (
        <Level
          right={
            <ButtonsContainer>
              <Button type="button" onClick={closeSubpage} testId={`${formId}-${name}-cancel-button`}>
                {t("form.cancel")}
              </Button>
              <Button
                type="button"
                variant="primary"
                onClick={saveActiveSubpage}
                disabled={!isDirty || !isValid}
                isLoading={isSubmitting}
                testId={`${formId}-${name}-submit-button`}
              >
                {t("form.submit")}
              </Button>
            </ButtonsContainer>
          }
        />
      ) : null}
    </>
  );
};

type OpenButtonProps = Omit<ComponentProps<typeof Button>, "onClick" | "type"> & {
  subpage: string;
  children: ReactNode;
};

const OpenButton: FC<OpenButtonProps> = ({ subpage, children, ...props }) => {
  const { openSubpage } = useSubpageContext();
  const { readOnly } = useScmFormContext();

  if (readOnly) {
    return null;
  }

  return (
    <PaddinglessButton variant="ghost" type="button" onClick={() => openSubpage(subpage)} {...props}>
      {children}
    </PaddinglessButton>
  );
};

type SubpageHeaderProps = Omit<OpenButtonProps, "subpage" | "children"> & {
  subpage: string;
  label: ReactNode;
  children: ReactNode;
  icon?: string;
};

const SubpageHeader: FC<SubpageHeaderProps> = ({ subpage, label, children, icon = "edit", className, ...props }) => {
  const labelId = useAriaId();
  return (
    <HeaderLevel
      left={
        <span id={labelId} className="hast-text-grey">
          {label}
        </span>
      }
      right={
        <OpenButton subpage={subpage} className={className} aria-describedby={labelId} {...props}>
          <Icon className="mr-1">{icon}</Icon>
          {children}
        </OpenButton>
      }
    />
  );
};

export { OpenButton, SubpageHeader, Divider };
export default Subpage;
