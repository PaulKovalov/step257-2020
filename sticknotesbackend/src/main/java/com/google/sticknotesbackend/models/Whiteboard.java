package com.google.sticknotesbackend.models;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import java.util.ArrayList;

@Entity
public class Whiteboard {
  public @Id Long id;
  public Long creationDate; //it's a timestamp
  private @Load Ref<User> creator;
  public String title;
  public int rows;
  public int cols;
  public String backgroundImg;
  public @Load ArrayList<Ref<Note>> notes = new ArrayList<Ref<Note>>();

  public Whiteboard() {}

  public Whiteboard(String title) {
    this.title = title;
  }

  public User getCreator(){
    return creator.get();
  }

  public void setCreator(User user){
    // for now create a user, later retrieve key of the existing user from datastore
    this.creator = Ref.create(user);
  }
}
