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
import React, {
  ChangeEvent,
  KeyboardEvent,
  FocusEvent,
  FC,
  useRef,
  useEffect, ForwardedRef,
} from "react";
import classNames from "classnames";
import LabelWithHelpIcon from "./LabelWithHelpIcon";
import { createAttributesForTesting } from "../devBuild";

type BaseProps = {
  label?: string;
  name?: string;
  placeholder?: string;
  value?: string;
  type?: string;
  autofocus?: boolean;
  onReturnPressed?: () => void;
  validationError?: boolean;
  errorMessage?: string;
  informationMessage?: string;
  disabled?: boolean;
  helpText?: string;
  className?: string;
  testId?: string;
};

type LegacyProps = BaseProps & {
  onChange?: (value: string, name?: string) => void;
  onBlur?: (value: string, name?: string) => void;
  innerRef?: never;
};

type RefProps = BaseProps & {
  onChange?: (event: ChangeEvent<HTMLInputElement>) => void;
  onBlur?: (event: FocusEvent<HTMLInputElement>) => void;
};

type InnerRefProps = RefProps & {
  innerRef: React.ForwardedRef<HTMLInputElement>;
};

type Props = LegacyProps | InnerRefProps;

const isUsingRef = (props: Partial<Props>): props is InnerRefProps => {
  return (props as Partial<InnerRefProps>).innerRef !== undefined;
};

const isLegacy = (props: Props): props is LegacyProps => {
  return (props as Partial<InnerRefProps>).innerRef === undefined;
};

export const InnerInputField: FC<Props> = ({
  name,
  onReturnPressed,
  type,
  placeholder,
  value,
  validationError,
  errorMessage,
  informationMessage,
  disabled,
  label,
  helpText,
  className,
  testId,
  autofocus,
  ...props
}) => {
  const field = useRef<HTMLInputElement>(null);
  useEffect(() => {
    if (autofocus && field.current) {
      field.current.focus();
    }
  }, [autofocus, field]);

  useEffect(() => {
    if (isUsingRef(props) && props.innerRef) {
      if (typeof props.innerRef === "function") {
        props.innerRef(field.current);
      } else {
        props.innerRef.current = field.current;
      }
    }
  }, [field, props]);

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    if (props.onChange) {
      if (isUsingRef(props)) {
        props.onChange(event);
      } else if (isLegacy(props)) {
        props.onChange(event.target.value, name);
      }
    }
  };

  const handleBlur = (event: FocusEvent<HTMLInputElement>) => {
    if (props.onBlur) {
      if (isUsingRef(props)) {
        props.onBlur(event);
      } else if (isLegacy(props)) {
        props.onBlur(event.target.value, name);
      }
    }
  };

  const handleKeyPress = (event: KeyboardEvent<HTMLInputElement>) => {
    if (onReturnPressed && event.key === "Enter") {
      event.preventDefault();
      onReturnPressed();
    }
  };

  const errorView = validationError ? "is-danger" : "";
  let helper;
  if (validationError) {
    helper = <p className="help is-danger">{errorMessage}</p>;
  } else if (informationMessage) {
    helper = <p className="help is-info">{informationMessage}</p>;
  }
  return (
    <div className={classNames("field", className)}>
      <LabelWithHelpIcon label={label} helpText={helpText} />
      <div className="control">
        <input
          ref={field}
          name={name}
          className={classNames("input", errorView)}
          type={type}
          placeholder={placeholder}
          value={value}
          disabled={disabled}
          onChange={handleChange}
          onKeyPress={handleKeyPress}
          onBlur={handleBlur}
          {...createAttributesForTesting(testId)}
        />
      </div>
      {helper}
    </div>
  );
};

type OuterProps = RefProps & {
  ref: React.Ref<HTMLInputElement>;
};

function InputField(props: OuterProps, ref: ForwardedRef<HTMLInputElement>): React.ReactElement | null;
function InputField(props: LegacyProps, ref: ForwardedRef<HTMLInputElement>): React.ReactElement | null;
function InputField(props: LegacyProps | OuterProps, ref: ForwardedRef<HTMLInputElement>) {
  if (ref) {
    return <InnerInputField innerRef={ref} {...(props as RefProps)} />;
  }
  return <InnerInputField {...(props as LegacyProps)} />;
}

export default React.forwardRef(InputField);
