package main.Controller;

import Controller.UserController;
import Server.Request;
import Server.Response;
import Models.User;
import Database.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserRepository mockUserRepository;
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockUserRepository = Mockito.mock(UserRepository.class);
        userController = new UserController();
    }

    @Test
    void login_shouldReturn200WhenCredentialsAreCorrect() throws Exception {
        String username = "kienboec";
        String password = "daniel";
        User mockUser = new User(username, password);
        when(mockUserRepository.findByUsername(username)).thenReturn(mockUser);

        Request mockRequest = new Request();
        mockRequest.setBody("{\"Username\":\"" + username + "\",\"Password\":\"" + password + "\"}");

        Response response = userController.login(mockRequest);

        assertEquals(200, response.getStatusCode());
        assertEquals("User login successful", response.getBody());
    }

    @Test
    void login_shouldReturn200WhenCredentialsAreCorrect2() throws Exception {
        String username = "altenhof";
        String password = "markus";
        User mockUser = new User(username, password);
        when(mockUserRepository.findByUsername(username)).thenReturn(mockUser);

        Request mockRequest = new Request();
        mockRequest.setBody("{\"Username\":\"" + username + "\",\"Password\":\"" + password + "\"}");

        Response response = userController.login(mockRequest);

        assertEquals(200, response.getStatusCode());
        assertEquals("User login successful", response.getBody());
    }


    @Test
    void login_shouldReturn401WhenCredentialsAreIncorrect() throws Exception {
        String username = "kienboec";
        String password = "wrongoassword";
        User mockUser = new User(username, "daniel");
        when(mockUserRepository.findByUsername(username)).thenReturn(mockUser);

        Request mockRequest = new Request();
        mockRequest.setBody("{\"Username\":\"" + username + "\",\"Password\":\"" + password + "\"}");

        Response response = userController.login(mockRequest);

        assertEquals(401, response.getStatusCode());
        assertEquals("Login failed", response.getBody());
    }
}
