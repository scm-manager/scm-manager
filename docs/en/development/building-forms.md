---
title: Building Forms
subtitle: Howto build forms for SCM-Manager
displayToc: false
---

Below we would like to explain how to write [React Hook Form](https://react-hook-form.com/) forms in an easy and fast way,
why it makes sense to switch and what needs to be considered.

### Legacy Process

Previously, we passed our self-written form component into the Configuration component's render function.
In the form we defined a prop for each entry, plus an onChange handler that takes the value and writes it to a state.
Additionally, we added validation logic when a field changes.

Especially in [old areas](https://github.com/scm-manager/scm-ldap-plugin/blob/develop/src/main/js/LdapConfigurationForm.tsx#L65), which were still built with class components, you should be very careful.

A lot of boilerplate code was needed, errors were frequent, and typings were generally flawed.

### Standard Process

React Hook Form will bring the `useForm` hook to validate your form with minimal re-render.
This contains a generic parameter which summarizes the possible input fields.

The useForm hook returns an object with several properties:

- `register` allows you to register an input or select element and apply validation rules to React Hook Form.
- `formState` contains information about the form state. This can also specify `isValid`.
- `handleSubmit` is called when you press the submit button and will receive the form data if form validation is successful.
- `reset` reset either the entire form state or part of the form state.

```tsx
import React, { FC, useEffect } from "react";
// import hook from react-hook-form library
import { useForm } from "react-hook-form";

const ReactHookForm: FC = () => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<Name>();
  const [stored, setStored] = useState<Person>();

  const onSubmit = (person: Person) => {
    setStored(person);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <InputField label="First Name" autofocus={true} {...register("firstName")} />
      <InputField
        label="Last Name"
        {...register("lastName", { required: true })}
        validationError={!!errors.lastName}
        errorMessage={"Last name is required"}
      />
      <Level className="pt-2" right={<SubmitButton>Submit</SubmitButton>} />
    </form>
  );
};
```

### Building Configuration Forms

`UseConfigLink` from `@scm-manager/ui-api` gets links via prop from binder and loads initial config asynchronously,
also specifies as reading part whether readOnly (no update link was set) and as writing part an update method.
As well as formProps for isLoading, isUpdating etc for ConfigurationForm.

```tsx
import React, { FC, useEffect } from "react";
import { useForm } from "react-hook-form";

const GlobalConfig: FC<Props> = ({ link }) => {
  // formProps spread syntax returns prop for name, onBlur, onChange and ref and additionally attaches them to fields
  const { initialConfiguration, update, isReadOnly, ...formProps } = useConfigLink<GlobalConfigurationDto>(link);
  const { formState, handleSubmit, register, reset, control } = useForm<GlobalConfigurationDto>({
    // mode onChange should be specified so that validation takes place immediately!
    mode: "onChange",
  });

  // ...
};
```

ConfigurationForm only takes care of the display of the component. All the logic now lives in the hook.

Registering your own `onChange`-handler is not necessary anymore.
`onSubmit` `handleSubmit`-function passes own submit function, which is called with filled form data type.

In the `register`-function you can specify additional options for validation.
For example, _required, min, max, pattern_.

```tsx
return (
  <ConfigurationForm isValid={formState.isValid} isReadOnly={isReadOnly} onSubmit={handleSubmit(update)} {...formProps}>
    <Title title={t("settings.title")} />
    <Checkbox
      label={t("fastForwardOnly.label")}
      helpText={t("fastForwardOnly.helpText")}
      disabled={isReadOnly}
      {...register("fastForwardOnly", { shouldUnregister: true })}
    />
    <InputField
      label={t("branchesAndTagsPatterns.label")}
      helpText={t("branchesAndTagsPatterns.helpText")}
      disabled={isReadOnly}
      {...register("branchesAndTagsPatterns")}
    />
    <GpgVerificationControl control={control} isReadonly={isReadOnly} />
  </ConfigurationForm>
);
```

#### Note when using `formState`

Be sure to use as proxy to get objects out (not formState.isValid!), because you won't notice the render cycle otherwise.

#### Set to initial values

In synchronous loading, a form can be set to an initial value using `defaultValue`.
In the asynchronous case, values for each field can be set separately by using `defaultValue={stored.fastForwardOnly}` or an entire form using `reset`.

```tsx
useEffect(() => {
  if (initialConfiguration) {
    reset(initialConfiguration);
  }
}, [initialConfiguration]);
```

### Note when Creating new Components

- If possible, pass all props.
- React Hook Form needs the following values for event to be recognized: name, onChange, onBlur, ref (reference to input element).
- `FormFieldTypes` is not a base, but helps for backwards compatibility with old function types. When writing a new component omit old onChange!
- Since some components have other elements built around an input field, there is also the `forwardRef`. It creates a reference that can be passed to an inner element.
- Nested forms are a bit more complex to build and might need a wrapper.
- Validation rules are all based on the HTML standard and also allow for custom validation methods.
- Fields marked as `disabled` in SCM-Manager won't be included on submission. If you want to prevent interaction but need to submit the value of a form element, `readOnly` is the better choice.

Some implementations:

- [Git Global Configuration](https://github.com/scm-manager/scm-manager/blob/develop/scm-plugins/scm-git-plugin/src/main/js/GitGlobalConfiguration.tsx#L43)
- [Repository Mirror Plugin Config](https://github.com/scm-manager/scm-repository-mirror-plugin/blob/develop/src/main/js/config/GlobalConfig.tsx#L37)
