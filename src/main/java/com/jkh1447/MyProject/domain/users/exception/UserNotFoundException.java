package com.jkh1447.MyProject.domain.users.exception;

public class UserNotFoundException extends UserException {
    public UserNotFoundException() {
        super(UserErrorCode.USER_NOT_FOUND);
    }
}
