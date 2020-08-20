/**
 * Notesboard
 * Board API servlet
 */
package com.google.sticknotesbackend.servlets;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.sticknotesbackend.models.User;
import com.google.sticknotesbackend.models.Whiteboard;
import com.google.sticknotesbackend.serializers.WhiteboardSerializer;
import com.googlecode.objectify.Key;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implements the following endpoints:
 * POST - create a board
 * GET with id - retrieve a board
 * PATCH with id - edit a board
 */
@WebServlet("api/board/")
public class BoardServlet extends NotesboardAbstractServlet {

  // returns a board with the given id
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
  }

  // creates a new board
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Gson gson = getBoardGsonParser();
    Whiteboard board = gson.fromJson(req.getReader(), Whiteboard.class);
    board.creationDate = System.currentTimeMillis();
    board.setCreator(ofy().save().entity(new User("randomid", "googler@google.com", "nickname")).now());
    board.rows = 4;
    board.cols = 6;
    Key<Whiteboard> savedBoardKey = ofy().save().entity(board).now();
    board.id = savedBoardKey.getId();
    resp.getWriter().println(gson.toJson(board));
  }

  // edits a board
  @Override
  public void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
  }

  public Gson getBoardGsonParser() {
    GsonBuilder gson = new GsonBuilder();
    gson.registerTypeAdapter(Whiteboard.class, new WhiteboardSerializer());
    Gson parser = gson.create();
    return parser;
  }
}
