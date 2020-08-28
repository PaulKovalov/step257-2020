package com.google.sticknotesbackend.servlets;

import static com.google.common.truth.Truth.assertThat;
import static com.googlecode.objectify.ObjectifyService.ofy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.repackaged.com.google.gson.JsonElement;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.sticknotesbackend.enums.Role;
import com.google.sticknotesbackend.models.Note;
import com.google.sticknotesbackend.models.User;
import com.google.sticknotesbackend.models.UserBoardRole;
import com.google.sticknotesbackend.models.Whiteboard;
import com.googlecode.objectify.Ref;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import javax.servlet.ServletException;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for NoteServlet
 */
public class NoteServletTest extends NotesboardTestBase {
  private NoteServlet noteServlet;

  @Before
  public void setUp() throws Exception {
    // parent logic of setting up objectify
    super.setUp();
    // Set up a fake HTTP request
    when(mockRequest.getContentType()).thenReturn("application/json");
    // Set up a fake HTTP response
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
    noteServlet = new NoteServlet();
  }

  // @Test
  public void testNoteCreateSuccessWithValidPayload() throws IOException, ServletException {
    // create a mock board
    Whiteboard board = createBoard();
    User user = createUser();
    board.setCreator(user);
    // save updated board
    ofy().save().entity(board).now();
    createRole(board, user, Role.USER);
    // log user in
    logIn(user);
    // generate a payload
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("boardId", board.id);
    jsonObject.addProperty("color", "#000000");
    jsonObject.addProperty("content", "dummy content");
    jsonObject.addProperty("x", 1);
    jsonObject.addProperty("y", 2);
    // prepare mocked request
    when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonObject.toString())));
    // do request
    noteServlet.doPost(mockRequest, mockResponse);
    // check that note with the id from response is saved in the datastore
    // read Json from response to a Note object
    // check that response has id
    // if id is there, check if the note with this id is really saved
    JsonObject body = new JsonParser().parse(responseWriter.toString()).getAsJsonObject();
    assertThat(body.has("id")).isTrue();
    // load note with the given id from datastore
    Note savedNote = ofy().load().type(Note.class).id(Long.parseLong(body.get("id").getAsString())).now();
    assertThat(savedNote).isNotNull();
  }

  @Test
  public void testNoteDeleteSuccessWithValidBoardId() throws IOException, ServletException {
    // creating mock user and log-in
    User user = createUser();
    // log user in
    logIn(user);
    // create mocked note and save it
    Note note = createNote();
    // create mocked board and save it
    Whiteboard board = createBoard();
    board.notes.add(Ref.create(note));
    board.setCreator(user);
    note.setCreator(user);
    note.boardId = board.id;
    // save updated board
    ofy().save().entity(board).now();
    // save updated note
    ofy().save().entity(note).now();
    createRole(board, user, Role.ADMIN);
    // add note id to the request
    when(mockRequest.getParameter("id")).thenReturn(Long.toString(note.id));
    // do delete
    noteServlet.doDelete(mockRequest, mockResponse);
    // verify that no note with this id is in the datastore
    Note deletedNote = ofy().load().type(Note.class).id(note.id).now();
    assertThat(deletedNote).isNull();
    verify(mockResponse).setStatus(NO_CONTENT);
  }
}
