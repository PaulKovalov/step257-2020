package com.google.sticknotesbackend.servlets;
import static com.googlecode.objectify.ObjectifyService.ofy;

import com.google.gson.Gson;
import com.google.sticknotesbackend.models.Note;
import com.google.sticknotesbackend.models.User;
import com.google.sticknotesbackend.models.Whiteboard;
import com.googlecode.objectify.Ref;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that handles Note logic
 * Implements methods:
 * * POST  - create note
 */
@WebServlet("api/notes/")
public class NoteServlet extends NoteAbstractServlet {
  @Override
  public void init() throws ServletException {
    // set the list of required fields for this servlet that will be checked in validateRequestData method
    requiredFields.add("content");
    requiredFields.add("boardId");
    requiredFields.add("color");
    requiredFields.add("x");
    requiredFields.add("y");
  }
  /**
   * The expected payload is
    boardId: id of the board
    content: the content of the note;
    image: url / base64 (not decided yet);
    color: string;
    x: the coordinate
    y: another coordinate
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // create gson parser that uses custom note serializer
    Gson gson = getNoteGsonParser();
    Note note = gson.fromJson(request.getReader(), Note.class);
    validateRequestData(note, response);
    // fill the remaining note data
    note.setCreator(new User("randomkey", "googler", "googler@google.com"));
    note.creationDate = System.currentTimeMillis();
    // save the note and set id
    note.id = ofy().save().entity(note).now().getId();
    // get the board of the note
    Whiteboard board = ofy().load().type(Whiteboard.class).id(note.boardId).now();
    // add reference to the note at this board
    board.notes.add(Ref.create(note));
    ofy().save().entity(board).now();
    // return the note
    response.getWriter().println(gson.toJson(note));
    // set 204 created status codes
    response.setStatus(CREATED);
  }
  /**
   * Deletes the note with the given id
   */
  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // get note id from the request
    String noteIdParam = request.getParameter("id");
    if (noteIdParam != null) {
      Long noteId = Long.parseLong(noteIdParam);
      // get note that is going to be deleted
      Note note = ofy().load().type(Note.class).id(noteId).now();
      // get note board
      Whiteboard noteBoard = ofy().load().type(Whiteboard.class).id(note.boardId).now();
      // remove note from the list of noteBoard notes
      noteBoard.notes.removeIf(noteRef -> noteRef.get().id == note.id);
      // update noteBoard
      ofy().save().entity(noteBoard).now();
      // delete note from datastore
      ofy().delete().type(Note.class).id(noteId).now();
      // delete note from board
      // return no content
      response.setStatus(NO_CONTENT);
    } else {
      response.getWriter().println("No id set");
      response.sendError(BAD_REQUEST);
    }
  }
}
