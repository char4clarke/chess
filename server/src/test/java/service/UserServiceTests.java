package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTests {
    private MemoryAuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    public void init() throws DataAccessException {
        MemoryUserDAO userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userDAO.clear();
        authDAO.clear();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    @Order(1)
    @DisplayName("Register User (Positive)")
    public void registerUserPositive() {
        UserService.RegisterRequest request = new UserService.RegisterRequest("username", "password", "email");
        UserService.RegisterResult result = userService.register(request);

        Assertions.assertEquals("username", result.username());
        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals("Success", result.message());
    }

    @Test
    @Order(2)
    @DisplayName("Register User (Negative)")
    public void registerUserNegative() {
        UserService.RegisterRequest request = new UserService.RegisterRequest(null, "password", "email");
        UserService.RegisterResult result = userService.register(request);
        Assertions.assertEquals("Error: Invalid input", result.message());

        request = new UserService.RegisterRequest("username", null, "email");
        result = userService.register(request);
        Assertions.assertEquals("Error: Invalid input", result.message());

        request = new UserService.RegisterRequest("username", "password", null);
        result = userService.register(request);
        Assertions.assertEquals("Error: Invalid input", result.message());

        request = new UserService.RegisterRequest("username", "password", "email");
        result = userService.register(request);
        UserService.RegisterResult result1 = userService.register(request);
        Assertions.assertNull(result1.username());


    }


    @Test
    @Order(3)
    @DisplayName("Login (Positive)")
    public void loginPositive() {
        UserService.RegisterRequest registerRequest = new UserService.RegisterRequest("username", "password", "email");
        userService.register(registerRequest);

        UserService.LoginRequest loginRequest = new UserService.LoginRequest("username", "password");
        UserService.LoginResult loginResult = userService.login(loginRequest);

        Assertions.assertEquals("username", loginResult.username());
        Assertions.assertNotNull(loginResult.authToken(), "Auth token should be generated");
        Assertions.assertEquals("Success", loginResult.message());
    }

    @Test
    @Order(4)
    @DisplayName("Login (Negative)")
    public void loginNegative() {
        UserService.LoginRequest loginRequest = new UserService.LoginRequest("username", "password");
        UserService.LoginResult loginResult = userService.login(loginRequest);

        Assertions.assertEquals("Error: unauthorized", loginResult.message());
        Assertions.assertNull(loginResult.username());
        Assertions.assertNull(loginResult.authToken());

        UserService.RegisterRequest registerRequest = new UserService.RegisterRequest("username", "pasword", "email");
        userService.register(registerRequest);

        loginRequest = new UserService.LoginRequest("username", "incorrect");
        loginResult = userService.login(loginRequest);

        Assertions.assertEquals("Error: unauthorized", loginResult.message());
        Assertions.assertNull(loginResult.username());
        Assertions.assertNull(loginResult.authToken());
    }


    @Test
    @Order(5)
    @DisplayName("Logout (Positive)")
    public void logoutPositive() throws DataAccessException {
        UserService.RegisterRequest registerRequest = new UserService.RegisterRequest("username", "password", "email");
        userService.register(registerRequest);
        UserService.LoginRequest loginRequest = new UserService.LoginRequest("username", "password");
        UserService.LoginResult loginResult = userService.login(loginRequest);
        String token = loginResult.authToken();
        Assertions.assertNotNull(token);

        UserService.LogoutRequest logoutRequest = new UserService.LogoutRequest(token);
        userService.logout(logoutRequest);
        AuthData auth = authDAO.getAuth(token);
        Assertions.assertNull(auth);
    }
}
