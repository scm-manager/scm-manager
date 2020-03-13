# scm-scheduler-plugin #

The scheduler plugin provides an api for other plugins to execute scheduled jobs. The plugin is based on the [quartz project](http://quartz-scheduler.org/).

## Configuration ##

The plugin needs no further configuration.

## API Usage ##

First you have to add the dependency to your pom.xml e.g.:

```
#!xml
<dependency>
  <groupId>sonia.scm.plugins</groupId>
  <artifactId>scm-scheduler-plugin</artifactId>
  <version>1.0</version>
</dependency>
```

But note you should use at least version 1.22 of scm-plugins as parent for your plugin.

Now you can get a [SchedulerFactory](http://www.quartz-scheduler.org/api/2.0.0/org/quartz/SchedulerFactory.html) via injection. 

For Example:


```
#!java

public class ReportHandler {
  
  private final SchedulerFactory schedulerFactory;
 
  @Inject
  public ReportContextListener(SchedulerFactory schedulerFactory){
    this.schedulerFactory = schedulerFactory;
  }

}
```

With the SchedulerFactory [SchedulerFactory](http://www.quartz-scheduler.org/api/2.0.0/org/quartz/SchedulerFactory.html) you can schedule [Jobs](http://www.quartz-scheduler.org/api/2.0.0/org/quartz/Job.html). To simplify the steps to schedule a job, the scm-scheduler-plugin comes with a helper class called Schedulers.

### Notes ###

* Schedulers must always be executed by an administrator or in an administrative context. Jobs which are executed by a scheduler are executed with administrator privileges. 
* Each job is able to get dependencies via injection.

### Further reading ###

* [Quartz Overview](http://www.quartz-scheduler.org/overview)
* [Quartz Cron Trigger](http://www.quartz-scheduler.org/documentation/quartz-2.1.x/tutorials/crontrigger)
* [Quartz Examples](http://www.quartz-scheduler.org/documentation/quartz-2.1.x/examples)

### Complete Example ###

In the following example a ReportJob is executed every minute since the start of scm-manager.


```
#!java

@Extension
public class ReportContextListener implements ServletContextListener {
  
  private final AdministrationContext adminContext;
  private final SchedulerFactory schedulerFactory;
 
  @Inject
  public ReportContextListener(AdministrationContext adminContext,
                               SchedulerFactory schedulerFactory){
    this.adminContext = adminContext;
    this.schedulerFactory = schedulerFactory;
  }
 
  @Override
  public void contextDestroyed(ServletContextEvent sce) {}
 
  @Override
  public void contextInitialized(ServletContextEvent sce){
    Schedulers.schedule(adminContext, schedulerFactory, 
      SimpleScheduleBuilder.repeatMinutelyForever(), ReportJob.class);
  }
  
  private static class ReportJob implements Job {
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
      // do something minutely
      System.out.println(new Date());
    }
  }
}
```