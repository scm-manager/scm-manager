# CLI Guidelines

## Resource centered api
Every new command group starts with the resource name like `repo`.
Commands should be defined as singular. For repository commands it is `repo` and **not** `repos`.
You may set aliases to make your command more convenient to use.

```java
@CommandLine.Command(name = "repo")
```

## Subcommands
Subcommands are action centered and can look like `scm repo create x/y`. 

The RepositoryCreateCommand is a subcommand of RepositoryCommand and must be explicitly annotated.
```java
@ParentCommand(value = RepositoryCommand.class)
```

## Parameters and options
Every required field for a command must be a parameter. All other fields have to be options.

`scm repo create git namespace/name --init --description="test"`

The repository `type` and `namespace/name` must be set, so they must be annotated as parameters. 
The other fields like `init` and `description` are optional and are therefore annotated as options.
```java
  @CommandLine.Parameters
  private String type;
  @CommandLine.Parameters
  private String repository;
  @CommandLine.Option(names = "--description")
  private String description;
  @CommandLine.Option(names = "--contact")
  private String contact;
  @CommandLine.Option(names = "--init")
  private boolean init;
```

## Templating
Commands which return large texts or much content should allow templating. 
This can be achieved by using the TemplateRenderer.
If you inject the TemplateRenderer you must annotate it as a Mixin:
```java
  @CommandLine.Mixin
  private final TemplateRenderer templateRenderer;
```

### Table
Besides "loose" templates, you can use a table-like template to render your output. 
For this purpose use the TemplateRender and create table first. 
Then add your table headers and rows. 

```java
      Table table = templateRenderer.createTable();
      table.addHeader("repoName", "repoType", "repoUrl");
      for (RepositoryCommandDto dto : dtos) {
        table.addRow(dto.getNamespace() + "/" + dto.getName(), dto.getType(), dto.getUrl());
      }
      templateRenderer.renderToStdout(TABLE_TEMPLATE, ImmutableMap.of("rows", table, "repos", dtos));
```

#### Result
```shell
    NAME                   TYPE URL 
    scmadmin/nice_repo     git  http://localhost:8081/scm/repo/scmadmin/nice_repo    
```

### Key/Value Table
To create a two column (key-value-style) table you can use the `addKeyValueRow()` method.

```java
    Table table = createTable();
    RepositoryCommandDto dto = mapper.map(repository);
    table.addLabelValueRow("repoNamespace", dto.getNamespace());
    table.addLabelValueRow("repoName", dto.getName());
    table.addLabelValueRow("repoType", dto.getType());
    table.addLabelValueRow("repoContact", dto.getContact());
    table.addLabelValueRow("repoUrl", dto.getUrl());
    table.addLabelValueRow("repoDescription", dto.getDescription());
    renderToStdout(DETAILS_TABLE_TEMPLATE, ImmutableMap.of("rows", table, "repo", dto));
```

#### Result

```shell
Namespace: scmadmin                                        
Name     : testrepo                                        
Type     : git     
```

## I18n
The CLI client commands should support multiple languages. 
This can be done by using translation keys in the related resource bundles. 
By default, we support English and German translations.

### Example
```java
  static final String DEFAULT_TEMPLATE = String.join("\n",
    "{{repo.namespace}}/{{repo.name}}",
    "{{i18n.repoDescription}}: {{repo.description}}",
    "{{i18n.repoType}}: {{repo.type}}",
    "{{i18n.repoContact}}: {{repo.contact}}"
  );
```

The variables starting with `i18n` are translations from the resource bundles. 
The fields starting with `repo` are context related model data from the repository we are currently accessing.

## Error handling
There are different options on how to handle errors. 
You can use the TemplateRender and print the errors or exception messages to stderr channel.

However, you also can throw an exception directly inside your execution. 
These exceptions will be handled by the CliExceptionHandler and will be printed to the stderr channel based on a specific template.

## Validation
The CLI commands support Java bean validation. 
If you want to use this validation you have to inject the CommandValidator and call `validator.validate()` in the first line of the command execution.
Then you can simply annotate your fields with validation annotations.

### Example
```java
  @Email
  @CommandLine.Option(names = {"--contact", "-c"})
  private String contact;
```

```java
  @Inject
  public MyCommand(CommandValidator validator) {
    this.validator = validator;
  }

@Override
public void run() {
  validator.validate();
  ...
}
