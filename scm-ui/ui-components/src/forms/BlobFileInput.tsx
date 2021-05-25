import React, { ChangeEvent, FC, FocusEvent } from "react";
import { createFormFieldWrapper, FieldProps, FieldType, isLegacy, isUsingRef } from "./FormFieldTypes";
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
};

const InnerBlobFileInput: FC<FieldProps<Props, HTMLInputElement, FileList>> = ({
  name,
  testId,
  helpText,
  placeholder,
  disabled,
  label,
  className,
  ...props
}) => {
  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    if (props.onChange && event.target.files) {
      if (isUsingRef<Props, HTMLInputElement, FileList>(props)) {
        props.onChange(event);
      } else if (isLegacy(props)) {
        props.onChange(event.target.files);
      }
    }
  };

  const handleBlur = (event: FocusEvent<HTMLInputElement>) => {
    if (props.onBlur && event.target.files) {
      if (isUsingRef<Props, HTMLInputElement, FileList>(props)) {
        props.onBlur(event);
      } else if (isLegacy(props)) {
        props.onBlur(event.target.files, name);
      }
    }
  };

  return (
    <div className={classNames("field", className)}>
      <LabelWithHelpIcon label={label} helpText={helpText} />
      <div className="control">
        <input
          ref={props.innerRef}
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

const BlobFileInput: FieldType<Props, HTMLInputElement, FileList> = createFormFieldWrapper(InnerBlobFileInput);

export default BlobFileInput;
