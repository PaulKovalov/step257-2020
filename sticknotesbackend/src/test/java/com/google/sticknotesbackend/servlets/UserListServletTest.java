package com.google.sticknotesbackend.servlets;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import com.google.gson.JsonElement;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.sticknotesbackend.enums.Role;
import com.google.sticknotesbackend.models.User;
import com.google.sticknotesbackend.models.UserBoardRole;
import com.google.sticknotesbackend.models.Whiteboard;
import java.io.BufferedReader;

import org.junit.Before;
import org.junit.Test;

public class UserListServletTest extends NotesboardTestBase {

  UserBoardRole userBoardRole1;
  UserBoardRole userBoardRole2;
  UserBoardRole userBoardRole3;
  UserBoardRole userBoardRole4;
  UserBoardRole userBoardRole5;
  UserBoardRole userBoardRole6;

  User user1;
  User user2;
  User user3;
  User user4;

  Whiteboard board1;
  Whiteboard board2;
  Whiteboard board3;

  private UserListServlet userListServlet;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    clearDatastore();
    ofy().clear(); //clearing Objectify cache
    // filling datastore with board and few users
    user1 = new User("user1@google.com", "user1");
    user2 = new User("user2@google.com", "user2");
    user3 = new User("user3@google.com", "user3");
    user4 = new User("user4@google.com", "user4");

    board1 = new Whiteboard("title1");
    board2 = new Whiteboard("title2");
    board3 = new Whiteboard("title3");

    ofy().save().entity(user1).now();
    ofy().save().entity(user2).now();
    ofy().save().entity(user3).now();
    ofy().save().entity(user4).now();

    ofy().save().entity(board1).now();
    ofy().save().entity(board2).now();
    ofy().save().entity(board3).now();

    userBoardRole1 = new UserBoardRole(Role.ADMIN, board1, user1);
    userBoardRole2 = new UserBoardRole(Role.ADMIN, board1, user2);
    userBoardRole3 = new UserBoardRole(Role.USER, board1, user3);
    userBoardRole4 = new UserBoardRole(Role.USER, board1, user4);
    userBoardRole5 = new UserBoardRole(Role.USER, board2, user3);
    userBoardRole6 = new UserBoardRole(Role.USER, board2, user4);

    ofy().save().entity(userBoardRole1).now();
    ofy().save().entity(userBoardRole2).now();
    ofy().save().entity(userBoardRole3).now();
    ofy().save().entity(userBoardRole4).now();
    ofy().save().entity(userBoardRole5).now();
    ofy().save().entity(userBoardRole6).now();

    ofy().clear(); //after loading data to datastore clearing cache once again

    userListServlet = new UserListServlet();

    when(mockRequest.getContentType()).thenReturn("application/json");
    // Set up a fake HTTP response
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
  }

  @Test
  public void getBoardNotAuthorized() throws IOException {
    userListServlet.doGet(mockRequest, mockResponse);
    // verify response status
    verify(mockResponse).sendError(UNAUTHORIZED);
  }

  @Test
  public void testBoard1Key() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    when(mockRequest.getParameter("id")).thenReturn(board1.id.toString());

    userListServlet.doGet(mockRequest, mockResponse);

    //preparing expected response based on dataset initialized in datastore
    Gson gson = userListServlet.getBoardRoleGsonParser();
    JsonArray expectedResponse = new JsonArray();
    expectedResponse.add(gson.toJsonTree(userBoardRole1));
    expectedResponse.add(gson.toJsonTree(userBoardRole2));
    expectedResponse.add(gson.toJsonTree(userBoardRole3));
    expectedResponse.add(gson.toJsonTree(userBoardRole4));
    
    // veryfing response
    verify(mockResponse).setContentType("application/json");
    verify(mockResponse).setStatus(OK);

    JsonArray actualResponse = gson.fromJson(responseWriter.getBuffer().toString(), JsonArray.class);
    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void testBoard2Key() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    when(mockRequest.getParameter("id")).thenReturn(board2.id.toString());

    userListServlet.doGet(mockRequest, mockResponse);

    //preparing expected response based on dataset initialized in datastore
    Gson gson = userListServlet.getBoardRoleGsonParser();
    JsonArray expectedResponse = new JsonArray();
    expectedResponse.add(gson.toJsonTree(userBoardRole5));
    expectedResponse.add(gson.toJsonTree(userBoardRole6));

    // veryfing status
    verify(mockResponse).setContentType("application/json");
    verify(mockResponse).setStatus(OK);

    JsonArray actualResponse = gson.fromJson(responseWriter.getBuffer().toString(), JsonArray.class);
    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void testNotExistingBoard() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    Long boardId = (long) -1;

    when(mockRequest.getParameter("id")).thenReturn(boardId.toString());

    userListServlet.doGet(mockRequest, mockResponse);

    // veryfing status
    verify(mockResponse).sendError(BAD_REQUEST);
  }

  @Test
  public void testBoardExistsButNoUsers() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    when(mockRequest.getParameter("id")).thenReturn(board3.id.toString());

    userListServlet.doGet(mockRequest, mockResponse);

    Gson gson = userListServlet.getBoardRoleGsonParser();
    JsonArray expectedResponse = new JsonArray();

    // veryfing status
    verify(mockResponse).setContentType("application/json");
    verify(mockResponse).setStatus(OK);

    JsonArray actualResponse = gson.fromJson(responseWriter.getBuffer().toString(), JsonArray.class);
    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void testAddUserNotAllowedUserAddsUser() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    //add user as USER of the board to not allow adding other USER user
    UserBoardRole userRole = new UserBoardRole(Role.USER, board2, user);
    ofy().save().entity(userRole);

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("email", user1.email);
    jsonObject.addProperty("role", "user");

    when(mockRequest.getParameter("id")).thenReturn(board2.id.toString());
    when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonObject.toString())));

    userListServlet.doPost(mockRequest, mockResponse);

    // verify response status
    verify(mockResponse).sendError(FORBIDDEN);
  }

  @Test
  public void testAddUserNotAllowedUserAddsAdmin() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    //add user as USER of the board to not allow adding other ADMIN user
    UserBoardRole userRole = new UserBoardRole(Role.USER, board2, user);
    ofy().save().entity(userRole);

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("email", user1.email);
    jsonObject.addProperty("role", "admin");

    when(mockRequest.getParameter("id")).thenReturn(board2.id.toString());
    when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonObject.toString())));

    userListServlet.doPost(mockRequest, mockResponse);

    // verify response status
    verify(mockResponse).sendError(FORBIDDEN);
  }

  @Test
  public void testAddUserNotAllowedUserAddsOwner() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    //add user as USER of the board to not allow adding other OWNER user
    UserBoardRole userRole = new UserBoardRole(Role.USER, board2, user);
    ofy().save().entity(userRole);

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("email", user1.email);
    jsonObject.addProperty("role", "owner");

    when(mockRequest.getParameter("id")).thenReturn(board2.id.toString());
    when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonObject.toString())));

    userListServlet.doPost(mockRequest, mockResponse);

    // verify response status
    verify(mockResponse).sendError(FORBIDDEN);
  }

  @Test
  public void testAddUserNotAllowedAdminAddsAdmin() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    //add user as ADMIN of the board to not allow adding other ADMIN user
    UserBoardRole ownerRole = new UserBoardRole(Role.ADMIN, board2, user);
    ofy().save().entity(ownerRole);

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("email", user1.email);
    jsonObject.addProperty("role", "admin");

    when(mockRequest.getParameter("id")).thenReturn(board2.id.toString());
    when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonObject.toString())));

    userListServlet.doPost(mockRequest, mockResponse);

    // verify response status
    verify(mockResponse).sendError(FORBIDDEN);
  }

  @Test
  public void testAddUserNotAllowedAdminAddsOwner() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    //add user as ADMIN of the board to not allow adding other OWNER user
    UserBoardRole ownerRole = new UserBoardRole(Role.ADMIN, board2, user);
    ofy().save().entity(ownerRole);

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("email", user1.email);
    jsonObject.addProperty("role", "owner");

    when(mockRequest.getParameter("id")).thenReturn(board2.id.toString());
    when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonObject.toString())));

    userListServlet.doPost(mockRequest, mockResponse);

    // verify response status
    verify(mockResponse).sendError(FORBIDDEN);
  }

  @Test
  public void testOwnerAddUserToBoardUserExistsBoardExists() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    //we need to add user as OWNER of the board to allow adding other ADMIN user
    UserBoardRole ownerRole = new UserBoardRole(Role.OWNER, board2, user);
    ofy().save().entity(ownerRole);

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("email", user1.email);
    jsonObject.addProperty("role", "admin");

    when(mockRequest.getParameter("id")).thenReturn(board2.id.toString());
    when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonObject.toString())));

    userListServlet.doPost(mockRequest, mockResponse);


    Gson gson = userListServlet.getBoardRoleGsonParser();

    // checking response status
    verify(mockResponse).setStatus(OK);

    // checking if data correctly in the datastore
    ofy().clear();
    UserBoardRole datastoreData = ofy().load().type(UserBoardRole.class).filter("board", board2).filter("user", user1)
        .filter("role", Role.ADMIN).first().now();

    assertNotNull(datastoreData);

    // checking response value
    JsonElement expectedResponse = gson.toJsonTree(datastoreData, UserBoardRole.class);
    JsonElement actualResponse = gson.fromJson(responseWriter.getBuffer().toString(), JsonObject.class);
    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void testAdminAddUserToBoardUserExistsBoardExists() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    //we need to add user as OWNER of the board to allow adding other ADMIN user
    UserBoardRole ownerRole = new UserBoardRole(Role.ADMIN, board2, user);
    ofy().save().entity(ownerRole);

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("email", user1.email);
    jsonObject.addProperty("role", "user");

    when(mockRequest.getParameter("id")).thenReturn(board2.id.toString());
    when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonObject.toString())));

    userListServlet.doPost(mockRequest, mockResponse);


    Gson gson = userListServlet.getBoardRoleGsonParser();

    // checking response status
    verify(mockResponse).setStatus(OK);

    // checking if data correctly in the datastore
    ofy().clear();
    UserBoardRole datastoreData = ofy().load().type(UserBoardRole.class).filter("board", board2).filter("user", user1)
        .filter("role", Role.USER).first().now();

    assertNotNull(datastoreData);

    // checking response value
    JsonElement expectedResponse = gson.toJsonTree(datastoreData, UserBoardRole.class);
    JsonElement actualResponse = gson.fromJson(responseWriter.getBuffer().toString(), JsonObject.class);
    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void testAddUserToBoardUserAlreadyInTheBoardList() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);
    
    //we need to add user as OWNER of the board to allow adding other ADMIN user
    UserBoardRole ownerRole = new UserBoardRole(Role.OWNER, board1, user);
    ofy().save().entity(ownerRole);

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("email", user1.email);
    jsonObject.addProperty("role", "admin");

    when(mockRequest.getParameter("id")).thenReturn(board1.id.toString());
    when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonObject.toString())));

    userListServlet.doPost(mockRequest, mockResponse);

    // checking response status
    verify(mockResponse).sendError(BAD_REQUEST);
    // checking response value
    String actualResponse = responseWriter.getBuffer().toString();
    String expectedResponse = "User already in the list.\n";

    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void testAddUserToBoardUserExistsBoardNotExists() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    //we need to add user as OWNER of the board to allow adding other ADMIN user
    UserBoardRole ownerRole = new UserBoardRole(Role.OWNER, board1, user);
    ofy().save().entity(ownerRole);

    Long boardId = (long) -1;
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("email", user4.email);
    jsonObject.addProperty("role", Role.ADMIN.toString());

    when(mockRequest.getParameter("id")).thenReturn(boardId.toString());
    when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonObject.toString())));

    userListServlet.doPost(mockRequest, mockResponse);

    // checking response status
    verify(mockResponse).sendError(BAD_REQUEST);
    // checking response value
    String actualResponse = responseWriter.getBuffer().toString();
    String expectedResponse = "Board with a given id not found.\n";

    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void testAddUserToBoardUserNotExistsBoardExists() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    //we need to add user as OWNER of the board to allow adding other ADMIN user
    UserBoardRole ownerRole = new UserBoardRole(Role.OWNER, board1, user);
    ofy().save().entity(ownerRole);

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("email", "user6@google.com");
    jsonObject.addProperty("role", "admin");

    when(mockRequest.getParameter("id")).thenReturn(board1.id.toString());
    when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonObject.toString())));

    userListServlet.doPost(mockRequest, mockResponse);

    // checking response status
    verify(mockResponse).setStatus(OK);
    // checking response value
    /**
     * will later implement whole test
     */
  }

  @Test
  public void testAddUserToBoardUserNotExistsBoardNotExists() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    Long boardId = (long) -1;

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("email", "user6@google.com");
    jsonObject.addProperty("role", "admin");

    when(mockRequest.getParameter("id")).thenReturn(boardId.toString());
    when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonObject.toString())));

    userListServlet.doPost(mockRequest, mockResponse);

    // checking response status
    verify(mockResponse).sendError(BAD_REQUEST);
    // checking response value
    String actualResponse = responseWriter.getBuffer().toString();
    String expectedResponse = "Board with a given id not found.\n";

    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void testDeleteRoleExists() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    //we need to add user as OWNER of the board to allow adding other ADMIN user
    UserBoardRole ownerRole = new UserBoardRole(Role.OWNER, board1, user);
    ofy().save().entity(ownerRole);

    when(mockRequest.getParameter("id")).thenReturn(userBoardRole1.id.toString());

    userListServlet.doDelete(mockRequest, mockResponse);

    // checking response status
    verify(mockResponse).setStatus(OK);

    UserBoardRole datastoreData = ofy().load().type(UserBoardRole.class).id(userBoardRole1.id).now();

    assertNull(datastoreData);
  }

  @Test
  public void testDeleteRoleNotExists() throws IOException {
    // creating mock user and log-in
    User user = new User("googler@google.com", "nick");
    user.googleAccId = "10";
    ofy().save().entity(user).now();
    logIn(user);

    //we need to add user as OWNER of the board to allow adding other ADMIN user
    UserBoardRole ownerRole = new UserBoardRole(Role.OWNER, board1, user);
    ofy().save().entity(ownerRole);

    Long roleId = (long) -1;
    when(mockRequest.getParameter("id")).thenReturn(roleId.toString());

    userListServlet.doDelete(mockRequest, mockResponse);
    // checking response status
    verify(mockResponse).sendError(BAD_REQUEST);
    // checking response value
    String actualResponse = responseWriter.getBuffer().toString();
    String expectedResponse = "Role with a given id not found.\n";

    assertEquals(expectedResponse, actualResponse);
  }
}
