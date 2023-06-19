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

export { default as Combobox } from "./combobox/Combobox";
export { default as ConfigurationForm } from "./ConfigurationForm";
export { default as SelectField } from "./select/SelectField";
export { default as ChipInputField } from "./chip-input/ChipInputField";
export { default as ComboboxField } from "./combobox/ComboboxField";
export { default as Select } from "./select/Select";
export * from "./resourceHooks";

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
});
