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

const BreadcrumbNav = styled.nav`
  li + li::before {
    content: "›";
    color: inherit;
    padding: 0 0.5rem;
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

  return (
    <div className="notification is-success">
      <button className="delete" onClick={hide} />
      {label}
    </div>
  );
};

const SubpageBreadcrumb: FC = () => {
  const { title, activeSubpage, subpageTitles, closeSubpage } = useSubpageContext();

  return (
    <BreadcrumbNav className="breadcrumb subtitle" aria-label="breadcrumbs">
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
            {title}
          </a>
        </li>
        {activeSubpage !== null ? (
          <li className="is-active">
            <a href="#" aria-current="page" onClick={(e) => e.preventDefault()}>
              {subpageTitles[activeSubpage] ?? activeSubpage}
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
