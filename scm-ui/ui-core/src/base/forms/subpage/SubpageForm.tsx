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

import React, { FC, useCallback, useEffect, useState } from "react";
import { DefaultValues, SubmitHandler, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { ScmFormContextProvider } from "../ScmFormContext";
import { SubpageContextProvider, useSubpageContext } from "./SubpageContext";
import { Notification } from "../../notifications";
import { Icon } from "../../buttons";

const BreadcrumbNav = styled.nav`
  /* Bulma aligns breadcrumb items to flex-start, which leaves the active item sitting a pixel high. */
  ul {
    align-items: center;
  }

  /* We render an angle-right icon as separator, so suppress both built-in pseudo-separators:
     Bulma core's "li + li::before" and the global "::after" from _main.scss. */
  && li + li::before,
  && li:not(:last-child)::after {
    content: none;
  }

  /* Let the separator icon size to the text instead of Bulma's fixed 1.5rem box,
     so it stays optically centered with the breadcrumb labels. */
  .icon {
    height: auto;
    color: var(--scm-secondary-more-color, #363636);
  }

  /* Breadcrumb typography per design: 20px, tight line-height, secondary-more color.
     The title crumb is semibold; the active subpage crumb (the second) is regular. */
  .subtitle {
    color: var(--scm-secondary-more-color, #363636);
    font-size: 20px;
    font-weight: 600;
    line-height: 18px;
  }

  li + li .subtitle {
    font-weight: 400;
  }

  a {
    color: inherit;
  }

  li:not(.is-active) a:hover {
    text-decoration: underline;
  }
`;

const SuccessNotification: FC<{ label?: string; hide: () => void }> = ({ label, hide }) => {
  if (!label) {
    return null;
  }

  // role="status" announces the success to screen readers via its implicit polite live region.
  return (
    <Notification type="success" role="status" onClose={hide}>
      {label}
    </Notification>
  );
};

const SubpageBreadcrumb: FC = () => {
  const { title, activeSubpage, subpageTitles, closeSubpage } = useSubpageContext();
  const { t } = useTranslation("commons");

  return (
    <BreadcrumbNav className="breadcrumb subtitle" aria-label={t("subpageForm.breadcrumb.ariaLabel")}>
      <ul>
        <li className={activeSubpage === null ? "is-active" : undefined}>
          <a
            href="#"
            aria-current={activeSubpage === null ? "page" : undefined}
            onClick={(e) => {
              e.preventDefault();
              closeSubpage();
            }}
          >
            <span className="subtitle">{title}</span>
          </a>
        </li>
        {activeSubpage !== null ? (
          <li className="is-active">
            <Icon className="mx-2">angle-right</Icon>
            <a href="#" aria-current="page" onClick={(e) => e.preventDefault()}>
              <span className="subtitle">{subpageTitles[activeSubpage] ?? activeSubpage}</span>
            </a>
          </li>
        ) : null}
      </ul>
    </BreadcrumbNav>
  );
};

type Props<FormType extends Record<string, unknown>> = {
  children: React.ReactNode;
  title: string;
  translationPath: [namespace: string, prefix: string];
  onSubmit: SubmitHandler<FormType>;
  defaultValues: DefaultValues<FormType>;
  readOnly?: boolean;
  successMessageFallback?: string;
};

/**
 * A read-only "overview" variant of the `Form` component. It displays a configuration without a global
 * submit button and lets you split editing into `Subpage`s, each saving the whole configuration object on
 * its own. The subtitle renders as a breadcrumb of the configuration title and the active subpage title.
 *
 * This is an additional variant and does not replace `Form` or `ConfigurationForm`.
 *
 * @beta
 * @since 4.0.0
 */
function SubpageForm<FormType extends Record<string, unknown>>({
  children,
  title,
  onSubmit,
  defaultValues,
  translationPath,
  readOnly,
  successMessageFallback,
}: Props<FormType>) {
  const form = useForm<FormType>({ mode: "onChange", defaultValues });
  const { formState, handleSubmit, reset } = form;
  const [ns, prefix] = translationPath;
  const { t } = useTranslation(ns, { keyPrefix: prefix });
  const translateWithFallback = useCallback<typeof t>(
    (key, ...args) => {
      const translation = t(key, ...(args as any));
      if (translation === `${prefix}.${key}`) {
        return "";
      }
      return translation;
    },
    [prefix, t],
  );
  const { isSubmitting } = formState;
  const [error, setError] = useState<Error | null | undefined>();
  const [showSuccessNotification, setShowSuccessNotification] = useState(false);
  const successNotification = translateWithFallback("submit-success-notification", {
    defaultValue: successMessageFallback,
  });

  useEffect(() => reset(defaultValues as never), [defaultValues, reset]);

  // Submits the whole configuration object. Resolves to true when the save succeeded so the active
  // subpage can return to the overview; on validation or save errors it resolves to false.
  const submitForm = useCallback(async () => {
    let success = false;
    await handleSubmit(async (data) => {
      setError(null);
      try {
        await onSubmit(data);
        // Reset the dirty state to the saved values until the reloaded defaultValues arrive.
        reset(data);
        setShowSuccessNotification(true);
        success = true;
      } catch (e) {
        if (e instanceof Error) {
          setError(e);
        } else {
          throw e;
        }
      }
    })();
    return success;
  }, [handleSubmit, onSubmit, reset]);

  // Leaving a subpage discards unsaved changes; entering one clears stale notifications.
  const onLeaveSubpage = useCallback(() => reset(), [reset]);
  const onEnterSubpage = useCallback(() => {
    setError(null);
    setShowSuccessNotification(false);
  }, []);

  return (
    <ScmFormContextProvider {...form} readOnly={isSubmitting || readOnly} t={translateWithFallback} formId={prefix}>
      <SubpageContextProvider
        title={title}
        error={error}
        submitForm={submitForm}
        onLeaveSubpage={onLeaveSubpage}
        onEnterSubpage={onEnterSubpage}
      >
        <SubpageBreadcrumb />
        {showSuccessNotification ? (
          <SuccessNotification label={successNotification} hide={() => setShowSuccessNotification(false)} />
        ) : null}
        {children}
      </SubpageContextProvider>
    </ScmFormContextProvider>
  );
}

export default SubpageForm;
