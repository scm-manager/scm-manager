import React, { ChangeEvent, FC, FocusEvent } from "react";
import classNames from "classnames";
import LabelWithHelpIcon from "./LabelWithHelpIcon";
import { createAttributesForTesting } from "../devBuild";

type Props = {
  name?: string;
  className?: string;
  label?: string;
  placeholder?: string;
  helpText?: string;
  disabled?: boolean;
  testId?: string;
  onChange?: (event: ChangeEvent<HTMLInputElement>) => void;
  onBlur?: (event: FocusEvent<HTMLInputElement>) => void;
  ref?: React.Ref<HTMLInputElement>;
};

const FileInput: FC<Props> = ({
  name,
  testId,
  helpText,
  placeholder,
  disabled,
  label,
  className,
  ref,
  onBlur,
  onChange
}) => {
  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    if (onChange && event.target.files) {
      onChange(event);
    }
  };

  const handleBlur = (event: FocusEvent<HTMLInputElement>) => {
    if (onBlur && event.target.files) {
      onBlur(event);
    }
  };

  return (
    <div className={classNames("field", className)}>
      <LabelWithHelpIcon label={label} helpText={helpText} />
      <div className="control">
        <input
          ref={ref}
          name={name}
          className={classNames("input", "p-1", className)}
          type="file"
          placeholder={placeholder}
          disabled={disabled}
          onChange={handleChange}
          onBlur={handleBlur}
          {...createAttributesForTesting(testId)}
        />
      </div>
    </div>
  );
};

export default FileInput;
