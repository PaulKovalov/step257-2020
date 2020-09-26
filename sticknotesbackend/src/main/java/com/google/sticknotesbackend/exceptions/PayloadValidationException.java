/*
* Copyright 2020 Google LLC
*/
package com.google.sticknotesbackend.exceptions;

public class PayloadValidationException extends Exception {
  public PayloadValidationException(String msg) {
    super(msg);
  }
}
