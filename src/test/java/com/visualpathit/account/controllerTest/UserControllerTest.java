package com.visualpathit.account.controllerTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import com.visualpathit.account.controller.UserController;
import com.visualpathit.account.model.User;
import com.visualpathit.account.service.SecurityService;
import com.visualpathit.account.service.UserService;
import com.visualpathit.account.setup.StandaloneMvcTestViewResolver;




public class UserControllerTest {
	
	@Mock
	private UserService controllerSer;
	@Mock
	private SecurityService securityService;
	@InjectMocks
	private UserController controller;
	private MockMvc mockMvc;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		
		/*InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");
		*/
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				  .setViewResolvers(new StandaloneMvcTestViewResolver()).build();
	}
	
	@Test
	public void registrationTestforHappyFlow() throws Exception{
		User user = new User();
		mockMvc.perform(get("/registration"))
        .andExpect(status().isOk())
        .andExpect(view().name("registration"))
        .andExpect(forwardedUrl("registration"));
		
	}
	@Test
	public void registrationTestforNullValueHappyFlow() throws Exception{
		mockMvc.perform(get("/registration"))
        .andExpect(status().isOk())
        .andExpect(view().name("registration"))
        .andExpect(forwardedUrl("registration"));
		
	}
	/*@Test
	public void registrationTestforPostValueHappyFlow() throws Exception{
		String description =new String("Error String");
		UserValidator userValidator;
		BindingResult bindingResult;
		when(userValidator.validate(new User(),bindingResult))
		.thenThrow(bindingResult.hasErrors());
		mockMvc.perform(post("/registration").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("userForm","userForm"))
		
        .andExpect(status().isOk());
        //.andExpect(view().name("redirect:/welcome"))
        //.andExpect(forwardedUrl("redirect:/welcome"));
		
	}*/
	@Test
	public void loginPageTestHappyFlow() throws Exception{
		mockMvc.perform(get("/").param("error", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("login"))
        .andExpect(forwardedUrl("login"));
		
	}
	@Test
	public void welcomeTestHappyFlow() throws Exception{
		mockMvc.perform(get("/welcome"))
        .andExpect(status().isOk())
        .andExpect(view().name("welcome"))
        .andExpect(forwardedUrl("welcome"));
		
	}
	@Test
	public void welcomeAfterDirectLoginTestHappyFlow() throws Exception{
		when(securityService.autologin("demo", "secret")).thenReturn(true);
		mockMvc.perform(post("/login")
				.param("username", "demo")
				.param("password", "secret"))
		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("/welcome"));
		
	}
	@Test
	public void indexTestHappyFlow() throws Exception{
		mockMvc.perform(get("/index"))
        .andExpect(status().isOk())
        .andExpect(view().name("index_home"))
        .andExpect(forwardedUrl("index_home"));
		
	}

	private static List<User> users(int count, long firstId) {
		List<User> list = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			User user = new User();
			user.setId(firstId + i);
			user.setUsername("user" + (firstId + i));
			list.add(user);
		}
		return list;
	}

	@Test
	public void usersListShowsFirstPageByDefault() throws Exception{
		when(controllerSer.getPage(0, 100)).thenReturn(
				new PageImpl<>(users(100, 1), PageRequest.of(0, 100), 5000));
		mockMvc.perform(get("/users"))
        .andExpect(status().isOk())
        .andExpect(view().name("userList"))
        .andExpect(model().attribute("currentPage", 1))
        .andExpect(model().attribute("totalPages", 50))
        .andExpect(model().attribute("totalUsers", 5000L))
        .andExpect(model().attribute("firstUser", 1L))
        .andExpect(model().attribute("lastUser", 100L));
		verify(controllerSer).getPage(0, 100);
	}

	@Test
	public void usersListShowsRequestedPage() throws Exception{
		when(controllerSer.getPage(2, 100)).thenReturn(
				new PageImpl<>(users(100, 201), PageRequest.of(2, 100), 5000));
		mockMvc.perform(get("/users").param("page", "3"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("currentPage", 3))
        .andExpect(model().attribute("firstUser", 201L))
        .andExpect(model().attribute("lastUser", 300L));
	}

	@Test
	public void usersListClampsPageBelowOne() throws Exception{
		when(controllerSer.getPage(0, 100)).thenReturn(
				new PageImpl<>(users(10, 1), PageRequest.of(0, 100), 10));
		mockMvc.perform(get("/users").param("page", "0"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("currentPage", 1))
        .andExpect(model().attribute("lastUser", 10L));
		verify(controllerSer).getPage(0, 100);
	}

	@Test
	public void usersListBeyondLastPageIsEmpty() throws Exception{
		when(controllerSer.getPage(98, 100)).thenReturn(
				new PageImpl<>(Collections.emptyList(), PageRequest.of(98, 100), 5000));
		mockMvc.perform(get("/users").param("page", "99"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("currentPage", 99))
        .andExpect(model().attribute("totalPages", 50))
        .andExpect(model().attribute("firstUser", 0L))
        .andExpect(model().attribute("lastUser", 0L));
	}

}
