package com.myblogbackend.blog.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblogbackend.blog.IntegrationTestUtil;
import com.myblogbackend.blog.models.RefreshTokenEntity;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.repositories.RefreshTokenRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.request.LoginFormRequest;
import com.myblogbackend.blog.response.JwtResponse;
import com.myblogbackend.blog.security.JwtProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;
import java.util.UUID;

import static com.myblogbackend.blog.ResponseBodyMatcher.responseBody;
import static com.myblogbackend.blog.login.LoginTestApi.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LoginApiDelegateImplTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JwtProvider jwtProvider;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UsersRepository usersRepository;
    @MockBean
    private RefreshTokenRepository refreshTokenRepository;
    private LoginFormRequest loginDataRequest;
    private UUID userId;
    private String jwtToken;
    private String password;
    private long expirationDuration;
    private RefreshTokenEntity refreshToken;
    private UserEntity userEntity;
    private JwtResponse jwtResponse;

    @BeforeEach
    public void setupContext() {
        SecurityContextHolder.clearContext();
    }

    @Before
    public void setup() {
        userId = UUID.randomUUID();
        loginDataRequest = loginDataForRequesting();
        jwtToken = mockJwtToken();
        password = mockPassword();
        expirationDuration = 3600000L;
        userEntity = userEntityForSaving(userId, passwordEncoder.encode(password));
        refreshToken = createRefreshTokenEntity(loginDataRequest, userEntity);
        jwtResponse = jwtResponseForSaving(jwtToken, refreshToken.getToken(), expirationDuration);
    }

    @Test
    public void givenLoginData_whenSendData_thenReturnsJwtResponseCreated() throws Exception {
        //mock user login data
        //mock user entity
        Mockito.when(usersRepository.findByEmail(loginDataRequest.getEmail())).thenReturn(Optional.of(userEntity));
        //create UsernamePasswordAuthenticationToken
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                loginDataRequest.getEmail(),
                loginDataRequest.getPassword());
        //mock authentication manager
        Mockito.when(authenticationManager.authenticate(authentication)).thenReturn(authentication);
        //set the authentication to SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //create the Jwt token from JwtProvider
        Mockito.when(jwtProvider.generateJwtToken(authentication)).thenReturn(jwtToken);
        //create the refresh token
        Mockito.when(refreshTokenRepository.save(Mockito.any())).thenReturn(refreshToken);
        //create expiration from jwt provider
        Mockito.when(jwtProvider.getExpiryDuration()).thenReturn(expirationDuration);
        //create jwt response after login successfully
        //test login api
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/signin")
                        .content(IntegrationTestUtil.asJsonString(loginDataRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.accessToken", is(jwtToken)))
                .andExpect(jsonPath("$.refreshToken", is(refreshToken.getToken())))
                .andExpect(jsonPath("$.expiryDuration").value((int) expirationDuration))
                .andExpect(responseBody().containsObjectBody(jwtResponse, JwtResponse.class, objectMapper));
    }


    @Test
    public void givenLoginDataV2_whenSendData_thenReturnsJwtResponseCreated() throws Exception {
//        // create user and save to database
//        UUID userId = UUID.randomUUID();
//        UserEntity userEntity = userEntityForSaving(userId, passwordEncoder.encode("123456"));
//        var userSignInRequest = loginDataForRequesting();
//
//        // Mock findByEmail
//        Mockito.when(usersRepository.findByEmail(userSignInRequest.getEmail())).thenReturn(Optional.of(userEntity));
//
//        // Mock authentication result
//        UsernamePasswordAuthenticationToken authentication =
//                new UsernamePasswordAuthenticationToken(userSignInRequest.getEmail(), userSignInRequest.getPassword());
//
//        // Mock authentication manager
//        Mockito.when(authenticationManager.authenticate(authentication)).thenReturn(authentication);
//
//        // Set the authentication in SecurityContextHolder
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        String jwtToken = "mockJwtToken";
//        // Mock JWT provider
//        Mockito.when(jwtProvider.generateJwtToken(authentication)).thenReturn(jwtToken);
//
//        // Mock and return a non-null refreshToken
//        RefreshTokenEntity refreshToken = refreshTokenForSaving();
//
//        Mockito.when(refreshTokenRepository.save(Mockito.any())).thenReturn(refreshToken);
//
//        // Set the expiry duration to 3600000 milliseconds (1 hour)
//        long expiryDuration = 3600000L;
//        Mockito.when(jwtProvider.getExpiryDuration()).thenReturn(expiryDuration);
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/signin")
//                        .content(IntegrationTestUtil.asJsonString(userSignInRequest))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken", is(jwtToken)))
//                .andExpect(jsonPath("$.refreshToken", is(refreshToken.getToken())))
//                .andExpect(jsonPath("$.expiryDuration").value((int) expiryDuration));
//    }
    }
}
