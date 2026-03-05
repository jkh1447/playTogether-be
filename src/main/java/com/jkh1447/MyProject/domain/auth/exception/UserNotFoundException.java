package com.jkh1447.MyProject.domain.auth.exception;

public class UserNotFoundException extends AuthException {
  public UserNotFoundException() {
    super(AuthErrorCode.USER_NOT_FOUND);
  }
}
