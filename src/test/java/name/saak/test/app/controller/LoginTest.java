package name.saak.test.app.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LoginTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void loginPageIsAccessibleWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"));
	}

	@Test
	void protectedPagesRedirectToLoginWhenNotAuthenticated() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrlPattern("**/login"));
	}

	@Test
	void loginWithAdminAndGeheimSucceeds() throws Exception {
		mockMvc.perform(formLogin("/login")
						.user("admin")
						.password("geheim"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(authenticated().withUsername("admin"));
	}

	@Test
	void loginWithWrongPasswordFails() throws Exception {
		mockMvc.perform(formLogin("/login")
						.user("admin")
						.password("falsch"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());
	}

	@Test
	void loginWithWrongUsernameFails() throws Exception {
		mockMvc.perform(formLogin("/login")
						.user("hacker")
						.password("geheim"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());
	}
}
