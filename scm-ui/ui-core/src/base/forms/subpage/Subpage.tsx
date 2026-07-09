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
import { Button, Icon, IconButton } from "../../buttons";
import { useAriaId } from "../../helpers";
import { Level } from "../../misc";
import { ErrorNotification } from "../../notifications";
import { useScmFormContext } from "../ScmFormContext";
import { useSubpageContext } from "./SubpageContext";
import { ScmNestedFormPathContextProvider } from "../FormPathContext";

const ButtonsContainer = styled.div`
  display: flex;
  gap: 0.75rem;
  
  @media screen and (max-width: 768px) {
    flex-direction: column;
  }
`;

const PaddinglessButton = styled(Button)`
  && {
    padding: 0;
    .icon {
      margin-left: 0 !important;
      margin-right: 0 !important;
    }
  }
`;

const HeaderLevel = styled(Level)`
  /* Theme-aware secondary text colour (same token as the breadcrumb title) so the header label keeps a
     sufficient contrast in the light, dark and high-contrast themes. Bulma's fixed .has-text-grey does not
     adapt and fails contrast on the light background. */
  .subtitle {
    color: var(--scm-secondary-more-color);
  }
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

type OpenIconButtonProps = Omit<ComponentProps<typeof IconButton>, "onClick"> & {
  subpage: string;
};

/**
 * Like {@link OpenButton}, but rendered as a bordered {@link IconButton} instead of a ghost button.
 * Use it for icon-only row actions (e.g. an edit button in a `SubpageTable`) so it matches the
 * neighbouring `IconButton`s. Requires an `aria-label`.
 *
 * @beta
 * @since 4.0.0
 */
const OpenIconButton: FC<OpenIconButtonProps> = ({ subpage, children, ...props }) => {
  const { openSubpage } = useSubpageContext();
  const { readOnly } = useScmFormContext();

  if (readOnly) {
    return null;
  }

  return (
    <IconButton onClick={() => openSubpage(subpage)} {...props}>
      {children}
    </IconButton>
  );
};

type SubpageHeaderProps = Omit<OpenButtonProps, "subpage" | "children"> & {
  subpage?: string;
  label: ReactNode;
  children?: ReactNode;
  icon?: string;
};

/**
 * A section header for a `SubpageForm` overview. Renders the `label` on the left and, when both a target
 * `subpage` and `children` are provided, an {@link OpenButton} (edit link) on the right that opens that
 * subpage. Omit `subpage`/`children` for a plain label-only header, e.g. above a `SubpageTable` whose
 * rows carry their own {@link OpenIconButton}s.
 *
 * @beta
 * @since 4.0.0
 */
const SubpageHeader: FC<SubpageHeaderProps> = ({ subpage, label, children, icon = "edit", className, ...props }) => {
  const labelId = useAriaId();
  return (
    <HeaderLevel
      left={
        <span id={labelId} className="subtitle">
          {label}
        </span>
      }
      right={
        subpage && children ? (
          <OpenButton subpage={subpage} className={className} aria-describedby={labelId} {...props}>
            <Icon className="mr-1">{icon}</Icon>
            {children}
          </OpenButton>
        ) : undefined
      }
    />
  );
};

export { OpenButton, OpenIconButton, SubpageHeader, Divider };
export default Subpage;
