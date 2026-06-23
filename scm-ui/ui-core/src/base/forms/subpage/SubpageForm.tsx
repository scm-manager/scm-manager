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
import { useSearchParams } from "react-router-dom";
import styled from "styled-components";
import { ScmFormContextProvider } from "../ScmFormContext";
import { SubpageContextProvider, useSubpageContext } from "./SubpageContext";
import { Notification } from "../../notifications";
import { Icon } from "../../buttons";

const BreadcrumbNav = styled.nav`
  ol {
    align-items: center;
  }

  && li + li::before,
  && li:not(:last-child)::after {
    content: none;
  }

  .icon {
    height: auto;
    color: var(--scm-secondary-more-color, #363636);
    font-size: 16px;
  }

  /* 12px gap on each side of the separator icon; beats Bulma's .breadcrumb .icon:first-child margin */
  && li .icon {
    margin-left: 12px;
    margin-right: 12px;
  }

  .subtitle {
    color: var(--scm-secondary-more-color, #363636);
    font-size: 20px;
    font-weight: 600;
    line-height: 18px;
  }

  li + li .subtitle {
    font-weight: 400;
  }

  && a {
    color: inherit;
    border-radius: 4px;
    padding-left: 0;
    padding-right: 0;
  }

  a:focus-visible {
    outline-width: 3px;
    outline-offset: 2px;
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
      <ol role="list">
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
            <Icon>angle-right</Icon>
            <a href="#" aria-current="page" onClick={(e) => e.preventDefault()}>
              <span className="subtitle">{subpageTitles[activeSubpage] ?? activeSubpage}</span>
            </a>
          </li>
        ) : null}
      </ol>
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
  subpageParam?: string;
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
  subpageParam = "subpage",
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

  const submitForm = useCallback(async () => {
    let success = false;
    await handleSubmit(async (data) => {
      setError(null);
      try {
        await onSubmit(data);
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

  const [searchParams, setSearchParams] = useSearchParams();
  const activeSubpage = readOnly ? null : searchParams.get(subpageParam);

  const setSubpageParam = useCallback(
    (name: string | null) => {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        if (name === null) {
          next.delete(subpageParam);
        } else {
          next.set(subpageParam, name);
        }
        return next;
      });
    },
    [setSearchParams, subpageParam],
  );

  const openSubpage = useCallback(
    (name: string) => {
      setError(null);
      setShowSuccessNotification(false);
      setSubpageParam(name);
    },
    [setSubpageParam],
  );

  const closeSubpage = useCallback(() => {
    reset();
    setSubpageParam(null);
  }, [reset, setSubpageParam]);

  const saveActiveSubpage = useCallback(async () => {
    const success = await submitForm();
    if (success) {
      setSubpageParam(null);
    }
  }, [submitForm, setSubpageParam]);

  return (
    <ScmFormContextProvider {...form} readOnly={isSubmitting || readOnly} t={translateWithFallback} formId={prefix}>
      <SubpageContextProvider
        title={title}
        error={error}
        activeSubpage={activeSubpage}
        openSubpage={openSubpage}
        closeSubpage={closeSubpage}
        saveActiveSubpage={saveActiveSubpage}
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
