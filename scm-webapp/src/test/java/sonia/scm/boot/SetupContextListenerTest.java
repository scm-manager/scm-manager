package sonia.scm.boot;

import com.google.common.collect.Lists;
import org.apache.shiro.authc.credential.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserTestData;
import sonia.scm.web.security.AdministrationContext;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SetupContextListenerTest {

  @Mock
  private AdministrationContext administrationContext;

  @InjectMocks
  private SetupContextListener setupContextListener;

  @Mock
  private UserManager userManager;

  @Mock
  private PasswordService passwordService;

  @Mock
  private PermissionAssigner permissionAssigner;

  @InjectMocks
  private SetupContextListener.SetupAction setupAction;

  @BeforeEach
  void setupObjectUnderTest() {
    doAnswer(ic -> {
      setupAction.run();
      return null;
    }).when(administrationContext).runAsAdmin(SetupContextListener.SetupAction.class);
  }

  @Test
  void shouldCreateAdminAccountAndAssignPermissions() {
    when(passwordService.encryptPassword("scmadmin")).thenReturn("secret");

    setupContextListener.contextInitialized(null);

    verifyAdminCreated();
    verifyAdminPermissionsAssigned();
  }

  @Test
  void shouldDoNothingOnSecondStart() {
    List<User> users = Lists.newArrayList(UserTestData.createTrillian());
    when(userManager.getAll()).thenReturn(users);

    setupContextListener.contextInitialized(null);

    verify(userManager, never()).create(any(User.class));
    verify(permissionAssigner, never()).setPermissionsForUser(anyString(), any(Collection.class));
  }

  private void verifyAdminPermissionsAssigned() {
    ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Collection<PermissionDescriptor>> permissionCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(permissionAssigner).setPermissionsForUser(usernameCaptor.capture(), permissionCaptor.capture());
    String username = usernameCaptor.getValue();
    assertThat(username).isEqualTo("scmadmin");
    PermissionDescriptor descriptor = permissionCaptor.getValue().iterator().next();
    assertThat(descriptor.getValue()).isEqualTo("*");
  }

  private void verifyAdminCreated() {
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userManager).create(userCaptor.capture());
    User user = userCaptor.getValue();
    assertThat(user.getName()).isEqualTo("scmadmin");
    assertThat(user.getPassword()).isEqualTo("secret");
  }

}
