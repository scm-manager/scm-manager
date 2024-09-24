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

import FormCmp from "./Form";
import FormRow from "./FormRow";
import ControlledInputField from "./input/ControlledInputField";
import ControlledCheckboxField from "./checkbox/ControlledCheckboxField";
import ControlledSecretConfirmationField from "./input/ControlledSecretConfirmationField";
import ControlledSelectField from "./select/ControlledSelectField";
import ControlledChipInputField from "./chip-input/ControlledChipInputField";
import { ScmFormListContextProvider } from "./ScmFormListContext";
import ControlledList from "./list/ControlledList";
import ControlledTable from "./table/ControlledTable";
import ControlledColumn from "./table/ControlledColumn";
import AddListEntryForm from "./AddListEntryForm";
import { ScmNestedFormPathContextProvider } from "./FormPathContext";
import ControlledComboboxField from "./combobox/ControlledComboboxField";
import ChipInputFieldComponent from "./chip-input/ChipInputField";
import ChipInput from "./headless-chip-input/ChipInput";
import ControlledRadioGroupField from "./radio-button/ControlledRadioGroupField";
import RadioGroupComponent from "./radio-button/RadioGroup";
import RadioButton from "./radio-button/RadioButton";
import RadioGroupFieldComponent from "./radio-button/RadioGroupField";

export { default as Field } from "./base/Field";
export { default as Checkbox } from "./checkbox/Checkbox";
export { default as Combobox } from "./combobox/Combobox";
export { default as ConfigurationForm } from "./ConfigurationForm";
export { default as SelectField } from "./select/SelectField";
export { default as ComboboxField } from "./combobox/ComboboxField";
export { default as Input } from "./input/Input";
export { default as Textarea } from "./input/Textarea";
export { default as Select } from "./select/Select";
export * from "./resourceHooks";
export { default as Label } from "./base/label/Label";

const RadioGroupExport = {
  Option: RadioButton,
};

export const RadioGroup = Object.assign(RadioGroupComponent, RadioGroupExport);
export const RadioGroupField = Object.assign(RadioGroupFieldComponent, RadioGroupExport);

export const ChipInputField = Object.assign(ChipInputFieldComponent, {
  AddButton: ChipInput.AddButton,
});

export const Form = Object.assign(FormCmp, {
  Row: FormRow,
  Input: ControlledInputField,
  Checkbox: ControlledCheckboxField,
  SecretConfirmation: ControlledSecretConfirmationField,
  Select: ControlledSelectField,
  PathContext: ScmNestedFormPathContextProvider,
  ListContext: ScmFormListContextProvider,
  List: ControlledList,
  AddListEntryForm: AddListEntryForm,
  Table: Object.assign(ControlledTable, {
    Column: ControlledColumn,
  }),
  ChipInput: ControlledChipInputField,
  Combobox: ControlledComboboxField,
  RadioGroup: Object.assign(ControlledRadioGroupField, RadioGroupExport),
});
