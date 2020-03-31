# scm-mail-plugin

The mail plugin provides an central api for sending e-mails. This api
can be used by other plugins.

## Configuration

The scm-mail-plugin provides a single place for the mail server
configurations at Config-\>General-\>Mail Settings.

## API Usage

First you have to add the dependency to your pom.xml e.g.:

```xml
<dependency>
  <groupId>sonia.scm.plugins</groupId>
  <artifactId>scm-mail-plugin</artifactId>
  <version>1.4</version>
</dependency>
```

But note you should use at least version 1.15 of scm-plugins as parent.

Now you can use the MailService class via injection e.g.:

```java
import com.google.inject.Inject;
import javax.mail.Message.RecipientType;
import org.codemonkey.simplejavamail.Email;
import sonia.scm.mail.api.MailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for e-mail notifications.
 * @author Sebastian Sdorra
 */
public class NotificationService
{
  /** logger for NotificationService */
  private static final Logger logger = LoggerFactory.getLogger(
    NotificationService.class);
  
  private MailService mailService;

  /**
   * Constructs a new NotificationService.
   * 
   * @param mailService injected mail service
   */
  @Inject
  public NotificationService(MailService mailService)
  {
    this.mailService = mailService;
  }
  
  /**
   * Sends a mail from admin@scm-manager.org to test.user@scm-manager.org,
   * if the mail service is configured.
   * 
   * @throws MailSendBatchException 
   */
  public void sendNotification() throws MailSendBatchException {
    // check if the mail service is configured
    if ( mailService.isConfigured() ){
      // create a new e-mail
      Email mail = new Email();
      mail.setFromAddress("SCM-Administrator", "admin@scm-manager.org");
      mail.addRecipient("Test User", "test.user@scm-manager.org", RecipientType.TO);
      mail.setSubject("SCM-Manager notification");
      mail.setText("Notification from SCM-Manager");
      
      // send the e-mail
      mailService.send(mail);
    } else {
      // log that the mail service is not configured
      logger.warn("mail service is not configured");
    }
  }
}
```
