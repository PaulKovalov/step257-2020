package com.google.sticknotesbackend.models;

import com.google.sticknotesbackend.enums.Role;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

@Entity
public class UserBoardRole {
  public static class OnlyUser {
  }

  public @Id Long id;
  public Long boardId;

  @Index
  public Role role;

  @Index
  @Load
  Ref<Whiteboard> board;

  @Index
  @Load
  Ref<User> user;

  public UserBoardRole() {
  }

  public UserBoardRole(Role role, Whiteboard board, User user) {
    this.board = Ref.create(board);
    this.user = Ref.create(user);
    this.role = role;
    this.boardId = board.id; // to speed up validation when only board id is known
  }

  public Whiteboard getBoard() {
    return board.get();
  }

  public User getUser() {
    return user.get();
  }
}
